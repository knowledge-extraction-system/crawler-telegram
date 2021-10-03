package kz.kaznu.telegram.client.services;

import kz.kaznu.telegram.client.models.TelegramChat;
import kz.kaznu.telegram.client.models.enums.TdApiErrorCodeEnum;
import kz.kaznu.telegram.client.repositories.ChatMemberService;
import kz.kaznu.telegram.client.repositories.TelegramChatRepository;
import kz.kaznu.telegram.client.repositories.TelegramChatService;
import kz.kaznu.telegram.client.tdlib.Client;
import kz.kaznu.telegram.client.tdlib.TdApi;
import kz.kaznu.telegram.client.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TelegramChatInfoService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    private ChatMemberService chatMemberService;

    @Autowired
    private TelegramChatService telegramChatService;

    @Autowired
    private TelegramChatRepository telegramChatRepository;

    /***
     * Updates chat members from active chats.
     * Chat is active if it has messages from yesterday.
     */
    public void updateChatMembersFromActiveChats() {
        final List<TelegramChat> telegramChats = telegramChatService.getAllActiveTelegramChats();
        for (TelegramChat telegramChat : telegramChats) {
            if (telegramChat.getType().equals("ChatTypeSupergroup")) {
                try {
                    updateTelegramSuperGroupChatMembers(telegramChat.getId().toString());
                } catch (Exception e) {
                    LOGGER.error("Error after updateTelegramSuperGroupChatMembers. " + e.getMessage());
                    e.printStackTrace();
                }
            } else if (telegramChat.getType().equals("ChatTypeBasicGroup")) {
                try {
                    updateTelegramBasicGroupChatMembers(telegramChat.getId().toString());
                } catch (Exception e) {
                    LOGGER.error("Error after updateTelegramBasicGroupChatMembers. " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateTelegramSuperGroupChatMembers(String chatId) throws InterruptedException {
        LOGGER.info("updateTelegramChatMembers started");
        final Integer chatIdAsInteger = Utils.convertChatIdFromStringToInt(chatId);
        final Client client = UpdatesHandler.getClient();
        final TdApi.SupergroupMembersFilterSearch filter = new TdApi.SupergroupMembersFilterSearch();

        final int[] offset = {0};
        int limit = 200;
        final int[] chatMembersSize = {0};
        final boolean[] isFinished = {false};

        client.send(new TdApi.GetSupergroupMembers(chatIdAsInteger, filter, offset[0], limit), object -> {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR:
                    LOGGER.error("Receive an error for GetSupergroupMembers:\n" + object);
                    break;
                case TdApi.ChatMembers.CONSTRUCTOR:
                    LOGGER.info("TelegramChatInfoService:\n " + object.toString());
                    if (chatMembersSize[0] == 0) {
                        chatMembersSize[0] = ((TdApi.ChatMembers) object).totalCount;
                    }
                    break;
                default:
                    LOGGER.error("Receive wrong response from TDLib:\n" + object);
            }
            isFinished[0] = true;
        });
        while (!isFinished[0]) {
            Thread.sleep(1000);
        }
        LOGGER.info("ChatID: " + chatIdAsInteger + "; Size " + chatMembersSize[0]);

        do {
            LOGGER.info("ChatID: " + chatIdAsInteger + "; Search from " + offset[0] + " to " + (offset[0] + limit));
            client.send(new TdApi.GetSupergroupMembers(chatIdAsInteger, filter, offset[0], limit), object -> {
                switch (object.getConstructor()) {
                    case TdApi.Error.CONSTRUCTOR:
                        LOGGER.error("Receive an error for GetSupergroupMembers:\n" + object);
                        break;
                    case TdApi.ChatMembers.CONSTRUCTOR:
                        LOGGER.info("TelegramChatInfoService:\n " + object.toString());
                        if (chatMembersSize[0] == 0) {
                            chatMembersSize[0] = ((TdApi.ChatMembers) object).totalCount;
                        }
                        final List<TdApi.ChatMember> chatMembers = new ArrayList<>(Arrays.asList(((TdApi.ChatMembers) object).members));
                        LOGGER.info("Save ChatMembers " + chatId + ":\n" + "count - " + chatMembers.size());
                        chatMemberService.saveChatMembers(Long.parseLong(chatId), chatMembers);
                        break;
                    default:
                        LOGGER.error("Receive wrong response from TDLib:\n" + object);
                }
            });
            offset[0] += limit;
        } while (offset[0] < chatMembersSize[0] || offset[0] == 0);
        LOGGER.info("updateTelegramChatMembers finished");
    }

    private void updateTelegramBasicGroupChatMembers(String chatId) {
        LOGGER.info("updateTelegramBasicGroupChatMembers started");
        final Integer chatIdAsInteger = Utils.convertChatIdFromStringToInt(chatId);
        final Client client = UpdatesHandler.getClient();

        AtomicInteger chatMembersCount = new AtomicInteger();
        client.send(new TdApi.GetBasicGroup(chatIdAsInteger), object -> {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR:
                    LOGGER.error("Receive an error for GetBasicGroup:\n" + object);
                    break;
                case TdApi.ChatMembers.CONSTRUCTOR://TODO: weird
                    LOGGER.info("GetBasicGroup response:\n" + object.toString());
                    chatMembersCount.set(((TdApi.BasicGroup) object).memberCount);
                    break;
                default:
                    LOGGER.error("Receive wrong response from TDLib:\n" + object);
            }
        });

        client.send(new TdApi.GetBasicGroupFullInfo(chatIdAsInteger), object -> {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR:
                    LOGGER.error("Receive an error for SearchChatMembers:\n" + object);
                    break;
                case TdApi.BasicGroupFullInfo.CONSTRUCTOR:
                    LOGGER.info("SearchChatMembers response:\n" + object.toString());
                    final List<TdApi.ChatMember> chatMembers = new ArrayList<>(Arrays.asList(((TdApi.BasicGroupFullInfo) object).members));
                    LOGGER.info("Save ChatMembers:\n" + "totalCount - " + chatMembers.size());
                    chatMemberService.saveChatMembers(Long.parseLong(chatId), chatMembers);
                    break;
                default:
                    LOGGER.error("Receive wrong response from TDLib:\n" + object);
            }
        });
        LOGGER.info("updateTelegramBasicGroupChatMembers finished");
    }

    public int updateTelegramSuperGroupChatMembersWithSleep(String chatId) throws Exception {
        LOGGER.info("updateTelegramChatMembers started");
        final Integer chatIdAsInteger = Utils.convertChatIdFromStringToInt(chatId);
        final Client client = UpdatesHandler.getClient();
        final TdApi.SupergroupMembersFilterSearch filter = new TdApi.SupergroupMembersFilterSearch();

        final int[] offset = {0};
        int limit = 200;
        final int[] chatMembersSize = {0};
        final boolean[] isFinished = {false};

        final List<TdApi.ChatMember> chatMembers = new ArrayList<>();
        do {
            LOGGER.info("ChatID: " + chatIdAsInteger + "; Search from " + offset[0] + " to " + (offset[0] + limit) +
                    ". ChatMembers size: " + chatMembers.size());
            client.send(new TdApi.GetSupergroupMembers(chatIdAsInteger, filter, offset[0], limit), object -> {
                switch (object.getConstructor()) {
                    case TdApi.Error.CONSTRUCTOR:
                        LOGGER.error("Receive an error for GetSupergroupMembers:\n" + object);
                        break;
                    case TdApi.ChatMembers.CONSTRUCTOR:
                        LOGGER.info("TelegramChatInfoService:\n " + object.toString());
                        if (chatMembersSize[0] == 0) {
                            chatMembersSize[0] = ((TdApi.ChatMembers) object).totalCount;
                        }
                        offset[0] += limit;
                        chatMembers.addAll(Arrays.asList(((TdApi.ChatMembers) object).members));
                        break;
                    default:
                        LOGGER.error("Receive wrong response from TDLib:\n" + object);
                }
                isFinished[0] = true;
            });
            while (!isFinished[0]) {
                Thread.sleep(1000);
            }
            isFinished[0] = false;
            LOGGER.info("While loop finished");
        } while (offset[0] < chatMembersSize[0]);

        LOGGER.info("Save ChatMembers:\n" + "totalCount - " + chatMembers.size());
        chatMemberService.saveChatMembers(Long.parseLong(chatId), chatMembers);
        LOGGER.info("updateTelegramChatMembers finished");
        return chatMembers.size();
    }

    /***
     * Strange behavior. If we call GetBasicGroupFullInfo, then we get 'Chat not found'.
     * However, if we call GetBasicGroupFullInfo after GetBasicGroup, then we get correct response.
     * ¯\_(ツ)_/¯
     * @param chatId
     * @throws Exception
     */
    public int updateTelegramBasicGroupChatMembersWithSleep(String chatId) throws Exception {
        LOGGER.info("updateTelegramBasicGroupChatMembers started");
        final Integer chatIdAsInteger = Utils.convertChatIdFromStringToInt(chatId);
        final Client client = UpdatesHandler.getClient();

        final boolean[] isFinished = {false};

        AtomicInteger chatMembersCount = new AtomicInteger();
        client.send(new TdApi.GetBasicGroup(chatIdAsInteger), object -> {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR:
                    LOGGER.error("Receive an error for GetBasicGroup:\n" + object);
                    break;
                case TdApi.ChatMembers.CONSTRUCTOR://TODO: weird
                    LOGGER.info("GetBasicGroup response:\n" + object.toString());
                    chatMembersCount.set(((TdApi.BasicGroup) object).memberCount);
                    break;
                default:
                    LOGGER.error("Receive wrong response from TDLib:\n" + object);
            }
            isFinished[0] = true;
        });
        while (!isFinished[0]) {
            Thread.sleep(1000);
        }
        isFinished[0] = false;

        final List<TdApi.ChatMember> chatMembers = new ArrayList<>();
        client.send(new TdApi.GetBasicGroupFullInfo(chatIdAsInteger), object -> {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR:
                    LOGGER.error("Receive an error for SearchChatMembers:\n" + object);
                    break;
                case TdApi.BasicGroupFullInfo.CONSTRUCTOR:
                    LOGGER.info("SearchChatMembers response:\n" + object.toString());
                    chatMembers.addAll(Arrays.asList(((TdApi.BasicGroupFullInfo) object).members));
                    break;
                default:
                    LOGGER.error("Receive wrong response from TDLib:\n" + object);
            }
            isFinished[0] = true;
        });
        while (!isFinished[0]) {
            Thread.sleep(1000);
        }

        LOGGER.info("Save ChatMembers:\n" + "totalCount - " + chatMembers.size());
        chatMemberService.saveChatMembers(Long.parseLong(chatId), chatMembers);
        LOGGER.info("updateTelegramBasicGroupChatMembers finished");
        return chatMembers.size();
    }

    public void getChatMemberInfo(long chatId, int userId) throws InterruptedException {
        LOGGER.info("getChatMemberInfo: chat - " + chatId + "; user - " + userId);
        final boolean[] isFinished = {false};
        final Client client = UpdatesHandler.getClient();
        client.send(new TdApi.GetChatMember(chatId, userId), object -> {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR:
                    LOGGER.error("Receive an error for GetChatMember:\n" + object);
                    break;
                case TdApi.ChatMember.CONSTRUCTOR:
                    LOGGER.info("GetChatMember response:\n" + object.toString());
                    chatMemberService.saveChatMember(chatId, (TdApi.ChatMember) object);
                    break;
                default:
                    LOGGER.error("Receive wrong response from TDLib:\n" + object);
            }
            isFinished[0] = true;
        });
        int waitCount = 0;
        while (!isFinished[0] && waitCount < 10) {
            Thread.sleep(10);
            waitCount++;
        }
    }

    public void checkUserMembership(int userId) throws InterruptedException {
        final List<TelegramChat> telegramChats = telegramChatService.getAllTelegramChatsWhereUserIsNotAMember(userId);
        for (TelegramChat telegramChat : telegramChats) {
            LOGGER.info("Chat: " + telegramChat.getTitle() + " - " + telegramChat.getId());
            getGroupMemberInfo(telegramChat.getId(), telegramChat.getTitle(), userId);
        }
        LOGGER.info("Checked user " + userId + " membership in " + telegramChats.size() + " chats.");
    }

    private void getGroupMemberInfo(long chatId, String chatTitle, int userId) throws InterruptedException {
        LOGGER.info("getChatMemberInfoWithoutSave: chat - " + chatId + "; user - " + userId);
        final Client client = UpdatesHandler.getClient();

        final boolean[] isFinished = {false};
        client.send(new TdApi.OpenChat(chatId), object -> {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR:
                    LOGGER.error("Receive an error for SearchPublicChats:\n" + object);
                    break;
                case TdApi.Ok.CONSTRUCTOR:
                    LOGGER.info("SearchPublicChats response:\n" + object.toString());
                    break;
                default:
                    LOGGER.error("Receive wrong response from TDLib:\n" + object);
            }
            isFinished[0] = true;
        });
        int waitCount = 0;
        while (!isFinished[0] && waitCount < 10) {
            Thread.sleep(10);
            waitCount++;
        }

        getChatMemberInfo(chatId, userId);
    }

    public void joinChats(int userId) throws InterruptedException {
        LOGGER.info("joinChats; user - " + userId);
        final List<TelegramChat> telegramChats = telegramChatService.getAllTelegramChatsWhereUserIsNotAMember(userId);
        for (TelegramChat telegramChat : telegramChats) {
            LOGGER.info("Chat: " + telegramChat.getTitle() + " - " + telegramChat.getId());
            join(telegramChat.getId(), telegramChat.getTitle(), userId);
        }
    }

    public void join(long chatId, String chatTitle, int userId) throws InterruptedException {
        LOGGER.info("join: chat - " + chatId + "; user - " + userId);
        final Client client = UpdatesHandler.getClient();

        final boolean[] isFinished = {false};

        client.send(new TdApi.SearchPublicChats(chatTitle), object -> {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR:
                    LOGGER.error("Receive an error for SearchPublicChats:\n" + object);
                    break;
                case TdApi.SearchPublicChats.CONSTRUCTOR:
                    LOGGER.info("SearchPublicChats response:\n" + object.toString());
                    break;
                default:
                    LOGGER.error("Receive wrong response from TDLib:\n" + object);
            }
            isFinished[0] = true;
        });
        while (!isFinished[0]) {
            Thread.sleep(10);
        }

        client.send(new TdApi.JoinChat(chatId), object -> {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR:
                    LOGGER.error("Receive an error for SearchPublicChats:\n" + object);
                    break;
                case TdApi.SearchPublicChats.CONSTRUCTOR:
                    LOGGER.info("SearchPublicChats response:\n" + object.toString());
                    break;
                default:
                    LOGGER.error("Receive wrong response from TDLib:\n" + object);
            }
            isFinished[0] = true;
        });

        while (!isFinished[0]) {
            Thread.sleep(5000);
        }
    }

    public void addSuperGroupsDescription() {
        final List<TelegramChat> telegramChats = telegramChatService.getAllTelegramSuperGroups();
        LOGGER.info("addSuperGroupsDescription count : " + telegramChats.size());
        final Client client = UpdatesHandler.getClient();
        for (TelegramChat telegramChat : telegramChats) {
            LOGGER.info("Chat: " + telegramChat.getTitle() + " - " + telegramChat.getId());
            setChatDescription(client, telegramChat.getId());
        }
    }

    public void addSuperGroupDescription(Long chatId) {
        final Client client = UpdatesHandler.getClient();
        setChatDescription(client, chatId);
    }

    private void setChatDescription(Client client, Long chatId) {
        final Integer chatIdAsInteger = Utils.convertChatIdFromStringToInt(chatId.toString());
        client.send(new TdApi.GetSupergroupFullInfo(chatIdAsInteger), object -> {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR:
                    LOGGER.error("Receive an error for SupergroupFullInfo(" + chatId + "):\n" + object);
                    TdApi.Error error = (TdApi.Error) object;
                    if (error.code == TdApiErrorCodeEnum.SUPERGROUP_NOT_FOUND.getCode()) {
                        telegramChatRepository.updateIsDeleted(true, chatId);
                    }
                    telegramChatRepository.updateIsActive(false, chatId);

                    checkChatType(client, chatId);
                    break;
                case TdApi.SupergroupFullInfo.CONSTRUCTOR:
                    LOGGER.info("SupergroupFullInfo(" + chatId + ") response:\n" + object.toString());
                    TdApi.SupergroupFullInfo fullInfo = (TdApi.SupergroupFullInfo) object;
                    telegramChatRepository.updateChatInfo(fullInfo.description, fullInfo.memberCount, chatId);
                    break;
                default:
                    LOGGER.error("Receive wrong response from SupergroupFullInfo(" + chatId + "):\n" + object);
            }
        });
    }

    private void checkChatType(Client client, Long chatId) {
        client.send(new TdApi.GetChat(chatId), object -> {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR:
                    LOGGER.error("Receive an error for Chat(" + chatId + "):\n" + object);
                    break;
                case TdApi.Chat.CONSTRUCTOR:
                    LOGGER.info("Chat(" + chatId + ") response:\n" + object.toString());
                    TdApi.Chat chat = (TdApi.Chat) object;
                    telegramChatRepository.updateChatInfo(chat.type.getClass().getSimpleName(), chatId);
                    break;
                default:
                    LOGGER.error("Receive wrong response from Chat(" + chatId + "):\n" + object);
            }
        });
    }

}

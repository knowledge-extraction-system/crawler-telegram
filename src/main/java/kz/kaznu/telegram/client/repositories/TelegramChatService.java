package kz.kaznu.telegram.client.repositories;

import kz.kaznu.telegram.client.models.TelegramChat;
import kz.kaznu.telegram.client.models.enums.ChatTypeEnum;
import kz.kaznu.telegram.client.services.TelegramChatInfoService;
import kz.kaznu.telegram.client.services.UpdatesHandler;
import kz.kaznu.telegram.client.tdlib.Client;
import kz.kaznu.telegram.client.tdlib.TdApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TelegramChatService {

    @Autowired
    private TelegramChatRepository telegramChatRepository;

    @Autowired
    private TelegramChatInfoService telegramChatInfoService;

    public void saveIfNewTelegramChat(TdApi.Chat chat) {
        TelegramChat telegramChat = telegramChatRepository.findById(chat.id);
        if (telegramChat == null) {
            telegramChat = new TelegramChat(chat);
            telegramChatRepository.insert(telegramChat);
            telegramChatInfoService.addSuperGroupDescription(telegramChat.getId());
        } else if (telegramChat.getIsChannel() == null) {
            telegramChatRepository.updateIsChannel(((TdApi.ChatTypeSupergroup) chat.type).isChannel, telegramChat.getId());
        }
    }

    public List<TelegramChat> getAllTelegramChatsWhereUserIsNotAMember(int userId) {
        return telegramChatRepository.findAllWhereUserIsNotAMember(userId);
    }

    public List<TelegramChat> getAllTelegramChatsWhereUserIsAMember(int userId) {
        return telegramChatRepository.findAllWhereUserIsAMember(userId);
    }

    public List<TelegramChat> getAllActiveTelegramChats() {
        return telegramChatRepository.findAllChatsWithMessagesFromYesterday();
    }

    public List<TelegramChat> getAllTelegramSuperGroups() {
        return telegramChatRepository.findAllByTypeAndEmptyDescriptionAndActiveTrue(ChatTypeEnum.ChatTypeSupergroup);
    }

    public List<TelegramChat> getAllTelegramBasicGroups() {
        return telegramChatRepository.findAllByTypeAndEmptyDescriptionAndActiveTrue(ChatTypeEnum.ChatTypeBasicGroup);
    }
}

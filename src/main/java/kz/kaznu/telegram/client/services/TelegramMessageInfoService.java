package kz.kaznu.telegram.client.services;

import kz.kaznu.telegram.client.models.TelegramChat;
import kz.kaznu.telegram.client.repositories.TelegramChatService;
import kz.kaznu.telegram.client.tdlib.Client;
import kz.kaznu.telegram.client.tdlib.TdApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TelegramMessageInfoService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getSimpleName());
    final int USER_ID = 1065207797;

    @Autowired
    private TelegramChatService telegramChatService;

    public void viewMessages() throws InterruptedException {
        final List<TelegramChat> chats = telegramChatService.getAllTelegramChatsWhereUserIsAMember(USER_ID);
        int count = 0;
        for (TelegramChat chat : chats) {
            viewMessagesInChat(chat.getId());
            count++;
            Thread.sleep(1000);
        }
        LOGGER.info("Messages marked as read in " + count + " chats.");
    }

    public void viewMessagesInChat(long chatId) throws InterruptedException {
        LOGGER.info("viewMessages: chat - " + chatId);
        final Client client = UpdatesHandler.getClient();

        final boolean[] isFinished = {false};
        final long[] messages = new long[1];
        final boolean[] hasUnreadMessages = {true};

        client.send(new TdApi.OpenChat(chatId), object -> {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR:
                    LOGGER.error("Receive an error for ViewMessages:\n" + object);
                    break;
                case TdApi.Ok.CONSTRUCTOR:
                    LOGGER.info("OpenChat response:\n" + object.toString());
                    break;
                default:
                    LOGGER.error("Receive wrong response from TDLib:\n" + object);
            }
            isFinished[0] = true;
        });
        while (!isFinished[0]) {
            Thread.sleep(10);
        }
        isFinished[0] = false;

        client.send(new TdApi.GetChat(chatId), object -> {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR:
                    LOGGER.error("Receive an error for ViewMessages:\n" + object);
                    break;
                case TdApi.Chat.CONSTRUCTOR:
                    LOGGER.info("ViewMessages response:\n" + object.toString());
                    final TdApi.Chat chat = (TdApi.Chat) object;
                    messages[0] = chat.lastMessage.id;
                    hasUnreadMessages[0] = chat.unreadCount != 0;
                    break;
                default:
                    LOGGER.error("Receive wrong response from TDLib:\n" + object);
            }
            isFinished[0] = true;
        });
        while (!isFinished[0]) {
            Thread.sleep(10);
        }

        if (hasUnreadMessages[0]) {
            isFinished[0] = false;
//            client.send(new TdApi.ViewMessages(chatId, messages, false), object -> {
//                switch (object.getConstructor()) {
//                    case TdApi.Error.CONSTRUCTOR:
//                        LOGGER.error("Receive an error for ViewMessages:\n" + object);
//                        break;
//                    case TdApi.Ok.CONSTRUCTOR:
//                        LOGGER.info("ViewMessages response:\n" + object.toString());
//                        break;
//                    default:
//                        LOGGER.error("Receive wrong response from TDLib:\n" + object);
//                }
//                isFinished[0] = true;
//            });
        }
        while (!isFinished[0]) {
            Thread.sleep(10);
        }
    }

}

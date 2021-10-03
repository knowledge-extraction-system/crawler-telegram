package kz.kaznu.telegram.client.repositories;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kz.kaznu.telegram.client.configs.RabbitMQConfig;
import kz.kaznu.telegram.client.models.TelegramChat;
import kz.kaznu.telegram.client.models.TelegramMessage;
import kz.kaznu.telegram.client.models.TelegramUser;
import kz.kaznu.telegram.client.models.logger.MessageDeletedLogging;
import kz.kaznu.telegram.client.models.logger.MessageLogging;
import kz.kaznu.telegram.client.models.shorts.TelegramMessageShortInfo;
import kz.kaznu.telegram.client.services.TelegramChatInfoService;
import kz.kaznu.telegram.client.tdlib.TdApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TelegramMessageService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getSimpleName());
    private Gson gson = new GsonBuilder().create();

    private final TelegramMessageRepository telegramMessageRepository;
    private final TelegramChatRepository telegramChatRepository;
    private final TelegramUserRepository telegramUserRepository;

    private final TelegramChatInfoService telegramChatInfoService;

    private final AmqpTemplate amqpTemplate;
    private final RabbitMQConfig rabbitMQConfig;

    @Autowired
    public TelegramMessageService(TelegramMessageRepository telegramMessageRepository, TelegramChatRepository telegramChatRepository,
                                  TelegramUserRepository telegramUserRepository, TelegramChatInfoService telegramChatInfoService,
                                  AmqpTemplate amqpTemplate, RabbitMQConfig rabbitMQConfig) {
        this.telegramMessageRepository = telegramMessageRepository;
        this.telegramChatRepository = telegramChatRepository;
        this.telegramUserRepository = telegramUserRepository;

        this.telegramChatInfoService = telegramChatInfoService;

        this.amqpTemplate = amqpTemplate;
        this.rabbitMQConfig = rabbitMQConfig;
    }

    public void saveIfNewTelegramMessage(TdApi.Message message) throws InterruptedException {
        Integer userId = null;
        if (message.sender instanceof TdApi.MessageSenderUser) {
            userId = ((TdApi.MessageSenderUser) message.sender).userId;
            LOGGER.info("Sender is a user " + userId);
        } else {
            LOGGER.info("Sender is a chat!");
        }
        final boolean isMessageExists = telegramMessageRepository.checkIfMessageExistsByMessageIdAndTelegramUserOrTelegramChat(
                (int) message.id, userId, (int) message.chatId);

        if (isMessageExists) {
            //TODO: update
            return;
        }

        final TelegramChat telegramChat = telegramChatRepository.findById(message.chatId);
        final TelegramUser telegramUser = userId == null ? null : telegramUserRepository.findById((long) userId);
        final LocalDateTime messageDate = new Date((long)message.date*1000).toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();

        TelegramMessage telegramMessage = null;
        Long telegramMessageId = null;
        switch (message.content.getConstructor()) {
            case TdApi.MessageText.CONSTRUCTOR:
                LOGGER.info("Message type is MessageText");
                final TdApi.MessageText messageText = (TdApi.MessageText) message.content;
                telegramMessage = new TelegramMessage(telegramChat, telegramUser, messageText.text.text, message.id,
                        messageDate, message.replyToMessageId, message.messageThreadId);
                telegramMessageId = telegramMessageRepository.insert(telegramMessage);
                break;
            case TdApi.MessageVideo.CONSTRUCTOR:
                LOGGER.info("Message type is MessageVideo");
                final TdApi.MessageVideo messageVideo = (TdApi.MessageVideo) message.content;
                telegramMessage = new TelegramMessage(telegramChat, telegramUser, messageVideo.caption.text, message.id,
                        messageDate, message.replyToMessageId, message.messageThreadId);
                telegramMessageId = telegramMessageRepository.insert(telegramMessage);
                break;
            case TdApi.MessagePhoto.CONSTRUCTOR:
                LOGGER.info("Message type is MessagePhoto");
                final TdApi.MessagePhoto messagePhoto = (TdApi.MessagePhoto) message.content;
                telegramMessage = new TelegramMessage(telegramChat, telegramUser, messagePhoto.caption.text, message.id,
                        messageDate, message.replyToMessageId, message.messageThreadId);
                telegramMessageId = telegramMessageRepository.insert(telegramMessage);
                break;
            case TdApi.MessageChatAddMembers.CONSTRUCTOR:
                LOGGER.info("Message type is MessageChatAddMembers");
                final TdApi.MessageChatAddMembers messageChatAddMembers = (TdApi.MessageChatAddMembers) message.content;
                for (int i = 0; i < messageChatAddMembers.memberUserIds.length; i++) {
                    telegramChatInfoService.getChatMemberInfo(telegramChat.getId(), messageChatAddMembers.memberUserIds[i]);
                }
            default:
                LOGGER.warn("Unhandled message type: " + message.content.getConstructor());
        }
        LOGGER.info("TelegramMessageId: " + telegramMessageId);

        if (telegramMessage != null) {
            telegramMessage.setId(telegramMessageId);
            final TelegramMessageShortInfo telegramMessageShortInfo = new TelegramMessageShortInfo(telegramMessage);
            amqpTemplate.convertAndSend(rabbitMQConfig.getMessageFanoutExchange(), "", telegramMessageShortInfo);
            logMessage(telegramChat, telegramUser, telegramMessage);
        }
    }

    private void logMessage(TelegramChat telegramChat, TelegramUser telegramUser, TelegramMessage telegramMessage) {
        LOGGER.info("New message saved to db:");
        LOGGER.info(gson.toJson(telegramMessage));
        final MessageLogging messageLogging = new MessageLogging(telegramMessage.getMessageId(), telegramMessage.getMessage(), telegramUser != null ? telegramUser.getId() : telegramChat.getId(),
                telegramChat != null ? telegramChat.getType() : null, telegramUser != null ? telegramUser.getType() : null);
        LOGGER.info(gson.toJson(messageLogging));
    }

    public void updateDeletedMessages(TdApi.UpdateDeleteMessages messages) {
        if (messages.fromCache) {
            // If it is not Permanent than deletion is fromCache
            return;
        }
        final List<Long> messageIds = Arrays.stream(messages.messageIds).boxed().collect(Collectors.toList());
        final List<TelegramMessage> telegramMessages = telegramMessageRepository.findByMessageIdsAndChatId(messageIds, messages.chatId);

        if (telegramMessages.size() == 0) {
            LOGGER.info("Messages for delete were not found");
            return;
        }

        telegramMessages.forEach(telegramMessage -> telegramMessage.setDeleted(true));
        telegramMessages.forEach(telegramMessage -> telegramMessage.setDeletionDate(LocalDateTime.now()));
        telegramMessageRepository.updateAllDeletedMessages(telegramMessages);
        LOGGER.info("Messages updated");
        telegramMessages.stream().map(telegramMessage -> new MessageDeletedLogging(telegramMessage.getMessageId(), true))
                .forEach(messageDeletedLogging -> LOGGER.info(gson.toJson(messageDeletedLogging)));
    }

//    public void updateMessageViews(TdApi.UpdateMessageViews updateMessageViews) {
//        telegramMessageRepository.updateMessageViews(updateMessageViews.messageId, updateMessageViews.chatId,
//                updateMessageViews.views);
//    }

}

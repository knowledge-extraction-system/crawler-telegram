package kz.kaznu.telegram.client.models;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TelegramMessage {

    private Long id;
    private TelegramChat telegramChat;
    private TelegramUser telegramUser;
    private String message;
    private LocalDateTime date;
    private LocalDateTime messageDate;
    private Long messageId;
    private boolean isDeleted;
    private LocalDateTime deletionDate;
    private Long replyToMessageId;
    private Long messageThreadId;

    public TelegramMessage(TelegramChat telegramChat, TelegramUser telegramUser, String message, Long messageId,
                           LocalDateTime messageDate, Long replyToMessageId, Long messageThreadId) {
        this.telegramChat = telegramChat;
        this.telegramUser = telegramUser;
        this.message = message;
        this.date = LocalDateTime.now();
        this.messageDate = messageDate;
        this.messageId = messageId;
        this.isDeleted = false;
        this.replyToMessageId = replyToMessageId;
        this.messageThreadId = messageThreadId;
    }
}

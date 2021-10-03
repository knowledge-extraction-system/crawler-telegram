package kz.kaznu.telegram.client.models.shorts;

import kz.kaznu.telegram.client.models.TelegramMessage;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TelegramMessageShortInfo {

    private Long chatId;
    private Long userId;
    private Long messageId;
    private String message;
    private String osn;

    public TelegramMessageShortInfo(TelegramMessage telegramMessage) {
        this.chatId = telegramMessage.getTelegramChat().getId();
        this.userId = telegramMessage.getTelegramUser().getId();
        this.messageId = telegramMessage.getId();
        this.message = telegramMessage.getMessage();
        this.osn = "TELEGRAM";
    }
}

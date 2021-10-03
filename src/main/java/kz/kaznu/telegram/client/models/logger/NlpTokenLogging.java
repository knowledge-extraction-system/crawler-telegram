package kz.kaznu.telegram.client.models.logger;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NlpTokenLogging {

    private String logClass = NlpTokenLogging.class.getSimpleName();
    private String token;

    private Long telegramChatId;
    private String chatTitle;
    private Long countByChat;

    private Long telegramUserId;
    private String userName;
    private Long countByUser;

    public NlpTokenLogging(String token) {
        this.token = token;
    }
}

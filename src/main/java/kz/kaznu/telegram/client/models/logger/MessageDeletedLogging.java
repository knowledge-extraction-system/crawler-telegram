package kz.kaznu.telegram.client.models.logger;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MessageDeletedLogging {

    private Long telegramMessageId;
    private boolean isDeleted;

}

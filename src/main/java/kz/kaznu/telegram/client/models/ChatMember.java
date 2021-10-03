package kz.kaznu.telegram.client.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMember {

    private Long telegramChatId;
    private int telegramUserId;
    private String status;

}

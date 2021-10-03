package kz.kaznu.telegram.client.models;

import kz.kaznu.telegram.client.tdlib.TdApi;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TelegramChat {

    private Long id;
    private String type;
    private String title;
    private LocalDateTime date;
    private Boolean isChannel;
    private Long countryId;
    private String description;
    private Boolean isDeleted = false;
    private Boolean isActive = true;
    private int memberCount = 0;

    public TelegramChat(TdApi.Chat chat) {
        this.id = chat.id;
        this.type = chat.type.getClass().getSimpleName();
        this.title = chat.title;
        this.date = LocalDateTime.now();
        this.isChannel = ((TdApi.ChatTypeSupergroup) chat.type).isChannel;
    }

    public TelegramChat(Long id) {
        this.id = id;
    }
}

package kz.kaznu.telegram.client.models;

import kz.kaznu.telegram.client.models.enums.Gender;
import kz.kaznu.telegram.client.tdlib.TdApi;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TelegramUser {

    private Long id;
    private String firstName = "";
    private String lastName = "";
    private String userName = "";
    private String phoneNumber = "";
    private String languageCode;
    private String type;
    private Long countryId;
    private Gender gender;
    private LocalDateTime date;

    public TelegramUser(TdApi.User user) {
        this.id = (long) user.id;
        this.firstName = user.firstName;
        this.lastName = user.lastName;
        this.userName = user.username;
        this.phoneNumber = user.phoneNumber;
        this.languageCode = user.languageCode;
        this.date = LocalDateTime.now();
        this.type = user.type.getClass().getSimpleName();
    }

    public TelegramUser(Long id) {
        this.id = id;
    }
}

package kz.kaznu.telegram.client.repositories;

import kz.kaznu.telegram.client.models.TelegramUser;
import kz.kaznu.telegram.client.tdlib.TdApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TelegramUserService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    private TelegramUserRepository telegramUserRepository;

    public void saveIfNewTelegramUser(TdApi.User user) {
        LOGGER.info("saveIfNewTelegramUser");
        boolean isUserExist = telegramUserRepository.checkIfUserExistsById((long) user.id);
        if (!isUserExist) {
            final TelegramUser telegramUser = new TelegramUser(user);
            telegramUserRepository.insert(telegramUser);
        }
    }

}

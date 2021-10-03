package kz.kaznu.telegram.client.services;

import kz.kaznu.telegram.client.models.TelegramUser;
import kz.kaznu.telegram.client.repositories.TelegramUserRepository;
import kz.kaznu.telegram.client.tdlib.Client;
import kz.kaznu.telegram.client.tdlib.TdApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TelegramUserInfoService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    private TelegramUserRepository telegramUserRepository;

    public void getUserFullInfo(int userId) throws InterruptedException {
        final Client client = UpdatesHandler.getClient();

        final boolean[] isFinished = {false};
        client.send(new TdApi.GetUser(userId), object -> {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR:
                    System.err.println("Receive an error for GetSupergroupMembers:\n" + object);
                    break;
                case TdApi.UserFullInfo.CONSTRUCTOR:
                    LOGGER.info("TelegramUserInfoService getUserFullInfo:\n " + object.toString());
                    final TdApi.User userFullInfo = (TdApi.User) object;
                    final TelegramUser telegramUser = new TelegramUser(userFullInfo);
                    telegramUserRepository.insert(telegramUser);
                    isFinished[0] = true;
                    break;
                default:
                    System.err.println("Receive wrong response from TDLib:\n" + object);
            }
        });
        while (!isFinished[0]) {
            Thread.sleep(1000);
        }
    }

}

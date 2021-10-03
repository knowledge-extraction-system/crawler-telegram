package kz.kaznu.telegram.client.schedulers;

import kz.kaznu.telegram.client.services.TelegramChatInfoService;
import kz.kaznu.telegram.client.services.TelegramMessageInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

@Service
@Component
public class ChatMemberScheduler {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final Random random = new Random();

    private final TelegramChatInfoService telegramChatInfoService;

    @Autowired
    public ChatMemberScheduler(TelegramChatInfoService telegramChatInfoService) {
        this.telegramChatInfoService = telegramChatInfoService;
    }

//        @Scheduled(cron="0 18 0 * * ?") // for tests
    @Scheduled(cron="0 0 1 * * ?") // for prod
    public void addChatMembers() throws InterruptedException {
        final Date now = new Date();
        LOGGER.info("We are here in time before: " + dateFormat.format(now));
//        final int millisForWait = (1 + random.nextInt(30))*1000; // for tests
        final int millisForWait = (1 + random.nextInt(10))*60*1000; // for prod
        LOGGER.info("Wait for " + millisForWait);

        Thread.sleep(millisForWait);
        final Date after = new Date();
        LOGGER.info("We are here in time after: " + dateFormat.format(after));

        telegramChatInfoService.updateChatMembersFromActiveChats();
        LOGGER.info("Finished addChatMembers");
    }

}

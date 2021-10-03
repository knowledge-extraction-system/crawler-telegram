package kz.kaznu.telegram.client.schedulers;

import kz.kaznu.telegram.client.services.TelegramMessageInfoService;
import kz.kaznu.telegram.client.services.UpdatesHandler;
import kz.kaznu.telegram.client.tdlib.Client;
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
public class MarkAsReadScheduler {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getSimpleName());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final Random random = new Random();

    private final TelegramMessageInfoService telegramMessageInfoService;

    @Autowired
    public MarkAsReadScheduler(TelegramMessageInfoService telegramMessageInfoService) {
        this.telegramMessageInfoService = telegramMessageInfoService;
    }

//    @Scheduled(cron="0 5,56 1,2 * * ?") // for tests
    @Scheduled(cron="0 0 10,18 * * ?") // for prod
    public void markAsRead() throws InterruptedException {
        final Date now = new Date();
        LOGGER.info("We are here in time before: " + dateFormat.format(now));
//        final int millisForWait = (1 + random.nextInt(30))*1000; // for tests
        final int millisForWait = (1 + random.nextInt(10))*60*1000; // for prod
        LOGGER.info("Wait for " + millisForWait);

        Thread.sleep(millisForWait);
        final Date after = new Date();
        LOGGER.info("We are here in time after: " + dateFormat.format(after));

        telegramMessageInfoService.viewMessages();
    }

}

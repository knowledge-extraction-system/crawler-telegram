package kz.kaznu.telegram.client.controllers;

import kz.kaznu.telegram.client.services.TelegramMessageInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/message")
public class TelegramMessageInfoController {

    private final TelegramMessageInfoService telegramMessageInfoService;

    @Autowired
    public TelegramMessageInfoController(TelegramMessageInfoService telegramMessageInfoService) {
        this.telegramMessageInfoService = telegramMessageInfoService;
    }

    @RequestMapping(value = "/mark-as-read", method = RequestMethod.GET)
    public void markMessagesAsRead(@RequestParam String chatId) throws Exception {
        telegramMessageInfoService.viewMessagesInChat(Long.parseLong(chatId));
    }

}

package kz.kaznu.telegram.client.controllers;

import kz.kaznu.telegram.client.models.responses.ChatMembersInfoResponse;
import kz.kaznu.telegram.client.services.TelegramChatInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/chat")
public class TelegramChatInfoController {

    private final TelegramChatInfoService telegramChatInfoService;

    @Autowired
    public TelegramChatInfoController(TelegramChatInfoService telegramChatInfoService) {
        this.telegramChatInfoService = telegramChatInfoService;
    }

    @RequestMapping(value = "/super/group/members", method = RequestMethod.GET)
    public ChatMembersInfoResponse getSuperGroupChatMembersInfo(@RequestParam String chatId) throws Exception {
        final int membersSize = telegramChatInfoService.updateTelegramSuperGroupChatMembersWithSleep(chatId);
        return new ChatMembersInfoResponse(membersSize);
    }

    @RequestMapping(value = "/basic/group/members", method = RequestMethod.GET)
    public ChatMembersInfoResponse getBasicGroupChatMembersInfo(@RequestParam String chatId) throws Exception {
        final int membersSize = telegramChatInfoService.updateTelegramBasicGroupChatMembersWithSleep(chatId);
        return new ChatMembersInfoResponse(membersSize);
    }

    @RequestMapping(value = "/check/membership", method = RequestMethod.GET)
    public void checkMembership(@RequestParam int userId) throws Exception {
        telegramChatInfoService.checkUserMembership(userId);
    }

    @RequestMapping(value = "/join/all", method = RequestMethod.GET)
    public void joinAllChats(@RequestParam int userId) throws Exception {
        telegramChatInfoService.joinChats(userId);
    }

    //TODO: create updateInfo API
    @RequestMapping(value = "/add-super-groups-description", method = RequestMethod.GET)
    public void getSuperGroupsFullInfo() {
        telegramChatInfoService.addSuperGroupsDescription();
    }

}

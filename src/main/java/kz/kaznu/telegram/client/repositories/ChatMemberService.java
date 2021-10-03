package kz.kaznu.telegram.client.repositories;

import kz.kaznu.telegram.client.models.ChatMember;
import kz.kaznu.telegram.client.models.TelegramUser;
import kz.kaznu.telegram.client.services.TelegramUserInfoService;
import kz.kaznu.telegram.client.tdlib.TdApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatMemberService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    private ChatMemberRepository chatMemberRepository;
    @Autowired
    private TelegramUserRepository telegramUserRepository;
    @Autowired
    private TelegramUserInfoService telegramUserInfoService;

    public void saveChatMembers(Long chatId, List<TdApi.ChatMember> chatMembers) throws InterruptedException {
        for (TdApi.ChatMember telegramChatMember : chatMembers) {
            final ChatMember chatMember = new ChatMember(chatId, telegramChatMember.userId, telegramChatMember.status.getClass().getSimpleName());
            chatMemberRepository.insert(chatMember);
        }
    }

    public void saveChatMember(Long chatId, TdApi.ChatMember chatMemberTD) {
        final ChatMember chatMember = new ChatMember(chatId, chatMemberTD.userId, chatMemberTD.status.getClass().getSimpleName());
        chatMemberRepository.insert(chatMember);
    }

    public void saveChatMember(long chatId, int userId, String status) {
        final ChatMember chatMember = new ChatMember(chatId, userId, status);
        chatMemberRepository.insert(chatMember);
    }

    private void checkUserExist(int userId) throws InterruptedException {
        final TelegramUser telegramUser = telegramUserRepository.findById((long) userId);
        if (telegramUser == null) {
            telegramUserInfoService.getUserFullInfo(userId);
        }
    }

}

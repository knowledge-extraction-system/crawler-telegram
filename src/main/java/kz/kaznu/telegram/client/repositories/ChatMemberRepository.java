package kz.kaznu.telegram.client.repositories;

import kz.kaznu.telegram.client.models.ChatMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ChatMemberRepository {

    private final JdbcTemplate jdbcTemplate;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    public ChatMemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    void insert(ChatMember chatMember) {
        try {
            String query = "insert into chat_member (telegram_chat_id, telegram_user_id, status) " +
                    "values ( ?, ?, ?) ON CONFLICT DO NOTHING";
            jdbcTemplate.update(query, chatMember.getTelegramChatId(), chatMember.getTelegramUserId(), chatMember.getStatus());
        } catch (Exception e) {
            LOGGER.error("ChatMemberRepository insert error", e);
            throw e;
        }
    }

}

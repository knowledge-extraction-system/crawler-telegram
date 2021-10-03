package kz.kaznu.telegram.client.repositories;

import kz.kaznu.telegram.client.models.TelegramChat;
import kz.kaznu.telegram.client.models.enums.ChatTypeEnum;
import kz.kaznu.telegram.client.models.rowmappers.TelegramChatListRowMapper;
import kz.kaznu.telegram.client.models.rowmappers.TelegramChatShortRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Created by yerzhan on 10/19/19.
 */
@Repository
public class TelegramChatRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TelegramChatRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    TelegramChat findById(Long id) {
        try {
            final String query = "select * from telegram_chat where id=?";
            return jdbcTemplate.queryForObject(query, new Object[]{id}, new TelegramChatShortRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    List<TelegramChat> findAllWhereUserIsNotAMember(int userId) {
        try {
            final String query = "select * from telegram_chat where id not in (select telegram_chat_id from chat_member where telegram_user_id=?)";
            return jdbcTemplate.queryForObject(query, new Object[]{userId}, new TelegramChatListRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    List<TelegramChat> findAllWhereUserIsAMember(int userId) {
        try {
            final String query = "select * from telegram_chat where id in (select telegram_chat_id from chat_member where telegram_user_id=? and status='ChatMemberStatusMember')";
            return jdbcTemplate.queryForObject(query, new Object[]{userId}, new TelegramChatListRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    List<TelegramChat> findAllChatsWithMessagesFromYesterday() {
        try {
            LocalTime midnight = LocalTime.MIDNIGHT;
            LocalDate today = LocalDate.now();
            LocalDateTime yesterdayMidnight = LocalDateTime.of(today, midnight).minusDays(1);
            final String query = "select * from telegram_chat where id in (select telegram_chat_id from telegram_message where date > ?)";
            return jdbcTemplate.queryForObject(query, new Object[]{yesterdayMidnight}, new TelegramChatListRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    List<TelegramChat> findAllByTypeAndEmptyDescriptionAndActiveTrue(ChatTypeEnum type) {
        try {
            final String query = "select * from telegram_chat " +
                    "where \"type\" = ?";
            return jdbcTemplate.queryForObject(query, new Object[]{type.name()}, new TelegramChatListRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    void insert(TelegramChat telegramChat) {
        String query = "insert into telegram_chat (id, type, title, date, is_channel, is_deleted, is_active) " +
                "values ( ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(query, telegramChat.getId(), telegramChat.getType(), telegramChat.getTitle(),
                telegramChat.getDate(), telegramChat.getIsChannel(), telegramChat.getIsDeleted(), telegramChat.getIsActive());
    }

    void updateIsChannel(Boolean isChannel, Long id) {
        final String query = "UPDATE telegram_chat SET IS_CHANNEL = ? WHERE id = ?";
        jdbcTemplate.update(query, isChannel, id);
    }

    public void updateChatInfo(String description, int memberCount, Long id) {
        final String query = "UPDATE telegram_chat SET description = ?, member_count = ? WHERE id = ?";
        jdbcTemplate.update(query, description, memberCount, id);
    }

    public void updateIsDeleted(boolean isDeleted, Long id) {
        final String query = "UPDATE telegram_chat SET is_deleted = ? WHERE id = ?";
        jdbcTemplate.update(query, isDeleted, id);
    }

    public void updateIsActive(boolean isDeleted, Long id) {
        final String query = "UPDATE telegram_chat SET is_active = ? WHERE id = ?";
        jdbcTemplate.update(query, isDeleted, id);
    }

    public void updateChatInfo(String chatType, Long id) {
        final String query = "UPDATE telegram_chat SET \"type\" = ?, is_active = true WHERE id = ?";
        jdbcTemplate.update(query, chatType, id);
    }

}

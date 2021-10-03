package kz.kaznu.telegram.client.repositories;

import kz.kaznu.telegram.client.models.TelegramMessage;
import kz.kaznu.telegram.client.models.rowmappers.TelegramMessageListRowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by yerzhan on 10/21/19.
 */
@Repository
public class TelegramMessageRepository {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final JdbcTemplate jdbcTemplate;
    private final CommonRepository commonRepository;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public TelegramMessageRepository(JdbcTemplate jdbcTemplate, CommonRepository commonRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.commonRepository = commonRepository;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    Boolean checkIfMessageExistsByMessageIdAndTelegramUserOrTelegramChat(Integer messageId, Integer telegramUserId, Integer telegramChatId) {
        final String query = "select exists(select 1 from telegram_message tm where message_id = ? and (telegram_user_id = ? or telegram_chat_id = ?))";
        return jdbcTemplate.queryForObject(query, new Object[]{messageId, telegramUserId, telegramChatId}, Boolean.class);
    }

    Long insert(TelegramMessage telegramMessage) {
        final String query = "insert into telegram_message (" +
                "id, date, message_date, message, message_id, telegram_chat_id, telegram_user_id, deleted, reply_to_message_id, message_thread_id) " +
                "values ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        final Long id = commonRepository.getNextValueForMessageSequence();

        jdbcTemplate.update(query, id, telegramMessage.getDate(), telegramMessage.getMessageDate(), telegramMessage.getMessage(),
                telegramMessage.getMessageId(), telegramMessage.getTelegramChat() != null ? telegramMessage.getTelegramChat().getId() : null,
                telegramMessage.getTelegramUser() != null ? telegramMessage.getTelegramUser().getId() : null, telegramMessage.isDeleted(),
                telegramMessage.getReplyToMessageId() == 0 ? null : telegramMessage.getReplyToMessageId(),
                telegramMessage.getMessageThreadId() == 0 ? null : telegramMessage.getMessageThreadId());
        return id;
    }

    void updateAllDeletedMessages(List<TelegramMessage> telegramMessages) {
        telegramMessages.forEach(this::updateDeletedMessage);
    }

    void updateDeletedMessage(TelegramMessage telegramMessage) {
        final String query = "update telegram_message set deleted = ?, deletion_date = ? where id = ?";

        jdbcTemplate.update(query, telegramMessage.isDeleted(), telegramMessage.getDeletionDate(), telegramMessage.getId());
    }

    void updateMessageViews(long messageId, long chatId, int views) {
        try {
            final String query = "update telegram_message set views = ? where message_id = ? AND telegram_chat_id = ?";
            jdbcTemplate.update(query, views, messageId, chatId);
        } catch (Exception e) {
            LOGGER.error("Telegram message update failed", e);
        }
    }

    List<TelegramMessage> findByMessageIdsAndChatId(List<Long> messageIds, Long chatId) {
        final String query = "select * from telegram_message where message_id in (:messageIds) and telegram_chat_id=:chatId";

        return namedParameterJdbcTemplate.queryForObject(query,
                new MapSqlParameterSource().addValue("messageIds", messageIds)
                        .addValue("chatId", chatId), new TelegramMessageListRowMapper()
        );
    }
}

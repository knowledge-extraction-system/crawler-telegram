package kz.kaznu.telegram.client.models.rowmappers;

import kz.kaznu.telegram.client.models.TelegramChat;
import kz.kaznu.telegram.client.models.TelegramMessage;
import kz.kaznu.telegram.client.models.TelegramUser;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TelegramMessageListRowMapper implements RowMapper<List<TelegramMessage>> {

    @Override
    public List<TelegramMessage> mapRow(ResultSet rs, int rowNum) throws SQLException {
        List<TelegramMessage> telegramMessages = new ArrayList<>();

        telegramMessages.add(extractInfo(rs));
        while(rs.next()) {
            telegramMessages.add(extractInfo(rs));
        }

        return telegramMessages;
    }

    private TelegramMessage extractInfo(ResultSet rs) throws SQLException {
        TelegramMessage telegramMessage = new TelegramMessage();
        telegramMessage.setId(rs.getLong("id"));
        telegramMessage.setTelegramUser(new TelegramUser(rs.getLong("telegram_user_id")));
        telegramMessage.setTelegramChat(new TelegramChat(rs.getLong("telegram_chat_id")));
        telegramMessage.setMessage(rs.getString("message"));
        telegramMessage.setMessageId(rs.getLong("message_id"));

        return telegramMessage;
    }
}

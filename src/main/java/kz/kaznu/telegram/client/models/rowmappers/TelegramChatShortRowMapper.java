package kz.kaznu.telegram.client.models.rowmappers;

import kz.kaznu.telegram.client.models.TelegramChat;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TelegramChatShortRowMapper implements RowMapper<TelegramChat> {

    @Override
    public TelegramChat mapRow(ResultSet rs, int rowNum) throws SQLException {
        TelegramChat telegramChat = new TelegramChat();
        telegramChat.setId(rs.getLong("id"));
        telegramChat.setIsChannel(rs.getBoolean("is_channel"));
        telegramChat.setType(rs.getString("type"));
        telegramChat.setTitle(rs.getString("title"));
        telegramChat.setCountryId(rs.getLong("country_id"));

        return telegramChat;
    }
}

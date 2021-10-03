package kz.kaznu.telegram.client.models.rowmappers;

import kz.kaznu.telegram.client.models.TelegramChat;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TelegramChatListRowMapper implements RowMapper<List<TelegramChat>> {

    @Override
    public List<TelegramChat> mapRow(ResultSet rs, int rowNum) throws SQLException {
        List<TelegramChat> telegramMessages = new ArrayList<>();

        telegramMessages.add(extractInfo(rs));
        while(rs.next()) {
            telegramMessages.add(extractInfo(rs));
        }

        return telegramMessages;
    }

    private TelegramChat extractInfo(ResultSet rs) throws SQLException {
        TelegramChat telegramChat = new TelegramChat();
        telegramChat.setId(rs.getLong("id"));
        telegramChat.setIsChannel(rs.getBoolean("is_channel"));
        telegramChat.setType(rs.getString("type"));
        telegramChat.setTitle(rs.getString("title"));
        telegramChat.setCountryId(rs.getLong("country_id"));
        telegramChat.setDescription(rs.getString("description"));

        return telegramChat;
    }
}

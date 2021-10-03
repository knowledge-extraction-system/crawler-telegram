package kz.kaznu.telegram.client.models.rowmappers;

import kz.kaznu.telegram.client.models.TelegramUser;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TelegramUserShortRowMapper implements RowMapper<TelegramUser> {

    @Override
    public TelegramUser mapRow(ResultSet rs, int rowNum) throws SQLException {
        TelegramUser telegramUser = new TelegramUser();
        telegramUser.setId(rs.getLong("id"));
        telegramUser.setType(rs.getString("type"));
        telegramUser.setFirstName(rs.getString("first_name"));
        telegramUser.setLastName(rs.getString("last_name"));
        telegramUser.setCountryId(rs.getLong("country_id"));

        return telegramUser;
    }
}

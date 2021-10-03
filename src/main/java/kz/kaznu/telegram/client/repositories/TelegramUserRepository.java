package kz.kaznu.telegram.client.repositories;

import kz.kaznu.telegram.client.models.TelegramUser;
import kz.kaznu.telegram.client.models.rowmappers.TelegramUserShortRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Created by yerzhan on 10/10/19.
 */
@Repository
public class TelegramUserRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TelegramUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public TelegramUser findById(Long id) {
        try {
            final String query = "select * from telegram_user where id=?";
            return jdbcTemplate.queryForObject(query, new Object[]{id}, new TelegramUserShortRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public boolean checkIfUserExistsById(Long id) {
        final String query = "select exists(select 1 from telegram_user where id=?)";
        return jdbcTemplate.queryForObject(query, new Object[]{id}, Boolean.class);
    }

    public void insert(TelegramUser telegramUser) {
        String query = "insert into telegram_user (id, first_name, last_name, user_name, phone_number, lang_code, type, date) " +
                "values ( ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(query, telegramUser.getId(), telegramUser.getFirstName(), telegramUser.getLastName(),
                telegramUser.getUserName(), telegramUser.getPhoneNumber(), telegramUser.getLanguageCode(),
                telegramUser.getType(), telegramUser.getDate());
    }

}

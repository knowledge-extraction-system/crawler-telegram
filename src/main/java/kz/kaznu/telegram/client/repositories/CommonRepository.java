package kz.kaznu.telegram.client.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class CommonRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CommonRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    Long getNextValueForMessageSequence() {
        String query = "select nextval('telegram_message_id_seq')";

        return jdbcTemplate.queryForObject(
                query,
                Long.class);
    }

    Long getNextValueForTokenSequence() {
        String query = "select nextval('nlp_token_id_seq')";

        return jdbcTemplate.queryForObject(
                query,
                Long.class);
    }
}

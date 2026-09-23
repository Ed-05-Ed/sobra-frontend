package com.sobra.inventory.service;

import java.sql.PreparedStatement;
import java.util.UUID;

import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class OperationIdLock {

    private final JdbcTemplate jdbcTemplate;

    public OperationIdLock(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void acquire(UUID operationId) {
        long lockKey = operationId.getMostSignificantBits() ^ operationId.getLeastSignificantBits();
        jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
            try (PreparedStatement statement = connection.prepareStatement("SELECT pg_advisory_xact_lock(?)")) {
                statement.setLong(1, lockKey);
                statement.execute();
            }
            return null;
        });
    }
}

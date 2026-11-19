package com.example.bookmanagesystem.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OperationLogRepository {
    private final JdbcTemplate jdbcTemplate;

    public OperationLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void record(String actor,String operation,int bookId){
        jdbcTemplate.update(
                "INSERT INTO operation_log (actor, operation, book_id) VALUES (?, ?, ?)",
                actor, operation, bookId
        );
    }
}

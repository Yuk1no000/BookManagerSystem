SHOW DATABASES ;

create database if not exists book_manage_system
default character set utf8mb4;

USE book_manage_system;

create table if not exists books(
                                    id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY ,
                                    name VARCHAR(100) NOT NULL ,
    author VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    description VARCHAR(100) NOT NULL
    );

select * from books;

CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    actor VARCHAR(100) NOT NULL,
    operation VARCHAR(16) NOT NULL,
    book_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_operation_log_book_time (book_id, created_at),
    INDEX idx_operation_log_actor_time (actor, created_at)
);
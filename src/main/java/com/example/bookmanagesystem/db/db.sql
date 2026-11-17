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
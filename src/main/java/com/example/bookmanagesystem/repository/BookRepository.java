package com.example.bookmanagesystem.repository;


import com.example.bookmanagesystem.entity.Book;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BookRepository {
    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<Book> BOOK_ROW_MAPPER = (rs, rowNum)->
        new Book(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("author"),
                rs.getBigDecimal("price"),
                rs.getString("description")
        );
    public BookRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    //根据书ID获取书
    public Book findById(Integer id){
        String sql = "SELECT id,name,author,price,description from books where id = ?";
        List<Book> books = jdbcTemplate.query(sql,BOOK_ROW_MAPPER,id);
        if(books.isEmpty()){
            return null;
        }
        return books.get(0);
    }

    //获取表中所有数据
    public List<Book> getAllBooks(){
        String sql = "select * from books";
        List<Book> books =jdbcTemplate.query(sql,BOOK_ROW_MAPPER);
        if(books.isEmpty()){
            return null;
        }
        return books;
    }

    //添加
    public int addById(Book newBook){
        String sql = "INSERT INTO books(name,author,price,description)values(?,?,?,?)";
        return jdbcTemplate.update(
                sql,
                newBook.getBookName(),
                newBook.getAuthor(),
                newBook.getPrice(),
                newBook.getDescription()
        );
    }

    //更新
    public int updateBookById(Integer id,Book book){
        String sql = "update books set name = ?,author = ?,price = ?,description = ? where id = ?";
        return jdbcTemplate.update(
                sql,
                book.getBookName(),
                book.getAuthor(),
                book.getPrice(),
                book.getDescription(),
                id
        );
    }

    //删除
    public int deleteById(Integer id){
        String sql = "delete from books where id = ?";
        return jdbcTemplate.update(
                sql,
                id
        );
    }

}

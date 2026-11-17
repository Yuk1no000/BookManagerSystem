package com.example.bookmanagesystem.entity;

import java.math.BigDecimal;

public class Book {
    private Integer id;
    private String bookName;
    private String author;
    private BigDecimal price;
    private String description;

    public Book(){}

    public Book(Integer id,String bookName,String author,BigDecimal price,String description)
    {
        this.id = id;
        this.bookName=bookName;
        this.author=author;
        this.price=price;
        this.description=description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", bookName='" + bookName + '\'' +
                ", author='" + author + '\'' +
                ", price=" + price +
                ", description='" + description + '\'' +
                '}';
    }
}

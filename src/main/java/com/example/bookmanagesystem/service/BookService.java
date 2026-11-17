package com.example.bookmanagesystem.service;

import com.example.bookmanagesystem.entity.Book;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookService {
    private final List<Book> books = new ArrayList<>();

    private Integer nextId = 1;

    public synchronized Book add(Book book)
    {
        book.setId(nextId++);
        books.add(book);
        return book;
    }

    public synchronized List<Book> getAllBooks()
    {
        return new ArrayList<>(books);
    }

    public synchronized Book getBookById(Integer id){
        int index = findIndexById(id);
        if(index == -1){
            return null;
        }
        return books.get(index);
    }

    public synchronized boolean deleteBookById(Integer id){
        int index = findIndexById(id);
        if(index==-1){
            return false;
        }
        books.remove(index);
        return true;
    }

    public synchronized Book updateBookById(Integer id,Book newBook){
        int index = findIndexById(id);
        if(index == -1){
            return null;
        }
        Book updatedBook = new Book(
                id,
                newBook.getBookName(),
                newBook.getAuthor(),
                newBook.getPrice(),
                newBook.getDescription()
        );
        books.set(index,updatedBook);
        return updatedBook;
    }


    public Integer findIndexById(Integer id){
        for(int i=0;i<books.size();i++){
            Book book = books.get(i);
            if(id.equals(book.getId())){
                return i;
            }
        }
        return -1;
    }

}

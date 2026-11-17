package com.example.bookmanagesystem.service;

import com.example.bookmanagesystem.entity.Book;
import com.example.bookmanagesystem.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository){
        this.bookRepository = bookRepository;
    }

    //新增
    @Transactional
    public Book add(Book book)
    {
        int id = bookRepository.addById(book);
        return bookRepository.findById(id);
    }

    //获取所有书籍
    public List<Book> getAllBooks()
    {
        return bookRepository.getAllBooks();
    }

    //根据ID获取书籍
    public Book getBookById(Integer id){
        return bookRepository.findById(id);
    }

    //删除
    public boolean deleteBookById(Integer id){
        int rows = bookRepository.deleteById(id);
        if(rows == 0){
            return false;
        }
        return true;
    }

    //更新
    @Transactional
    public Book updateBookById(Integer id,Book newBook){
        int rows = bookRepository.updateBookById(id,newBook);
        if(rows == 0) {
            return null;
        }
        return bookRepository.findById(id);
    }


//    public Integer findIndexById(Integer id){
//        for(int i=0;i<books.size();i++){
//            Book book = books.get(i);
//            if(id.equals(book.getId())){
//                return i;
//            }
//        }
//        return -1;
//    }

}

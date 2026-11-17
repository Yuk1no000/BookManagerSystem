package com.example.bookmanagesystem.controller;


import com.example.bookmanagesystem.dto.BookRequest;
import com.example.bookmanagesystem.entity.Book;
import com.example.bookmanagesystem.repository.BookRepository;
import com.example.bookmanagesystem.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService)
    {
        this.bookService = bookService;
    }

    @GetMapping
    public List<Book> getAllBooks(){
        return bookService.getAllBooks();
    }

    //添加
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book add(
            @Valid
            @RequestBody BookRequest request
    ){
        return bookService.add(toBook(request));
    }

    public ResponseEntity<Book> findById(@PathVariable("id")Integer id){
        Book book = bookService.getBookById(id);
        if(book == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(book);
    }

    //删除
    @DeleteMapping("/{id}")
    public ResponseEntity<Book> deleteById(@PathVariable("id")Integer id){
        boolean flag = bookService.deleteBookById(id);
        if(!flag){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    //更新
    @PutMapping("/{id}")
    public ResponseEntity<Book> update(
            @PathVariable("id")Integer id,
            @Valid
            @RequestBody BookRequest request
    )
    {
        Book updatedBook = bookService.updateBookById(id,toBook(request));
        if(updatedBook == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedBook);
    }

    //通过ID查询
    @GetMapping("/{id}")
    public ResponseEntity<Book> getById(@PathVariable("id")Integer id)
    {
        Book book = bookService.getBookById(id);
        if(book == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(book);
    }

    private Book toBook(BookRequest request){
        return new Book(null,request.name(), request.author(), request.price(), request.description());
    }

}

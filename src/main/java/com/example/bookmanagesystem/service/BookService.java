package com.example.bookmanagesystem.service;

import com.example.bookmanagesystem.entity.Book;
import com.example.bookmanagesystem.repository.BookRepository;
import com.example.bookmanagesystem.repository.OperationLogRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookService {
    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    private final BookRepository bookRepository;
    private final OperationLogRepository operationLogRepository;

    public BookService(BookRepository bookRepository, OperationLogRepository operationLogRepository) {
        this.bookRepository = bookRepository;
        this.operationLogRepository = operationLogRepository;
    }

    //新增
    @Transactional
    public Book add(Book book,String actor)
    {
        int id = bookRepository.addById(book);
        // 在新增成功、拿到生成的 ID 后：
        operationLogRepository.record(actor, "新增图书", id, MDC.get("requestId"));
        return bookRepository.findById(id);
    }

    //获取所有书籍
    public List<Book> getAllBooks() {
        return bookRepository.getAllBooks();
    }

    //根据ID获取书籍
    public Book getBookById(Integer id){
        return bookRepository.findById(id);
    }

    //删除
    @Transactional
    public boolean deleteBookById(Integer id,String actor){
        int rows = bookRepository.deleteById(id);
        if(rows == 0){
            // 删除时 rows == 0：
            log.warn("删除图书失败，图书不存在，bookId={}", id);
            return false;
        }
        // 删除成功后，记录操作日志：
        operationLogRepository.record(actor, "删除图书", id, MDC.get("requestId"));
        return true;
    }

    //更新
    @Transactional
    public Book updateBookById(Integer id,Book newBook,String actor){
        int rows = bookRepository.updateBookById(id,newBook);
        if(rows == 0) {
            return null;
        }
        // 更新成功后，记录操作日志：
        operationLogRepository.record(actor, "更新图书", id,MDC.get("requestId"));
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

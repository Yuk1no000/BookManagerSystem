package com.example.bookmanagesystem.repository;

import com.example.bookmanagesystem.entity.Book;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.RowSetProvider;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookRepositoryTests {

    @Test
    void addReturnsGeneratedIdInsteadOfAffectedRowCount() throws SQLException {
        BookRepository repository = repositoryWithGeneratedId(42);
        Book book = new Book(null,"New book","Author",new BigDecimal("39.90"),"Description");

        assertEquals(42,repository.addById(book));
    }

    @Test
    void addFailsWhenNoGeneratedIdIsReturned() throws SQLException {
        BookRepository repository = repositoryWithGeneratedId(null);
        Book book = new Book(null,"New book","Author",new BigDecimal("39.90"),"Description");

        assertThrows(IllegalStateException.class,() -> repository.addById(book));
    }

    private BookRepository repositoryWithGeneratedId(Integer id) throws SQLException {
        RowSetMetaDataImpl metadata = new RowSetMetaDataImpl();
        metadata.setColumnCount(1);
        metadata.setColumnName(1,"GENERATED_KEY");
        metadata.setColumnLabel(1,"GENERATED_KEY");
        metadata.setColumnType(1,Types.INTEGER);
        CachedRowSet generatedKeys = RowSetProvider.newFactory().createCachedRowSet();
        generatedKeys.setMetaData(metadata);
        if(id != null){
            generatedKeys.moveToInsertRow();
            generatedKeys.updateInt(1,id);
            generatedKeys.insertRow();
            generatedKeys.moveToCurrentRow();
            generatedKeys.beforeFirst();
        }

        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(connection.prepareStatement(anyString(),eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);
        when(statement.getGeneratedKeys()).thenReturn(generatedKeys);
        return new BookRepository(new JdbcTemplate(dataSource));
    }
}

package service;

import model.Book;
import model.Category;
import model.Isbn;
import persistence.BookDAO;

import java.util.List;
import java.util.Objects;

// Regras do caso de uso. Recebe o DAO pelo construtor (não sabe se é MySQL Workbench ou memória).
public class BookService {
    private final BookDAO dao;

    public BookService(BookDAO dao) {
        this.dao = Objects.requireNonNull(dao);
    }

    public void register(Book book) {
        if (!dao.add(book)) {
            throw new BookException("A book with ISBN " + book.getIsbn() + " already exists");
        }
    }

    public void rent(Isbn isbn) {
        if (find(isbn).isRented()) throw new BookException("This book is already in your library");
        dao.setRented(isbn, true);
    }

    public void giveBack(Isbn isbn) {
        if (!find(isbn).isRented()) throw new BookException("This book is not in your library");
        dao.setRented(isbn, false);
    }

    public void delete(Isbn isbn) {
        if (!dao.remove(isbn)) throw new BookException("This book no longer exists");
    }

    public List<Book> search(String title, Category category) { return dao.search(title, category); }
    public List<Book> recent(int limit) { return dao.findRecent(limit); }
    public List<Book> suggestions(int limit) { return dao.findRandom(limit); }
    public List<Book> rented() { return dao.findRented(); }

    public int countBooks() { return dao.countBooks(); }
    public int countRented() { return dao.countRented(); }
    public int countAuthors() { return dao.countAuthors(); }
    public int countByCategory(Category category) { return dao.countByCategory(category); }

    private Book find(Isbn isbn) {
        return dao.findByIsbn(isbn).orElseThrow(() -> new BookException("This book no longer exists"));
    }
}
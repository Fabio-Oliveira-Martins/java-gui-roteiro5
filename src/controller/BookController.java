package controller;

import dto.BookInput;
import dto.BookView;
import dto.LibraryStats;
import model.Book;
import model.Category;
import model.Isbn;
import persistence.DataAccessException;
import service.BookException;
import service.BookService;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class BookController {
    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    // ===== Consultas (falha de dados sobe como DataAccessException, de mensagem segura) =====

    public List<BookView> all() { return views(service.search(null, null)); }
    public List<BookView> search(String title, Category category) { return views(service.search(title, category)); }
    public List<BookView> recent(int limit) { return views(service.recent(limit)); }
    public List<BookView> suggestions(int limit) { return views(service.suggestions(limit)); }
    public List<BookView> rented() { return views(service.rented()); }

    public LibraryStats stats() {
        Map<Category, Integer> byCategory = new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            byCategory.put(category, service.countByCategory(category));
        }
        return new LibraryStats(service.countAuthors(), service.countBooks(), service.countRented(), byCategory);
    }

    // ===== Comandos: conversão/validação -> serviço -> resposta para a view =====

    public Outcome register(BookInput input) {
        return run("Your form was successfully submitted", () -> service.register(BookMapper.toDomain(input)));
    }

    public Outcome rent(String isbn) {
        return run("Book added to your library", () -> service.rent(Isbn.of(isbn)));
    }

    public Outcome giveBack(String isbn) {
        return run("Book removed from your library", () -> service.giveBack(Isbn.of(isbn)));
    }

    public Outcome delete(String isbn) {
        return run("Book deleted successfully", () -> service.delete(Isbn.of(isbn)));
    }

    // Regra violada, dado inválido ou falha de dados viram uma resposta previsível
    private Outcome run(String successMessage, Runnable command) {
        try {
            command.run();
            return Outcome.ok(successMessage);
        } catch (BookException | IllegalArgumentException | DataAccessException e) {
            return Outcome.fail(e.getMessage());
        }
    }

    private static List<BookView> views(List<Book> books) {
        return books.stream().map(BookMapper::toView).toList();
    }
}
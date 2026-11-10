package controller;

import dto.BookInput;
import dto.BookView;
import model.Author;
import model.Book;
import model.Category;
import model.Isbn;

final class BookMapper {
    private BookMapper() {}

    // BookInput (texto da tela) -> Book (domínio): aqui as regras de Isbn e Book são aplicadas
    static Book toDomain(BookInput in) {
        return new Book(in.title(), new Author(in.author()), in.publisher(),
                Category.fromActionCommand(in.category()), Isbn.of(in.isbn()), in.description());
    }

    // Book (domínio) -> BookView (dados prontos para exibir)
    static BookView toView(Book b) {
        return new BookView(b.getTitle(), b.getAuthor().getName(), b.getPublisher(), b.getCategory(),
                b.getIsbn().value(), b.getDescription(), b.isRented());
    }
}
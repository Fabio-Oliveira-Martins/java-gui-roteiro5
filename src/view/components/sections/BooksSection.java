package view.components.sections;

import controller.BookController;
import dto.BookView;
import view.components.BookCard;
import view.components.BookGrid;
import view.components.MutedLabel;

import java.util.List;

public class BooksSection extends Section {
    private static final String EMPTY_MESSAGE = "No books found.";

    private final BookController controller;
    private final Runnable onChange;

    public BooksSection(String title, List<BookView> books, BookController controller, Runnable onChange) {
        super(title);
        this.controller = controller;
        this.onChange = onChange;
        showBooks(books);
    }

    // Troca a lista exibida (usado também pelo filtro da Explore)
    public final void showBooks(List<BookView> books) {
        if (books.isEmpty()) {
            setContent(new MutedLabel(EMPTY_MESSAGE));
            return;
        }

        BookGrid grid = new BookGrid(BookCard.Size.SMALL);
        for (BookView book : books) {
            grid.add(new BookCard(book, BookCard.Size.SMALL, controller, onChange));
        }
        setContent(grid);
    }
}
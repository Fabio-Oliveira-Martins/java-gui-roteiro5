package view.pages;

import controller.BookController;
import view.components.sections.Section;
import view.components.sections.BooksSection;

import java.util.List;

public class MyLibraryPage extends Page {
    private final BookController controller;

    public MyLibraryPage(BookController controller) {
        this.controller = controller;
    }

    @Override
    protected List<Section> createSections() {
        return List.of(new BooksSection("My Library", controller.rented(), controller, this::render));
    }
}
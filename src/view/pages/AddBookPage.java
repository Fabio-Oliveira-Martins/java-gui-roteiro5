package view.pages;

import controller.BookController;
import view.components.BookForm;
import view.components.sections.Section;

import java.util.List;

public class AddBookPage extends Page {
    private final BookController controller;

    public AddBookPage(BookController controller) {
        this.controller = controller;
    }

    @Override
    protected List<Section> createSections() {
        Section section = new Section("Add Book");
        section.setContent(new BookForm(controller));
        return List.of(section);
    }
}
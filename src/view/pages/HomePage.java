package view.pages;

import controller.BookController;
import view.components.sections.Section;
import view.components.sections.BookRowSection;
import view.components.sections.CategoriesSection;
import view.components.sections.DashboardSection;

import java.util.List;

public class HomePage extends Page {
    private final BookController controller;

    public HomePage(BookController controller) {
        this.controller = controller;
    }

    @Override
    protected List<Section> createSections() {
        return List.of(
                new DashboardSection(controller),
                BookRowSection.recentlyAdded(controller, this::render),
                BookRowSection.suggestion(controller, this::render),
                new CategoriesSection(controller)
        );
    }
}
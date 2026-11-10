package view.components.sections;

import controller.BookController;
import dto.LibraryStats;
import model.Category;
import view.components.AdaptiveGrid;
import view.components.CategoryCard;

public class CategoriesSection extends Section {

    public CategoriesSection(BookController controller) {
        super("Categories Available");

        LibraryStats stats = controller.stats();

        AdaptiveGrid grid = new AdaptiveGrid(200, 2, 3, 12, true);
        for (Category category : Category.values()) {
            grid.add(new CategoryCard(category, stats.byCategory().get(category)));
        }
        setContent(grid);
    }
}
package view.components.sections;

import controller.BookController;
import dto.BookView;
import view.components.BookCard;
import view.components.MutedLabel;
import view.components.componentsUtils.CustomScrollBarUI;
import view.themes.Theme;
import view.themes.ThemeManager;
import view.themes.Themeable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class BookRowSection extends Section {

    private BookRowSection(String title, List<BookView> books, BookCard.Size size,
                           BookController controller, Runnable onChange) {
        super(title);
        setContent(books.isEmpty()
                ? new MutedLabel("No books available yet.")
                : new BookRow(books, size, controller, onChange));
    }

    // Até 5 livros mais recentes
    public static BookRowSection recentlyAdded(BookController controller, Runnable onChange) {
        return new BookRowSection("Recently Added", controller.recent(5), BookCard.Size.LARGE, controller, onChange);
    }

    // Até 6 livros sorteados
    public static BookRowSection suggestion(BookController controller, Runnable onChange) {
        return new BookRowSection("Suggestion", controller.suggestions(6), BookCard.Size.SMALL, controller, onChange);
    }
}

class BookRow extends JScrollPane implements Themeable {
    private static final int GAP = 16;
    private static final int BAR_HEIGHT = 10;

    private final BookCard.Size size;
    private final int count;
    private int lastHeight = -1;

    public BookRow(List<BookView> books, BookCard.Size size, BookController controller, Runnable onChange) {
        super(new Row(books, size, controller, onChange),
                VERTICAL_SCROLLBAR_NEVER, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.size = size;
        this.count = books.size();

        setBorder(null);
        setOpaque(false);
        getViewport().setOpaque(false);
        getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        getHorizontalScrollBar().setUnitIncrement(32);

        applyTheme(ThemeManager.current());
    }

    private int cardWidth() {
        int width = Math.max(getWidth(), size.minWidth);
        return Math.max(size.minWidth, (width - (count - 1) * GAP) / count);
    }

    // Altura = card + espaço reservado para a barra. A largura vem do layout da seção.
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(0, BookCard.heightFor(size, cardWidth()) + BAR_HEIGHT);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize(); // evita o mínimo de ~4px do JScrollPane
    }

    @Override
    public void doLayout() {
        super.doLayout();
        int preferred = getPreferredSize().height; // a altura depende da largura
        if (preferred != lastHeight) {
            lastHeight = preferred;
            SwingUtilities.invokeLater(this::revalidate);
        }
    }

    @Override
    public void applyTheme(Theme theme) {
        Color c = theme.text;
        getHorizontalScrollBar().setUI(new CustomScrollBarUI(
                new Color(c.getRed(), c.getGreen(), c.getBlue(), 70),
                new Color(c.getRed(), c.getGreen(), c.getBlue(), 120),
                null, BAR_HEIGHT, 5));
        getHorizontalScrollBar().setOpaque(false);
    }

    private static class Row extends JPanel implements Scrollable {
        private final BookCard.Size size;

        Row(List<BookView> books, BookCard.Size size, BookController controller, Runnable onChange) {
            this.size = size;
            setOpaque(false);
            setLayout(null);
            for (BookView book : books) {
                add(new BookCard(book, size, controller, onChange));
            }
        }

        @Override
        public Dimension getPreferredSize() {
            int n = getComponentCount();
            return new Dimension(
                    n * size.minWidth + Math.max(0, n - 1) * GAP,    // largura mínima de todos
                    BookCard.heightFor(size, size.minWidth));
        }

        @Override
        public void doLayout() {
            int n = getComponentCount();
            if (n == 0) return;

            int width = Math.max(size.minWidth, (getWidth() - (n - 1) * GAP) / n);
            int height = BookCard.heightFor(size, width);

            for (int i = 0; i < n; i++) {
                getComponent(i).setBounds(i * (width + GAP), 0, width, height);
            }
        }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 32; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return r.width; }

        // Preenche o viewport quando há espaço; senão mantém a largura mínima e rola
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return getParent() instanceof JViewport v && v.getWidth() >= getPreferredSize().width;
        }

        // Altura acompanha o viewport
        @Override
        public boolean getScrollableTracksViewportHeight() {
            return true;
        }
    }
}
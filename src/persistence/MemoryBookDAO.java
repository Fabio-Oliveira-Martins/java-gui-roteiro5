package persistence;

import model.Book;
import model.Category;
import model.Isbn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.UnaryOperator;

public class MemoryBookDAO implements BookDAO {
    private final CopyOnWriteArrayList<Book> books = new CopyOnWriteArrayList<>();

    @Override
    public List<Book> search(String title, Category category) {
        String term = title == null ? "" : title.trim().toLowerCase();
        return books.stream()
                .filter(b -> term.isEmpty() || b.getTitle().toLowerCase().contains(term))
                .filter(b -> category == null || b.getCategory() == category)
                .toList();
    }

    @Override
    public List<Book> findRecent(int limit) {
        List<Book> last = new ArrayList<>(books);
        Collections.reverse(last);
        return last.subList(0, Math.min(limit, last.size()));
    }

    @Override
    public List<Book> findRandom(int limit) {
        List<Book> copy = new ArrayList<>(books);
        Collections.shuffle(copy);
        return copy.subList(0, Math.min(limit, copy.size()));
    }

    @Override
    public List<Book> findRented() {
        return books.stream().filter(Book::isRented).toList();
    }

    @Override
    public Optional<Book> findByIsbn(Isbn isbn) {
        return books.stream().filter(b -> b.getIsbn().equals(isbn)).findFirst();
    }

    @Override
    public boolean add(Book book) {
        return books.addIfAbsent(book); // equals compara o ISBN
    }

    // Mesma semântica do JDBC: o autor e o estado "alugado" não mudam no update
    @Override
    public synchronized boolean update(Book book) {
        return replace(book.getIsbn(), old -> new Book(book.getTitle(), old.getAuthor(), book.getPublisher(),
                book.getCategory(), old.getIsbn(), book.getDescription(), old.isRented()));
    }

    @Override
    public synchronized boolean remove(Isbn isbn) {
        return books.removeIf(b -> b.getIsbn().equals(isbn));
    }

    @Override
    public synchronized boolean setRented(Isbn isbn, boolean rented) {
        return replace(isbn, old -> old.withRented(rented));
    }

    private boolean replace(Isbn isbn, UnaryOperator<Book> change) {
        for (int i = 0; i < books.size(); i++) {
            Book old = books.get(i);
            if (old.getIsbn().equals(isbn)) {
                books.set(i, change.apply(old));
                return true;
            }
        }
        return false;
    }

    @Override
    public int countBooks() {
        return books.size();
    }

    @Override
    public int countRented() {
        return (int) books.stream().filter(Book::isRented).count();
    }

    @Override
    public int countAuthors() {
        return (int) books.stream().map(Book::getAuthor).distinct().count();
    }

    @Override
    public int countByCategory(Category category) {
        return (int) books.stream().filter(b -> b.getCategory() == category).count();
    }
}
package persistence.jdbc;

import model.Author;
import model.Book;
import model.Category;
import model.Isbn;
import persistence.BookDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcBookDAO implements BookDAO {
    private static final int DUPLICATE_ENTRY = 1062;
    private static final String CATEGORY_ID = "(SELECT id FROM categories WHERE name = ?)";
    private static final String SELECT_BOOKS = """
            SELECT b.isbn, b.title, a.name AS author, b.publisher,
                   c.name AS category, b.description, b.rented
              FROM books b
              JOIN authors a ON a.id = b.author_id
              JOIN categories c ON c.id = b.category_id
            """;

    private final Database database;

    public JdbcBookDAO(Database database) {
        this.database = database;
    }

    // ===== Consultas =====

    @Override
    public List<Book> search(String title, Category category) {
        StringBuilder sql = new StringBuilder(SELECT_BOOKS).append(" WHERE 1 = 1");
        List<Object> params = new ArrayList<>();

        if (title != null && !title.isBlank()) {
            sql.append(" AND b.title LIKE ?");
            params.add("%" + escapeLike(title.trim()) + "%");
        }
        if (category != null) {
            sql.append(" AND c.name = ?");
            params.add(category.name());
        }
        sql.append(" ORDER BY b.id");
        return query(sql.toString(), params.toArray());
    }

    @Override
    public List<Book> findRecent(int limit) {
        return query(SELECT_BOOKS + " ORDER BY b.id DESC LIMIT ?", limit);
    }

    @Override
    public List<Book> findRandom(int limit) {
        return query(SELECT_BOOKS + " ORDER BY RAND() LIMIT ?", limit);
    }

    @Override
    public List<Book> findRented() {
        return query(SELECT_BOOKS + " WHERE b.rented = TRUE ORDER BY b.id");
    }

    @Override
    public Optional<Book> findByIsbn(Isbn isbn) {
        return query(SELECT_BOOKS + " WHERE b.isbn = ?", isbn.value()).stream().findFirst();
    }

    @Override
    public int countBooks() {
        return count("SELECT COUNT(*) FROM books");
    }

    @Override
    public int countRented() {
        return count("SELECT COUNT(*) FROM books WHERE rented = TRUE");
    }

    @Override
    public int countAuthors() {
        return count("SELECT COUNT(DISTINCT author_id) FROM books");
    }

    @Override
    public int countByCategory(Category category) {
        return count("SELECT COUNT(*) FROM books b JOIN categories c ON c.id = b.category_id WHERE c.name = ?",
                category.name());
    }

    // ===== Alterações =====

    // Transação: autor (se novo) e livro, ou os dois, ou nenhum
    @Override
    public boolean add(Book book) {
        try (Connection connection = database.open()) {
            try {
                connection.setAutoCommit(false);
                long authorId = findOrCreateAuthor(connection, book.getAuthor());
                insertBook(connection, book, authorId);
                connection.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                rollback(connection);
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            if (e.getErrorCode() == DUPLICATE_ENTRY) return false;
            throw Database.failure("Could not save the book", e);
        } catch (SQLException e) {
            throw Database.failure("Could not save the book", e);
        }
    }

    @Override
    public boolean update(Book book) {
        return execute("Could not update the book",
                "UPDATE books SET title = ?, publisher = ?, description = ?, category_id = " + CATEGORY_ID
                        + " WHERE isbn = ?",
                book.getTitle(), book.getPublisher(), book.getDescription(),
                book.getCategory().name(), book.getIsbn().value()) == 1;
    }

    @Override
    public boolean remove(Isbn isbn) {
        return execute("Could not delete the book", "DELETE FROM books WHERE isbn = ?", isbn.value()) == 1;
    }

    @Override
    public boolean setRented(Isbn isbn, boolean rented) {
        return execute("Could not update the book",
                "UPDATE books SET rented = ? WHERE isbn = ?", rented, isbn.value()) == 1;
    }

    // ===== JDBC =====

    private List<Book> query(String sql, Object... params) {
        try (Connection connection = database.open();
             PreparedStatement statement = prepare(connection, sql, params);
             ResultSet rows = statement.executeQuery()) {
            List<Book> books = new ArrayList<>();
            while (rows.next()) {
                books.add(toBook(rows));
            }
            return books;
        } catch (SQLException e) {
            throw Database.failure("Could not load books", e);
        }
    }

    private int count(String sql, Object... params) {
        try (Connection connection = database.open();
             PreparedStatement statement = prepare(connection, sql, params);
             ResultSet rows = statement.executeQuery()) {
            return rows.next() ? rows.getInt(1) : 0;
        } catch (SQLException e) {
            throw Database.failure("Could not read book data", e);
        }
    }

    private int execute(String errorMessage, String sql, Object... params) {
        try (Connection connection = database.open();
             PreparedStatement statement = prepare(connection, sql, params)) {
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw Database.failure(errorMessage, e);
        }
    }

    private static PreparedStatement prepare(Connection connection, String sql, Object... params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
        return statement;
    }

    // Linha do banco -> entidade
    private static Book toBook(ResultSet row) throws SQLException {
        return new Book(
                row.getString("title"),
                new Author(row.getString("author")),
                row.getString("publisher"),
                Category.valueOf(row.getString("category")),
                Isbn.of(row.getString("isbn")),
                row.getString("description"),
                row.getBoolean("rented"));
    }

    private long findOrCreateAuthor(Connection connection, Author author) throws SQLException {
        try (PreparedStatement find = prepare(connection, "SELECT id FROM authors WHERE name = ?", author.getName());
             ResultSet rows = find.executeQuery()) {
            if (rows.next()) return rows.getLong(1);
        }

        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO authors (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            insert.setString(1, author.getName());
            requireOneRow(insert.executeUpdate());

            try (ResultSet keys = insert.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Generated key not returned");
                return keys.getLong(1);
            }
        }
    }

    private void insertBook(Connection connection, Book book, long authorId) throws SQLException {
        try (PreparedStatement insert = prepare(connection,
                "INSERT INTO books (isbn, title, author_id, publisher, category_id, description) "
                        + "VALUES (?, ?, ?, ?, " + CATEGORY_ID + ", ?)",
                book.getIsbn().value(), book.getTitle(), authorId, book.getPublisher(),
                book.getCategory().name(), book.getDescription())) {
            requireOneRow(insert.executeUpdate());
        }
    }

    private static void requireOneRow(int affected) throws SQLException {
        if (affected != 1) throw new SQLException("Expected 1 affected row but got " + affected);
    }

    private static void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            System.err.printf("[DB] rollback failed (SQLState=%s)%n", e.getSQLState());
        }
    }

    private static String escapeLike(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
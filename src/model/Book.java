package model;

import java.util.Objects;

public class Book {
    public static final int MAX_TEXT = 120;
    public static final int MAX_DESCRIPTION = 1200;

    private final String title;
    private final Author author;
    private final String publisher;
    private final Category category;
    private final Isbn isbn;
    private final String description; // opcional: nunca é null, no máximo ""
    private final boolean rented;

    public Book(String title, Author author, String publisher, Category category, Isbn isbn, String description) {
        this(title, author, publisher, category, isbn, description, false);
    }

    public Book(
            String title, Author author, String publisher, Category category,
            Isbn isbn, String description, boolean rented)
    {
        this.title = requireText(title, "Title", MAX_TEXT);
        this.author = Objects.requireNonNull(author, "Author is required");
        this.publisher = requireText(publisher, "Publisher", MAX_TEXT);
        this.category = Objects.requireNonNull(category, "Category is required");
        this.isbn = Objects.requireNonNull(isbn, "ISBN is required");
        this.description = description == null ? "" : description.trim();
        if (this.description.length() > MAX_DESCRIPTION) {
            throw new IllegalArgumentException("Description must have at most " + MAX_DESCRIPTION + " characters");
        }
        this.rented = rented;
    }

    private static String requireText(String value, String field, int max) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        String text = value.trim();
        if (text.length() > max) {
            throw new IllegalArgumentException(field + " must have at most " + max + " characters");
        }
        return text;
    }

    public Book withRented(boolean rented) {
        return new Book(title, author, publisher, category, isbn, description, rented);
    }

    public String getTitle() { return title; }
    public Author getAuthor() { return author; }
    public String getPublisher() { return publisher; }
    public Category getCategory() { return category; }
    public Isbn getIsbn() { return isbn; }
    public String getDescription() { return description; }
    public boolean isRented() { return rented; }

    // O ISBN identifica o livro
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Book other && isbn.equals(other.isbn));
    }

    @Override
    public int hashCode() {
        return isbn.hashCode();
    }

    @Override
    public String toString() {
        return "\"" + title + "\" - " + author + " | " + publisher + " | " + category
                + " | ISBN " + isbn + (description.isEmpty() ? "" : " | " + description);
    }
}
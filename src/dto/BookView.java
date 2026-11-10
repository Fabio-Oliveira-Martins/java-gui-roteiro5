package dto;

import model.Category;

// Dados prontos para exibir na tela
public record BookView(String title, String author, String publisher, Category category,
                       String isbn, String description, boolean rented) {}
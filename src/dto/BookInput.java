package dto;

// Dados crus da tela (texto). Sem validação e sem comportamento.
public record BookInput(String title, String author, String publisher,
                        String category, String isbn, String description) {}
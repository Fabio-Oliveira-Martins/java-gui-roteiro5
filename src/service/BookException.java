package service;

// Violação de regra do caso de uso. A mensagem é própria para exibir.
public class BookException extends RuntimeException {
    public BookException(String message) {
        super(message);
    }
}
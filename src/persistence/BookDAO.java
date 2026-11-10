package persistence;

import model.Book;
import model.Category;
import model.Isbn;

import java.util.List;
import java.util.Optional;

// Contrato de acesso a dados. Quem usa não sabe se por trás está o MySQL Workbench ou a memória.
public interface BookDAO {
    List<Book> search(String title, Category category); // título contém / categoria; nulos = sem filtro
    List<Book> findRecent(int limit);
    List<Book> findRandom(int limit);
    List<Book> findRented();
    Optional<Book> findByIsbn(Isbn isbn);

    boolean add(Book book);                        // false: ISBN já cadastrado
    boolean update(Book book);                     // false: ISBN inexistente
    boolean remove(Isbn isbn);                     // false: ISBN inexistente
    boolean setRented(Isbn isbn, boolean rented);  // false: ISBN inexistente

    int countBooks();
    int countRented();
    int countAuthors();
    int countByCategory(Category category);
}
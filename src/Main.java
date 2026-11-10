import controller.AuthController;
import controller.BookController;
import model.Author;
import model.Book;
import model.Category;
import model.InMemoryUserRepository;
import model.Isbn;
import model.User;
import model.UserRepository;
import persistence.BookDAO;
import persistence.MemoryBookDAO;
import persistence.jdbc.Database;
import persistence.jdbc.DatabaseConfig;
import persistence.jdbc.JdbcBookDAO;
import service.BookService;
import view.LoginScreen;
import view.MainFrame;
import view.components.Header;
import view.components.NavigationBar;
import view.components.customDialogs.statusDialog.AlertDialog;
import view.pages.AddBookPage;
import view.pages.ExplorePage;
import view.pages.HomePage;
import view.pages.MyLibraryPage;
import view.pages.SettingsPage;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BookDAO bookDao;
            try {
                bookDao = createBookDao();
            } catch (RuntimeException e) { // banco configurado, mas sem conexão: a mensagem não tem senha
                JOptionPane.showMessageDialog(null, e.getMessage(), "Digital Library", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // DAO -> serviço -> controlador (injeção pelo construtor)
            BookController bookController = new BookController(new BookService(bookDao));

            UserRepository userRepository = new InMemoryUserRepository();
            userRepository.save(new User("Guest123", "Guest@123")); // usuário pré-definido
            AuthController auth = new AuthController(userRepository);

            Header header = new Header("Digital Library", "");
            auth.setOnUserChanged(user -> header.setUserName(user.getUsername()));

            NavigationBar navigationBar = new NavigationBar();
            MainFrame frame = new MainFrame(header, navigationBar, new LoginScreen(auth));

            // Mantenha os nomes de ícone que você já definiu
            navigationBar.addItem("Home", "home.png", "home-light.png",
                    () -> new HomePage(bookController));
            navigationBar.addItem("Explore", "compass.png", "compass-light.png",
                    () -> new ExplorePage(bookController));
            navigationBar.addItem("My Library", "bookmark.png", "bookmark-light.png",
                    () -> new MyLibraryPage(bookController));
            navigationBar.addItem("Add Book", "plus.png", "plus-light.png",
                    () -> new AddBookPage(bookController));
            navigationBar.addSeparator();
            navigationBar.addItem("Settings", "settings.png", "settings-light.png",
                    () -> new SettingsPage(auth));
            navigationBar.addAction("Logout", "logout.png", "logout-light.png",
                    () -> AlertDialog.confirm(
                            frame,
                            "Log Out",
                            "Are you sure that you want to log out?",
                            () -> {
                                auth.logout();
                                frame.showLogin();
                            }));

            frame.showLogin();
            frame.setVisible(true);
        });
    }

    // Composição: a troca de implementação acontece aqui e em nenhum outro lugar.
    // Com BOOKS_DB_URL definida usa o banco (MySQL Workbench); sem ela, usa a memória.
    private static BookDAO createBookDao() {
        if (System.getenv("BOOKS_DB_URL") == null) {
            System.err.println("[Main] BOOKS_DB_URL not set: using in-memory books");
            return demoDao();
        }
        Database database = new Database(DatabaseConfig.fromEnvironment());
        database.check();
        return new JdbcBookDAO(database);
    }

    // Livros de demonstração, só para a versão em memória
    private static MemoryBookDAO demoDao() {
        MemoryBookDAO dao = new MemoryBookDAO();
        for (int i = 1; i <= 12; i++) {
            dao.add(new Book(
                    "Book Title " + i,
                    new Author("Author " + (i % 5 + 1)),
                    "Publisher " + i,
                    Category.values()[i % Category.values().length],
                    Isbn.of(String.valueOf(9780000000000L + i)),
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. ".repeat(i % 3 + 1)));
        }
        return dao;
    }
}
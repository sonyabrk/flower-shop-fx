package flowershop;

import flowershop.exception.DatabaseConnectionException;
import flowershop.util.DatabaseManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

// Stage — окно, Scene — содержимое окна, Node — любой элемент внутри сцены
public class FlowerShopApp extends Application {

    private static final Logger log = LoggerFactory.getLogger(FlowerShopApp.class);

    @Override
    public void start(Stage stage) throws IOException {
        try {
            DatabaseManager.testConnection();
        } catch (DatabaseConnectionException e) {
            log.error("Не удалось подключиться к базе данных при запуске", e);
            showStartupError(e);
            Platform.exit();
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1150, 720);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        stage.setTitle("Цветочный магазин");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    private void showStartupError(DatabaseConnectionException e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Нет подключения к базе данных");
        alert.setHeaderText("Не удалось запустить приложение");
        alert.setContentText(e.getMessage()
                + "\n\nПроверьте файл database.properties и убедитесь, что PostgreSQL запущен.");
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
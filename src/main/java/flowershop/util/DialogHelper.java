package flowershop.util;

import flowershop.exception.BusinessException;
import flowershop.exception.DatabaseConnectionException;
import flowershop.exception.EntityNotFoundException;
import flowershop.exception.ValidationException;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public final class DialogHelper {

    private static final Logger log = LoggerFactory.getLogger(DialogHelper.class);
    private static final String STYLESHEET = "/css/styles.css";

    private DialogHelper() {
        // утилитный класс
    }

    /** Показывает пользователю понятное сообщение об ошибке. Stack trace уходит только в лог. */
    public static void showError(Throwable e) {
        if (e instanceof ValidationException ve) {
            show(AlertType.WARNING, "Проверьте введённые данные", ve.getUserMessage());

        } else if (e instanceof EntityNotFoundException) {
            show(AlertType.WARNING, "Запись не найдена",
                    e.getMessage() + "\nВозможно, она уже была удалена. Обновите данные.");

        } else if (e instanceof BusinessException) {
            show(AlertType.WARNING, "Действие невозможно", e.getMessage());

        } else if (e instanceof DatabaseConnectionException) {
            log.error("Ошибка работы с базой данных", e);
            show(AlertType.ERROR, "Ошибка базы данных",
                    "Не удалось выполнить операцию с базой данных.\n"
                            + "Проверьте, что PostgreSQL запущен, и повторите попытку.");

        } else {
            log.error("Непредвиденная ошибка", e);
            show(AlertType.ERROR, "Непредвиденная ошибка",
                    "Произошла внутренняя ошибка. Подробности записаны в журнал.");
        }
    }

    public static void showInfo(String title, String message) {
        show(AlertType.INFORMATION, title, message);
    }

    /** Подтверждение опасного действия (например, удаления). true, если пользователь согласился. */
    public static boolean confirm(String title, String message, String confirmLabel) {
        ButtonType yes = new ButtonType(confirmLabel, ButtonData.OK_DONE);
        ButtonType no = new ButtonType("Отмена", ButtonData.CANCEL_CLOSE);

        Alert alert = new Alert(AlertType.CONFIRMATION, message, yes, no);
        alert.setTitle(title);
        alert.setHeaderText(title);
        applyStyle(alert);

        return alert.showAndWait().orElse(no) == yes;
    }

    private static void show(AlertType type, String title, String message) {
        // Диалоги можно показывать только из потока JavaFX
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> show(type, title, message));
            return;
        }
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(title);
        applyStyle(alert);
        alert.showAndWait();
    }

    private static void applyStyle(Alert alert) {
        URL css = DialogHelper.class.getResource(STYLESHEET);
        if (css != null) {
            alert.getDialogPane().getStylesheets().add(css.toExternalForm());
        }
    }
}
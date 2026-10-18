package flowershop.util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

// Универсальный диалог с формой из FXML. Если save() выбросил исключение,
// оказывается сообщение, а диалог остаётся открытым, и пользователь исправляет данные
public final class FormDialog {

    private static final String STYLESHEET = "/css/styles.css";

    private FormDialog() {
    }

    /** @return true, если пользователь нажал «Сохранить» и данные успешно сохранены */
    public static <C extends FormController> boolean show(
            Window owner, String title, String fxmlPath, Consumer<C> configure) {
        try {
            FXMLLoader loader = new FXMLLoader(FormDialog.class.getResource(fxmlPath));
            Parent form = loader.load();
            C controller = loader.getController();
            configure.accept(controller);
            return showDialog(owner, title, form, controller);
        } catch (IOException e) {
            DialogHelper.showError(new IllegalStateException("Не удалось открыть форму " + fxmlPath, e));
            return false;
        } catch (RuntimeException e) {
            DialogHelper.showError(e);
            return false;
        }
    }

    public static void applyStyles(DialogPane pane) {
        URL css = FormDialog.class.getResource(STYLESHEET);
        if (css != null) {
            pane.getStylesheets().add(css.toExternalForm());
        }
    }

    private static boolean showDialog(Window owner, String title, Parent form, FormController controller) {
        ButtonType saveType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle(title);

        DialogPane pane = dialog.getDialogPane();
        pane.setContent(form);
        pane.getButtonTypes().addAll(saveType, cancelType);
        applyStyles(pane);

        Button saveButton = (Button) pane.lookupButton(saveType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                controller.save();
            } catch (RuntimeException e) {
                event.consume(); // не закрываем диалог
                DialogHelper.showError(e);
            }
        });

        return dialog.showAndWait().filter(button -> button == saveType).isPresent();
    }
}
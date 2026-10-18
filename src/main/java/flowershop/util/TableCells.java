package flowershop.util;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.util.function.Function;

//Форматирование ячеек таблицы. Столбец хранит настоящее значение (число, дату), поэтому
// сортировка по заголовку работает правильно, а на экране показывается отформатированный текст.
public final class TableCells {

    private TableCells() {
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> formatted(Function<T, String> format) {
        return formatted(format, Pos.CENTER_LEFT);
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> formatted(
            Function<T, String> format, Pos alignment) {
        return column -> new TableCell<S, T>() {
            {
                setAlignment(alignment);
            }

            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (item == null ? "—" : format.apply(item)));
            }
        };
    }
}
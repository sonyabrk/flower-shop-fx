package flowershop;

import javafx.beans.binding.BooleanBinding;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Node;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.stage.Window;

// Общие приёмы работы с TableView, чтобы контроллеры не повторяли один и тот же код
public final class TableSupport {

    private TableSupport() {
        // утилитный класс
    }

    // Показывает данные в таблице с сортировкой по клику на заголовок:
    // comparator таблицы привязывается к SortedList
    public static <T> SortedList<T> attachSortedItems(TableView<T> table, ObservableList<T> source) {
        SortedList<T> sorted = new SortedList<>(source);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
        return sorted;
    }

    // Двойной клик по непустой строке выполняет действие (обычно «Изменить»)
    public static <T> void onRowDoubleClick(TableView<T> table, Runnable action) {
        table.setRowFactory(view -> {
            TableRow<T> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    action.run();
                }
            });
            return row;
        });
    }

    // Кнопки недоступны, пока в таблице не выбрана строка
    public static void disableWhenNothingSelected(TableView<?> table, Node... nodes) {
        BooleanBinding nothingSelected = table.getSelectionModel().selectedItemProperty().isNull();
        for (Node node : nodes) {
            node.disableProperty().bind(nothingSelected);
        }
    }

    // Окно, в котором находится элемент. Нужно диалогам как владелец (owner)
    public static Window windowOf(Node node) {
        return node.getScene().getWindow();
    }
}
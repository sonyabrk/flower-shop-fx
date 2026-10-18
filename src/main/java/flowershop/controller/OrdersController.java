package flowershop.controller;

import flowershop.AppContext;
import flowershop.TableSupport;
import flowershop.dto.OrderFilter;
import flowershop.dto.OrderRow;
import flowershop.exception.ValidationException;
import flowershop.model.OrderStatus;
import flowershop.service.OrderService;
import flowershop.util.CsvExporter;
import flowershop.util.DialogHelper;
import flowershop.util.FormDialog;
import flowershop.util.Formats;
import flowershop.util.FxTasks;
import flowershop.util.TableCells;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OrdersController implements Refreshable {

    private static final String ORDER_FORM = "/fxml/order-dialog.fxml";

    private final OrderService orderService = AppContext.get().orderService();

    // Цепочка данных: ObservableList → FilteredList (поиск и фильтры) → SortedList (сортировка) → TableView
    private final ObservableList<OrderRow> rows = FXCollections.observableArrayList();
    private FilteredList<OrderRow> filtered;
    private SortedList<OrderRow> sorted;
    private boolean applyingSort;

    @FXML private TextField searchField;
    @FXML private ComboBox<OrderStatus> statusFilter;
    @FXML private DatePicker dateFromPicker;
    @FXML private DatePicker dateToPicker;
    @FXML private ComboBox<SortOption> sortBox;
    @FXML private Button editButton;
    @FXML private Button statusButton;
    @FXML private Button deleteButton;
    @FXML private Label countLabel;

    @FXML private TableView<OrderRow> ordersTable;
    @FXML private TableColumn<OrderRow, String> customerColumn;
    @FXML private TableColumn<OrderRow, String> bouquetColumn;
    @FXML private TableColumn<OrderRow, Integer> quantityColumn;
    @FXML private TableColumn<OrderRow, BigDecimal> totalColumn;
    @FXML private TableColumn<OrderRow, OrderStatus> statusColumn;
    @FXML private TableColumn<OrderRow, LocalDate> orderDateColumn;
    @FXML private TableColumn<OrderRow, LocalDate> deliveryDateColumn;

    // Вариант сортировки для выпадающего списка: какой столбец и в каком направлении
    private record SortOption(String label, TableColumn<OrderRow, ?> column, TableColumn.SortType type) {
        @Override
        public String toString() {
            return label;
        }
    }

    @FXML
    private void initialize() {
        setupColumns();
        setupTable();
        setupFilters();
        setupSorting();
        setupButtons();
    }

    // Настройка интерфейса 

    private void setupColumns() {
        customerColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getCustomerName()));
        bouquetColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getBouquetName()));
        quantityColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getQuantity()));
        totalColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getTotalPrice()));
        statusColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getStatus()));
        orderDateColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getOrderDate()));
        deliveryDateColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getDeliveryDate()));

        quantityColumn.setCellFactory(TableCells.formatted(Object::toString, Pos.CENTER_RIGHT));
        totalColumn.setCellFactory(TableCells.formatted(Formats::money, Pos.CENTER_RIGHT));
        orderDateColumn.setCellFactory(TableCells.formatted(Formats::date));
        deliveryDateColumn.setCellFactory(TableCells.formatted(Formats::date));

        // Статус: русский текст + цвет через CSS-класс status-new, status-delivered и т. д.
        statusColumn.setCellFactory(column -> new TableCell<OrderRow, OrderStatus>() {
            @Override
            protected void updateItem(OrderStatus item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeIf(style -> style.startsWith("status-"));
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(Formats.status(item));
                    getStyleClass().add("status-" + item.name().toLowerCase());
                }
            }
        });
    }

    private void setupTable() {
        filtered = new FilteredList<>(rows, row -> true);
        sorted = TableSupport.attachSortedItems(ordersTable, filtered);
        TableSupport.onRowDoubleClick(ordersTable, this::onEdit);
    }

    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList(OrderStatus.values()));
        statusFilter.setConverter(Formats.converter(Formats::status));
        dateFromPicker.setConverter(Formats.dateConverter());
        dateToPicker.setConverter(Formats.dateConverter());

        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilter());
        statusFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyFilter());
        dateFromPicker.valueProperty().addListener((obs, oldValue, newValue) -> applyFilter());
        dateToPicker.valueProperty().addListener((obs, oldValue, newValue) -> applyFilter());
    }

    private void setupSorting() {
        sortBox.setItems(FXCollections.observableArrayList(
                new SortOption("Дата заказа: сначала новые", orderDateColumn, TableColumn.SortType.DESCENDING),
                new SortOption("Дата заказа: сначала старые", orderDateColumn, TableColumn.SortType.ASCENDING),
                new SortOption("Сумма: по убыванию", totalColumn, TableColumn.SortType.DESCENDING),
                new SortOption("Сумма: по возрастанию", totalColumn, TableColumn.SortType.ASCENDING),
                new SortOption("Клиент: по алфавиту", customerColumn, TableColumn.SortType.ASCENDING)));

        // Выбор в списке = сортировка по соответствующему столбцу таблицы
        sortBox.valueProperty().addListener((obs, oldValue, option) -> {
            if (option == null) {
                return;
            }
            applyingSort = true;
            option.column().setSortType(option.type());
            ordersTable.getSortOrder().setAll(option.column());
            applyingSort = false;
        });

        // Если пользователь отсортировал кликом по заголовку, выбор в списке сбрасывается
        ordersTable.getSortOrder().addListener((ListChangeListener<TableColumn<OrderRow, ?>>) change -> {
            if (!applyingSort) {
                sortBox.getSelectionModel().clearSelection();
            }
        });

        sortBox.getSelectionModel().selectFirst();
    }

    private void setupButtons() {
        TableSupport.disableWhenNothingSelected(ordersTable, editButton, statusButton, deleteButton);
    }

    // Данные 

    @Override
    public void refresh() {
        FxTasks.load(orderService::getRows, list -> {
            rows.setAll(list);
            updateCount();
        });
    }

    private void applyFilter() {
        try {
            orderService.validatePeriod(dateFromPicker.getValue(), dateToPicker.getValue());
        } catch (ValidationException e) {
            DialogHelper.showError(e);
            dateToPicker.setValue(null);
            return;
        }
        OrderFilter filter = new OrderFilter(
                searchField.getText(), statusFilter.getValue(),
                dateFromPicker.getValue(), dateToPicker.getValue());
        filtered.setPredicate(filter::matches);
        updateCount();
    }

    private void updateCount() {
        countLabel.setText("Показано заказов: " + filtered.size() + " из " + rows.size());
    }

    // Обработчики кнопок 

    @FXML
    private void onAdd() {
        boolean saved = FormDialog.show(TableSupport.windowOf(ordersTable), "Новый заказ", ORDER_FORM,
                (OrderDialogController form) -> form.open(null));
        if (saved) {
            refresh();
        }
    }

    @FXML
    private void onEdit() {
        OrderRow row = selectedRow();
        if (row == null) {
            return;
        }
        // Заказ в финальном статусе (доставлен или отменён) редактировать нельзя
        if (orderService.allowedNextStatuses(row.getStatus()).isEmpty()) {
            DialogHelper.showInfo("Редактирование недоступно",
                    "Доставленные и отменённые заказы редактировать нельзя.");
            return;
        }
        boolean saved = FormDialog.show(TableSupport.windowOf(ordersTable), "Редактирование заказа", ORDER_FORM,
                (OrderDialogController form) -> form.open(row));
        if (saved) {
            refresh();
        }
    }

    @FXML
    private void onDelete() {
        OrderRow row = selectedRow();
        if (row == null) {
            return;
        }
        String question = "Удалить заказ клиента «" + row.getCustomerName() + "»\n("
                + row.getBouquetName() + ", " + Formats.money(row.getTotalPrice()) + ")?";
        if (!DialogHelper.confirm("Удаление заказа", question, "Удалить")) {
            return;
        }
        FxTasks.execute(() -> orderService.delete(row.getId()), this::refresh);
    }

    @FXML
    private void onChangeStatus() {
        OrderRow row = selectedRow();
        if (row == null) {
            return;
        }
        Set<OrderStatus> next = orderService.allowedNextStatuses(row.getStatus());
        if (next.isEmpty()) {
            DialogHelper.showInfo("Смена статуса", "Заказ в статусе «"
                    + Formats.status(row.getStatus()) + "» нельзя перевести в другой статус.");
            return;
        }

        // Предлагаем только допустимые переходы, поэтому ошибочный выбор невозможен
        ComboBox<OrderStatus> box = new ComboBox<>(
                FXCollections.observableArrayList(next.stream().sorted().toList()));
        box.setConverter(Formats.converter(Formats::status));
        box.getSelectionModel().selectFirst();
        box.setMaxWidth(Double.MAX_VALUE);

        VBox content = new VBox(10,
                new Label("Новый статус для заказа клиента «" + row.getCustomerName() + "»:"), box);
        content.getStyleClass().add("form-grid");

        ButtonType apply = new ButtonType("Применить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(TableSupport.windowOf(ordersTable));
        dialog.setTitle("Смена статуса");
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(apply, cancel);
        FormDialog.applyStyles(dialog.getDialogPane());

        if (dialog.showAndWait().filter(button -> button == apply).isEmpty()) {
            return;
        }
        OrderStatus target = box.getValue();
        FxTasks.execute(() -> orderService.changeStatus(row.getId(), target), this::refresh);
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    @FXML
    private void onReset() {
        searchField.clear();
        statusFilter.setValue(null);
        dateFromPicker.setValue(null);
        dateToPicker.setValue(null);
        sortBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void onExport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Экспорт заказов в CSV");
        chooser.setInitialFileName("orders.csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV (*.csv)", "*.csv"));
        File file = chooser.showSaveDialog(TableSupport.windowOf(ordersTable));
        if (file == null) {
            return; // пользователь нажал «Отмена»
        }
        Path path = file.toPath();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            path = path.resolveSibling(file.getName() + ".csv");
        }

        List<String> headers = List.of("Клиент", "Букет", "Количество", "Сумма",
                "Статус", "Дата заказа", "Дата доставки");
        List<List<String>> data = new ArrayList<>();
        for (OrderRow row : sorted) { // именно то, что сейчас видно в таблице: с фильтрами и сортировкой
            data.add(List.of(
                    row.getCustomerName(),
                    row.getBouquetName(),
                    String.valueOf(row.getQuantity()),
                    row.getTotalPrice().toPlainString().replace('.', ','),
                    Formats.status(row.getStatus()),
                    Formats.date(row.getOrderDate()),
                    row.getDeliveryDate() == null ? "" : Formats.date(row.getDeliveryDate())));
        }

        try {
            CsvExporter.export(path, headers, data);
            DialogHelper.showInfo("Экспорт завершён",
                    "Экспортировано заказов: " + data.size() + "\nФайл: " + path.getFileName());
        } catch (RuntimeException e) {
            DialogHelper.showError(e);
        }
    }

    // Вспомогательное 

    private OrderRow selectedRow() {
        return ordersTable.getSelectionModel().getSelectedItem();
    }

}
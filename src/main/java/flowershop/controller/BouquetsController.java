package flowershop.controller;

import flowershop.AppContext;
import flowershop.exception.ValidationException;
import flowershop.model.Bouquet;
import flowershop.service.BouquetService;
import flowershop.util.DialogHelper;
import flowershop.util.FormDialog;
import flowershop.util.Formats;
import flowershop.util.FxTasks;
import flowershop.util.InputParsers;
import flowershop.util.TableCells;
import flowershop.TableSupport;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BouquetsController implements Refreshable {

    private static final String BOUQUET_FORM = "/fxml/bouquet-dialog.fxml";

    private final BouquetService bouquetService = AppContext.get().bouquetService();
    private final ObservableList<Bouquet> rows = FXCollections.observableArrayList();

    @FXML private TextField searchField;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Label countLabel;
    @FXML private TableView<Bouquet> bouquetsTable;
    @FXML private TableColumn<Bouquet, String> nameColumn;
    @FXML private TableColumn<Bouquet, String> descriptionColumn;
    @FXML private TableColumn<Bouquet, BigDecimal> priceColumn;

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getName()));
        descriptionColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getDescription()));
        priceColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getPrice()));
        descriptionColumn.setCellFactory(TableCells.formatted(text -> text));
        priceColumn.setCellFactory(TableCells.formatted(Formats::money, Pos.CENTER_RIGHT));

        TableSupport.attachSortedItems(bouquetsTable, rows);
        bouquetsTable.getSortOrder().add(nameColumn);
        TableSupport.onRowDoubleClick(bouquetsTable, this::onEdit);
        TableSupport.disableWhenNothingSelected(bouquetsTable, editButton, deleteButton);
    }

    @Override
    public void refresh() {
        BigDecimal min;
        BigDecimal max;
        try {
            min = InputParsers.optionalDecimal("Цена от", minPriceField.getText());
            max = InputParsers.optionalDecimal("Цена до", maxPriceField.getText());
        } catch (ValidationException e) {
            DialogHelper.showError(e);
            return;
        }
        load(searchField.getText(), min, max);
    }

    /** Поиск по названию и описанию плюс фильтр по диапазону цены. Обе операции выполняет сервис. */
    private void load(String query, BigDecimal min, BigDecimal max) {
        FxTasks.load(() -> {
            List<Bouquet> found = bouquetService.search(query);
            if (min == null && max == null) {
                return found;
            }
            Set<Long> inRange = bouquetService.filterByPriceRange(min, max).stream()
                    .map(Bouquet::getId)
                    .collect(Collectors.toSet());
            return found.stream().filter(b -> inRange.contains(b.getId())).toList();
        }, list -> {
            rows.setAll(list);
            countLabel.setText("Букетов: " + rows.size());
        });
    }

    @FXML
    private void onApply() {
        refresh();
    }

    @FXML
    private void onReset() {
        searchField.clear();
        minPriceField.clear();
        maxPriceField.clear();
        refresh();
    }

    @FXML
    private void onAdd() {
        boolean saved = FormDialog.show(TableSupport.windowOf(bouquetsTable), "Новый букет", BOUQUET_FORM,
                (BouquetDialogController form) -> form.open(null));
        if (saved) {
            refresh();
        }
    }

    @FXML
    private void onEdit() {
        Bouquet bouquet = bouquetsTable.getSelectionModel().getSelectedItem();
        if (bouquet == null) {
            return;
        }
        boolean saved = FormDialog.show(TableSupport.windowOf(bouquetsTable), "Редактирование букета",
                BOUQUET_FORM, (BouquetDialogController form) -> form.open(bouquet));
        if (saved) {
            refresh();
        }
    }

    @FXML
    private void onDelete() {
        Bouquet bouquet = bouquetsTable.getSelectionModel().getSelectedItem();
        if (bouquet == null) {
            return;
        }
        if (!DialogHelper.confirm("Удаление букета",
                "Удалить букет «" + bouquet.getName() + "» из каталога?", "Удалить")) {
            return;
        }
        FxTasks.execute(() -> bouquetService.delete(bouquet.getId()), this::refresh);
    }
}
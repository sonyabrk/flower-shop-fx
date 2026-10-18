package flowershop.controller;

import flowershop.AppContext;
import flowershop.model.Customer;
import flowershop.service.CustomerService;
import flowershop.util.DialogHelper;
import flowershop.util.FormDialog;
import flowershop.util.FxTasks;
import flowershop.TableSupport;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class CustomersController implements Refreshable {

    private static final String CUSTOMER_FORM = "/fxml/customer-dialog.fxml";

    private final CustomerService customerService = AppContext.get().customerService();
    private final ObservableList<Customer> rows = FXCollections.observableArrayList();
    private int requestId; // номер последнего запроса: устаревшие ответы игнорируются

    @FXML private TextField searchField;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Label countLabel;
    @FXML private TableView<Customer> customersTable;
    @FXML private TableColumn<Customer, String> nameColumn;
    @FXML private TableColumn<Customer, String> phoneColumn;
    @FXML private TableColumn<Customer, String> emailColumn;

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getFullName()));
        phoneColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getPhone()));
        emailColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getEmail()));

        TableSupport.attachSortedItems(customersTable, rows);
        customersTable.getSortOrder().add(nameColumn);
        TableSupport.onRowDoubleClick(customersTable, this::onEdit);
        TableSupport.disableWhenNothingSelected(customersTable, editButton, deleteButton);

        searchField.textProperty().addListener((obs, oldValue, newValue) -> refresh());
    }

    @Override
    public void refresh() {
        int id = ++requestId;
        String query = searchField.getText();
        FxTasks.load(() -> customerService.search(query), list -> {
            if (id == requestId) {
                rows.setAll(list);
                countLabel.setText("Клиентов: " + rows.size());
            }
        });
    }

    @FXML
    private void onAdd() {
        boolean saved = FormDialog.show(TableSupport.windowOf(customersTable), "Новый клиент", CUSTOMER_FORM,
                (CustomerDialogController form) -> form.open(null));
        if (saved) {
            refresh();
        }
    }

    @FXML
    private void onEdit() {
        Customer customer = customersTable.getSelectionModel().getSelectedItem();
        if (customer == null) {
            return;
        }
        boolean saved = FormDialog.show(TableSupport.windowOf(customersTable), "Редактирование клиента",
                CUSTOMER_FORM, (CustomerDialogController form) -> form.open(customer));
        if (saved) {
            refresh();
        }
    }

    @FXML
    private void onDelete() {
        Customer customer = customersTable.getSelectionModel().getSelectedItem();
        if (customer == null) {
            return;
        }
        if (!DialogHelper.confirm("Удаление клиента",
                "Удалить клиента «" + customer.getFullName() + "»?", "Удалить")) {
            return;
        }
        FxTasks.execute(() -> customerService.delete(customer.getId()), this::refresh);
    }

    @FXML
    private void onRefresh() {
        refresh();
    }
}
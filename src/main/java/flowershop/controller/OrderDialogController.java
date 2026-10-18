package flowershop.controller;

import flowershop.AppContext;
import flowershop.dto.OrderRow;
import flowershop.exception.ValidationException;
import flowershop.model.Bouquet;
import flowershop.model.BouquetOrder;
import flowershop.model.Customer;
import flowershop.model.OrderStatus;
import flowershop.service.BouquetService;
import flowershop.service.CustomerService;
import flowershop.service.OrderService;
import flowershop.util.FieldStyles;
import flowershop.util.FormController;
import flowershop.util.Formats;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.util.Map;

public class OrderDialogController implements FormController {

    private final OrderService orderService = AppContext.get().orderService();
    private final CustomerService customerService = AppContext.get().customerService();
    private final BouquetService bouquetService = AppContext.get().bouquetService();

    @FXML private ComboBox<Customer> customerBox;
    @FXML private ComboBox<Bouquet> bouquetBox;
    @FXML private TextField quantityField;
    @FXML private Label totalLabel;
    @FXML private DatePicker orderDatePicker;
    @FXML private DatePicker deliveryDatePicker;
    @FXML private Label statusLabel;

    private Map<String, Node> fields;
    private OrderRow editing; // null — создаём новый заказ

    @FXML
    private void initialize() {
        // Связанные сущности выбираются из списка, вводить ID вручную не нужно
        customerBox.setConverter(Formats.converter(c -> c.getFullName() + " (" + c.getEmail() + ")"));
        bouquetBox.setConverter(Formats.converter(b -> b.getName() + " — " + Formats.money(b.getPrice())));
        orderDatePicker.setConverter(Formats.dateConverter());
        deliveryDatePicker.setConverter(Formats.dateConverter());
        orderDatePicker.setValue(LocalDate.now());

        // Сумма пересчитывается «на лету», но итоговый расчёт всё равно делает сервис
        quantityField.textProperty().addListener((obs, oldValue, newValue) -> updateTotal());
        bouquetBox.valueProperty().addListener((obs, oldValue, newValue) -> updateTotal());

        fields = Map.of(
                "Клиент", customerBox,
                "Букет", bouquetBox,
                "Количество", quantityField,
                "Дата заказа", orderDatePicker,
                "Дата доставки", deliveryDatePicker);
    }

    /** Вызывается после загрузки формы. row == null означает создание нового заказа. */
    public void open(OrderRow row) {
        editing = row;
        customerBox.setItems(FXCollections.observableArrayList(customerService.getAll()));
        bouquetBox.setItems(FXCollections.observableArrayList(bouquetService.getAll()));

        if (row == null) {
            quantityField.setText("1");
            statusLabel.setText(Formats.status(OrderStatus.NEW));
            return;
        }

        customerBox.getItems().stream()
                .filter(c -> c.getId().equals(row.getCustomerId()))
                .findFirst().ifPresent(customerBox::setValue);
        bouquetBox.getItems().stream()
                .filter(b -> b.getId().equals(row.getBouquetId()))
                .findFirst().ifPresent(bouquetBox::setValue);
        quantityField.setText(String.valueOf(row.getQuantity()));
        orderDatePicker.setValue(row.getOrderDate());
        deliveryDatePicker.setValue(row.getDeliveryDate());
        statusLabel.setText(Formats.status(row.getStatus()));
        totalLabel.setText(Formats.money(row.getTotalPrice())); // сохранённая сумма, пока не изменят букет или количество
    }

    @Override
    public void save() {
        FieldStyles.clear(customerBox, bouquetBox, quantityField, orderDatePicker, deliveryDatePicker);
        try {
            Integer quantity = parseQuantity(quantityField.getText());
            if (quantity == null) {
                throw new ValidationException("Количество", "введите целое число");
            }

            BouquetOrder order = new BouquetOrder();
            order.setCustomerId(customerBox.getValue() == null ? null : customerBox.getValue().getId());
            order.setBouquetId(bouquetBox.getValue() == null ? null : bouquetBox.getValue().getId());
            order.setQuantity(quantity);
            order.setOrderDate(orderDatePicker.getValue());
            order.setDeliveryDate(deliveryDatePicker.getValue());

            if (editing == null) {
                orderService.create(order);
            } else {
                orderService.update(editing.getId(), order);
            }
        } catch (ValidationException e) {
            FieldStyles.mark(fields, e.getField());
            throw e;
        }
    }

    private void updateTotal() {
        Bouquet bouquet = bouquetBox.getValue();
        Integer quantity = parseQuantity(quantityField.getText());
        boolean valid = bouquet != null && quantity != null
                && quantity >= 1 && quantity <= OrderService.MAX_QUANTITY;
        totalLabel.setText(valid
                ? Formats.money(OrderService.calculateTotal(bouquet.getPrice(), quantity))
                : "—");
    }

    private static Integer parseQuantity(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return null; // сообщение об ошибке сформирует save()
        }
    }
}
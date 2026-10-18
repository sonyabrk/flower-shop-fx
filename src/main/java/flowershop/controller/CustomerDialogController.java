package flowershop.controller;

import flowershop.AppContext;
import flowershop.exception.ValidationException;
import flowershop.model.Customer;
import flowershop.service.CustomerService;
import flowershop.util.FieldStyles;
import flowershop.util.FormController;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;

import java.util.Map;

public class CustomerDialogController implements FormController {

    private final CustomerService customerService = AppContext.get().customerService();

    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;

    private Map<String, Node> fields;
    private Customer editing; // null — создаём нового клиента

    @FXML
    private void initialize() {
        fields = Map.of("Имя", nameField, "Телефон", phoneField, "Email", emailField);
    }

    // Форма открывается с текущими значениями выбранного клиента
    public void open(Customer customer) {
        editing = customer;
        if (customer != null) {
            nameField.setText(customer.getFullName());
            phoneField.setText(customer.getPhone());
            emailField.setText(customer.getEmail());
        }
    }

    @Override
    public void save() {
        FieldStyles.clear(nameField, phoneField, emailField);
        Customer form = new Customer(nameField.getText(), phoneField.getText(), emailField.getText());
        try {
            if (editing == null) {
                customerService.create(form);
            } else {
                customerService.update(editing.getId(), form);
            }
        } catch (ValidationException e) {
            FieldStyles.mark(fields, e.getField());
            throw e;
        }
    }
}
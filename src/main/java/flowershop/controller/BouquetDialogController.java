package flowershop.controller;

import flowershop.AppContext;
import flowershop.exception.ValidationException;
import flowershop.model.Bouquet;
import flowershop.service.BouquetService;
import flowershop.util.FieldStyles;
import flowershop.util.FormController;
import flowershop.util.InputParsers;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.Map;

public class BouquetDialogController implements FormController {

    private final BouquetService bouquetService = AppContext.get().bouquetService();

    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextArea descriptionArea;

    private Map<String, Node> fields;
    private Bouquet editing; // null — создаём новый букет

    @FXML
    private void initialize() {
        fields = Map.of("Название", nameField, "Цена", priceField, "Описание", descriptionArea);
    }

    public void open(Bouquet bouquet) {
        editing = bouquet;
        if (bouquet != null) {
            nameField.setText(bouquet.getName());
            priceField.setText(bouquet.getPrice().toPlainString());
            descriptionArea.setText(bouquet.getDescription());
        }
    }

    @Override
    public void save() {
        FieldStyles.clear(nameField, priceField, descriptionArea);
        try {
            Bouquet form = new Bouquet(
                    nameField.getText(),
                    descriptionArea.getText(),
                    InputParsers.requiredDecimal("Цена", priceField.getText()));
            if (editing == null) {
                bouquetService.create(form);
            } else {
                bouquetService.update(editing.getId(), form);
            }
        } catch (ValidationException e) {
            FieldStyles.mark(fields, e.getField());
            throw e;
        }
    }
}
package flowershop.util;

import javafx.scene.Node;

import java.util.Map;

// Подсветка полей формы с ошибкой (стиль .field-error в CSS)
public final class FieldStyles {

    private static final String ERROR_CLASS = "field-error";

    private FieldStyles() {
    }

    public static void mark(Node node) {
        if (node != null && !node.getStyleClass().contains(ERROR_CLASS)) {
            node.getStyleClass().add(ERROR_CLASS);
        }
    }

    // Подсвечивает поле по имени, которое вернула ValidationException.getField()
    public static void mark(Map<String, Node> fields, String fieldName) {
        if (fieldName != null) {
            mark(fields.get(fieldName));
        }
    }

    public static void clear(Node... nodes) {
        for (Node node : nodes) {
            node.getStyleClass().remove(ERROR_CLASS);
        }
    }
}
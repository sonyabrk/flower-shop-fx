package flowershop.util;

import flowershop.exception.ValidationException;

import java.math.BigDecimal;

// Разбор введённого пользователем текста с понятным сообщением об ошибке
public final class InputParsers {

    private InputParsers() {
    }

    public static BigDecimal requiredDecimal(String field, String text) {
        BigDecimal value = optionalDecimal(field, text);
        if (value == null) {
            throw new ValidationException(field, "обязательное поле");
        }
        return value;
    }

    // Пустое поле означает «значение не задано» (null). Принимает и запятую, и точку
    public static BigDecimal optionalDecimal(String field, String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String normalized = text.replaceAll("\\s", "").replace(',', '.');
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException e) {
            throw new ValidationException(field, "введите число, например 2500 или 2500.50");
        }
    }
}
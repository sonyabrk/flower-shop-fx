package flowershop.service;

import flowershop.exception.ValidationException;

import java.math.BigDecimal;

// Общие проверки полей для всех сервисов (чтобы не дублировать один и тот же код).
// Методы возвращают значение без лишних пробелов, поэтому проверка и нормализация идут вместе.
final class Validation {

    // Максимум для NUMERIC(10, 2) в базе данных. 
    static final BigDecimal MAX_AMOUNT = new BigDecimal("99999999.99");

    private Validation() {
        // утилитный класс
    }

    static String requiredText(String field, String value, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(field, "обязательное поле");
        }
        String trimmed = value.trim();
        checkLength(field, trimmed, maxLength);
        return trimmed;
    }

    // Необязательное поле: пустая строка превращается в null
    static String optionalText(String field, String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        checkLength(field, trimmed, maxLength);
        return trimmed;
    }

    private static void checkLength(String field, String value, int maxLength) {
        if (value.length() > maxLength) {
            throw new ValidationException(field,
                    "максимум " + maxLength + " символов (сейчас " + value.length() + ")");
        }
    }
}
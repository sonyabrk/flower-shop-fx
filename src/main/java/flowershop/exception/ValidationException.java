package main.java.flowershop.exception;

public class ValidationException extends RuntimeException {

    private final String field;

    public ValidationException(String message) {
        this(null, message);
    }

    public ValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.field = null;
    }

    /** Название поля формы или null, если ошибка не привязана к полю. */
    public String getField() {
        return field;
    }

    /** Текст для пользователя: «Телефон: значение слишком длинное (максимум 30 символов)». */
    public String getUserMessage() {
        return field == null ? getMessage() : field + ": " + getMessage();
    }
}
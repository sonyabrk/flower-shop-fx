package flowershop.util;

import flowershop.model.OrderStatus;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.function.Function;

//Преобразование значений в текст для интерфейса (даты, деньги, русские названия статусов)
public final class Formats {

    private static final Locale RU = Locale.forLanguageTag("ru-RU");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String EMPTY = "—";

    private Formats() {
    }

    public static String date(LocalDate date) {
        return date == null ? EMPTY : DATE.format(date);
    }

    public static String money(BigDecimal amount) {
        if (amount == null) {
            return EMPTY;
        }
        NumberFormat format = NumberFormat.getNumberInstance(RU);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return format.format(amount) + " ₽";
    }

    public static String status(OrderStatus status) {
        if (status == null) {
            return EMPTY;
        }
        return switch (status) {
            case NEW -> "Новый";
            case CONFIRMED -> "Подтверждён";
            case IN_DELIVERY -> "В доставке";
            case DELIVERED -> "Доставлен";
            case CANCELLED -> "Отменён";
        };
    }

    /** Превращает объект в текст для ComboBox (иначе там был бы виден toString()). */
    public static <T> StringConverter<T> converter(Function<T, String> toText) {
        return new StringConverter<T>() {
            @Override
            public String toString(T value) {
                return value == null ? "" : toText.apply(value);
            }

            @Override
            public T fromString(String text) {
                return null; // ComboBox не редактируемый, обратное преобразование не нужно
            }
        };
    }

    /** Формат дд.мм.гггг для DatePicker независимо от региона компьютера. */
    public static StringConverter<LocalDate> dateConverter() {
        return new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : DATE.format(date);
            }

            @Override
            public LocalDate fromString(String text) {
                if (text == null || text.isBlank()) {
                    return null;
                }
                try {
                    return LocalDate.parse(text.trim(), DATE);
                } catch (DateTimeParseException e) {
                    return null; // неверный ввод: поле очищается, пользователь видит пустое значение
                }
            }
        };
    }
}
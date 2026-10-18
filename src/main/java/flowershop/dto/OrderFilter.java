package flowershop.dto;

import flowershop.model.OrderStatus;

import java.time.LocalDate;
import java.util.Locale;

// Условия поиска и фильтрации таблицы заказов.
// Пустое значение (null или пустая строка) означает «не фильтровать по этому полю».

public record OrderFilter(String searchText, OrderStatus status, LocalDate dateFrom, LocalDate dateTo) {

    public static OrderFilter empty() {
        return new OrderFilter(null, null, null, null);
    }

    // Подходит ли строка под все выбранные условия (они объединяются через «И»)
    public boolean matches(OrderRow row) {
        return matchesText(row) && matchesStatus(row) && matchesPeriod(row);
    }

    // Поиск по трём полям: клиент, букет, сумма
    private boolean matchesText(OrderRow row) {
        if (searchText == null || searchText.isBlank()) {
            return true;
        }
        String query = searchText.trim().toLowerCase(Locale.ROOT);
        return row.getCustomerName().toLowerCase(Locale.ROOT).contains(query)
                || row.getBouquetName().toLowerCase(Locale.ROOT).contains(query)
                || row.getTotalPrice().toPlainString().contains(query);
    }

    // Фильтр 1: статус
    private boolean matchesStatus(OrderRow row) {
        return status == null || row.getStatus() == status;
    }

    // Фильтр 2: период по дате заказа (границы включаются)
    private boolean matchesPeriod(OrderRow row) {
        if (dateFrom != null && row.getOrderDate().isBefore(dateFrom)) {
            return false;
        }
        return dateTo == null || !row.getOrderDate().isAfter(dateTo);
    }
}
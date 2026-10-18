package flowershop.service;

import flowershop.dto.OrderRow;
import flowershop.exception.BusinessException;
import flowershop.exception.EntityNotFoundException;
import flowershop.exception.ValidationException;
import flowershop.model.Bouquet;
import flowershop.model.BouquetOrder;
import flowershop.model.OrderStatus;
import flowershop.repository.BouquetRepository;
import flowershop.repository.CustomerRepository;
import flowershop.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OrderService {

    public static final int MAX_QUANTITY = 999;

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final BouquetRepository bouquetRepository;

    // Бизнес-правило: разрешённые переходы статусов
    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.NEW, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, Set.of(OrderStatus.IN_DELIVERY, OrderStatus.CANCELLED),
            OrderStatus.IN_DELIVERY, Set.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED),
            OrderStatus.DELIVERED, Set.of(),
            OrderStatus.CANCELLED, Set.of()
    );

    public OrderService(OrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        BouquetRepository bouquetRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.bouquetRepository = bouquetRepository;
    }

    // Данные для таблицы: заказы вместе с именем клиента и названием букета (один JOIN-запрос)
    public List<OrderRow> getRows() {
        return orderRepository.findAllRows();
    }

    public BouquetOrder getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Заказ с ID " + id + " не найден"));
    }

    public BouquetOrder create(BouquetOrder order) {
        LocalDate orderDate = order.getOrderDate() != null ? order.getOrderDate() : LocalDate.now();
        validateFields(order.getCustomerId(), order.getBouquetId(), order.getQuantity(),
                orderDate, order.getDeliveryDate());

        // Бизнес-правило: клиент и букет должны существовать
        requireCustomer(order.getCustomerId());
        Bouquet bouquet = requireBouquet(order.getBouquetId());

        order.setOrderDate(orderDate);
        order.setStatus(OrderStatus.NEW);
        // Бизнес-правило: сумма рассчитывается автоматически, а не вводится вручную
        order.setTotalPrice(totalFor(bouquet, order.getQuantity()));

        return orderRepository.save(order);
    }

    public BouquetOrder update(Long id, BouquetOrder updated) {
        BouquetOrder existing = getById(id);

        if (existing.getStatus() == OrderStatus.DELIVERED
                || existing.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Доставленные и отменённые заказы редактировать нельзя");
        }

        LocalDate orderDate = updated.getOrderDate() != null
                ? updated.getOrderDate() : existing.getOrderDate();
        validateFields(updated.getCustomerId(), updated.getBouquetId(), updated.getQuantity(),
                orderDate, updated.getDeliveryDate());

        requireCustomer(updated.getCustomerId());
        Bouquet bouquet = requireBouquet(updated.getBouquetId());

        // Сумма пересчитывается только если изменился букет или количество,
        // иначе изменение цены в каталоге задним числом поменяло бы старый заказ
        boolean repriceNeeded = !existing.getBouquetId().equals(updated.getBouquetId())
                || existing.getQuantity() != updated.getQuantity();
        if (repriceNeeded) {
            existing.setTotalPrice(totalFor(bouquet, updated.getQuantity()));
        }

        existing.setCustomerId(updated.getCustomerId());
        existing.setBouquetId(updated.getBouquetId());
        existing.setQuantity(updated.getQuantity());
        existing.setOrderDate(orderDate);
        existing.setDeliveryDate(updated.getDeliveryDate());

        return orderRepository.update(existing);
    }

    // Бизнес-правило: переход статуса только по разрешённой схеме
    public BouquetOrder changeStatus(Long id, OrderStatus newStatus) {
        BouquetOrder order = getById(id);

        if (!allowedNextStatuses(order.getStatus()).contains(newStatus)) {
            throw new BusinessException(
                    "Недопустимый переход статуса: " + order.getStatus() + " -> " + newStatus);
        }

        order.setStatus(newStatus);
        return orderRepository.update(order);
    }

    //Статусы, в которые можно перевести заказ. Нужен окну смены статуса, чтобы не предлагать лишнего
    public Set<OrderStatus> allowedNextStatuses(OrderStatus current) {
        return ALLOWED_TRANSITIONS.get(current);
    }

    public void delete(Long id) {
        getById(id);
        orderRepository.deleteById(id);
    }

    // Сумма заказа = цена букета × количество. Используется и формой для расчёта «на лету»
    public static BigDecimal calculateTotal(BigDecimal price, int quantity) {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    // Проверка периода в фильтре: дата «с» не должна быть позже даты «по»
    public void validatePeriod(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ValidationException("Период", "дата «с» не может быть позже даты «по»");
        }
    }

    private BigDecimal totalFor(Bouquet bouquet, int quantity) {
        BigDecimal total = calculateTotal(bouquet.getPrice(), quantity);
        if (total.compareTo(Validation.MAX_AMOUNT) > 0) {
            throw new ValidationException("Количество",
                    "сумма заказа превышает допустимую (99 999 999,99), уменьшите количество");
        }
        return total;
    }

    private void requireCustomer(Long customerId) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Выбранный клиент не найден (ID " + customerId + ")"));
    }

    private Bouquet requireBouquet(Long bouquetId) {
        return bouquetRepository.findById(bouquetId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Выбранный букет не найден (ID " + bouquetId + ")"));
    }

    private void validateFields(Long customerId, Long bouquetId, int quantity,
                                LocalDate orderDate, LocalDate deliveryDate) {
        if (customerId == null) {
            throw new ValidationException("Клиент", "выберите клиента из списка");
        }
        if (bouquetId == null) {
            throw new ValidationException("Букет", "выберите букет из списка");
        }
        if (quantity < 1 || quantity > MAX_QUANTITY) {
            throw new ValidationException("Количество", "должно быть от 1 до " + MAX_QUANTITY);
        }
        if (orderDate.isAfter(LocalDate.now())) {
            throw new ValidationException("Дата заказа", "не может быть в будущем");
        }
        if (deliveryDate != null && deliveryDate.isBefore(orderDate)) {
            throw new ValidationException("Дата доставки", "не может быть раньше даты заказа");
        }
    }
}
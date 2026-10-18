package flowershop.service;

import flowershop.exception.BusinessException;
import flowershop.exception.EntityNotFoundException;
import flowershop.exception.ValidationException;
import flowershop.model.Bouquet;
import flowershop.model.BouquetOrder;
import flowershop.model.OrderStatus;
import flowershop.repository.BouquetRepository;
import flowershop.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;

public class BouquetService {

    private static final int NAME_MAX_LENGTH = 150;
    private static final int DESCRIPTION_MAX_LENGTH = 1000;

    private final BouquetRepository bouquetRepository;
    private final OrderRepository orderRepository;

    public BouquetService(BouquetRepository bouquetRepository, OrderRepository orderRepository) {
        this.bouquetRepository = bouquetRepository;
        this.orderRepository = orderRepository;
    }

    public Bouquet create(Bouquet bouquet) {
        validateAndNormalize(bouquet);
        return bouquetRepository.save(bouquet);
    }

    public Bouquet getById(Long id) {
        return bouquetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Букет с ID " + id + " не найден"));
    }

    public List<Bouquet> getAll() {
        return bouquetRepository.findAll();
    }

    public Bouquet update(Long id, Bouquet updated) {
        Bouquet existing = getById(id);
        validateAndNormalize(updated);

        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setPrice(updated.getPrice());

        return bouquetRepository.update(existing);
    }

    // Бизнес-правило: нельзя удалить букет, по которому есть незавершённые заказы.
    // Кроме того, в БД стоит ON DELETE RESTRICT, поэтому букет из истории заказов тоже удалить нельзя.
    public void delete(Long id) {
        getById(id);

        List<BouquetOrder> orders = orderRepository.findByBouquetId(id);
        long active = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.DELIVERED
                        && o.getStatus() != OrderStatus.CANCELLED)
                .count();

        if (active > 0) {
            throw new BusinessException(
                    "Нельзя удалить букет: по нему есть незавершённые заказы (" + active + ")");
        }
        if (!orders.isEmpty()) {
            throw new BusinessException(
                    "Нельзя удалить букет: он указан в заказах из истории (" + orders.size() + ")");
        }
        bouquetRepository.deleteById(id);
    }

    public List<Bouquet> search(String query) {
        if (query == null || query.isBlank()) {
            return bouquetRepository.findAll();
        }
        String lower = query.trim().toLowerCase();
        return bouquetRepository.findAll().stream()
                .filter(b -> b.getName().toLowerCase().contains(lower)
                        || (b.getDescription() != null
                            && b.getDescription().toLowerCase().contains(lower)))
                .toList();
    }

    // Фильтр по цене; граница, равная null, не учитывается
    public List<Bouquet> filterByPriceRange(BigDecimal min, BigDecimal max) {
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new ValidationException("Цена", "минимум не может быть больше максимума");
        }
        return bouquetRepository.findAll().stream()
                .filter(b -> min == null || b.getPrice().compareTo(min) >= 0)
                .filter(b -> max == null || b.getPrice().compareTo(max) <= 0)
                .toList();
    }

    // Проверяет поля формы и заодно убирает лишние пробелы по краям
    private void validateAndNormalize(Bouquet bouquet) {
        bouquet.setName(Validation.requiredText("Название", bouquet.getName(), NAME_MAX_LENGTH));
        bouquet.setDescription(
                Validation.optionalText("Описание", bouquet.getDescription(), DESCRIPTION_MAX_LENGTH));

        BigDecimal price = bouquet.getPrice();
        if (price == null) {
            throw new ValidationException("Цена", "обязательное поле");
        }
        if (price.signum() <= 0) {
            throw new ValidationException("Цена", "должна быть больше нуля");
        }
        if (price.compareTo(Validation.MAX_AMOUNT) > 0) {
            throw new ValidationException("Цена", "не может превышать 99 999 999,99");
        }
        if (price.stripTrailingZeros().scale() > 2) {
            throw new ValidationException("Цена", "не более двух знаков после запятой");
        }
    }
}
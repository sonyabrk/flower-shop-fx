package flowershop.service;

import flowershop.exception.BusinessException;
import flowershop.exception.EntityNotFoundException;
import flowershop.exception.ValidationException;
import flowershop.model.BouquetOrder;
import flowershop.model.Customer;
import flowershop.repository.CustomerRepository;
import flowershop.repository.OrderRepository;

import java.util.List;
import java.util.regex.Pattern;

public class CustomerService {

    private static final int NAME_MAX_LENGTH = 150;
    private static final int EMAIL_MAX_LENGTH = 150;
    private static final int PHONE_MAX_LENGTH = 30;
    private static final int PHONE_MIN_DIGITS = 10;
    private static final int PHONE_MAX_DIGITS = 15;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE_CHARS = Pattern.compile("^[0-9+()\\-\\s]+$");

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public CustomerService(CustomerRepository customerRepository, OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    public Customer create(Customer customer) {
        validateAndNormalize(customer);

        // Бизнес-правило: email уникален
        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new BusinessException("Клиент с таким email уже существует");
        }
        return customerRepository.save(customer);
    }

    public Customer getById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Клиент с ID " + id + " не найден"));
    }

    public List<Customer> getAll() {
        return customerRepository.findAll();
    }

    public Customer update(Long id, Customer updated) {
        Customer existing = getById(id); // бросит EntityNotFoundException, если запись уже удалена
        validateAndNormalize(updated);

        if (customerRepository.existsByEmailAndIdNot(updated.getEmail(), id)) {
            throw new BusinessException("Клиент с таким email уже существует");
        }

        existing.setFullName(updated.getFullName());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());

        return customerRepository.update(existing);
    }

    // Бизнес-правило: нельзя удалить клиента, у которого есть заказы
    public void delete(Long id) {
        getById(id);

        List<BouquetOrder> orders = orderRepository.findByCustomerId(id);
        if (!orders.isEmpty()) {
            throw new BusinessException(
                    "Нельзя удалить клиента: у него есть заказы (" + orders.size() + ")");
        }
        customerRepository.deleteById(id);
    }

    public List<Customer> search(String query) {
        if (query == null || query.isBlank()) {
            return customerRepository.findAll();
        }
        String lower = query.trim().toLowerCase();
        return customerRepository.findAll().stream()
                .filter(c -> c.getFullName().toLowerCase().contains(lower)
                        || c.getEmail().toLowerCase().contains(lower)
                        || c.getPhone().toLowerCase().contains(lower))
                .toList();
    }

    // Проверяет поля формы и заодно убирает лишние пробелы по краям
    private void validateAndNormalize(Customer customer) {
        customer.setFullName(Validation.requiredText("Имя", customer.getFullName(), NAME_MAX_LENGTH));

        String email = Validation.requiredText("Email", customer.getEmail(), EMAIL_MAX_LENGTH);
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Email", "некорректный адрес, пример: name@example.com");
        }
        customer.setEmail(email);

        String phone = Validation.requiredText("Телефон", customer.getPhone(), PHONE_MAX_LENGTH);
        long digits = phone.chars().filter(Character::isDigit).count();
        if (!PHONE_CHARS.matcher(phone).matches()
                || digits < PHONE_MIN_DIGITS || digits > PHONE_MAX_DIGITS) {
            throw new ValidationException("Телефон",
                    "допустимы цифры, пробелы и символы + ( ) -, всего от "
                            + PHONE_MIN_DIGITS + " до " + PHONE_MAX_DIGITS + " цифр");
        }
        customer.setPhone(phone);
    }
}
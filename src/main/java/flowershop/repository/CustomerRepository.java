package flowershop.repository;
 
import flowershop.model.Customer;
 
public interface CustomerRepository extends Repository<Customer, Long> {
 
    // Есть ли клиент с таким email (без учёта регистра), для проверки при создании
    boolean existsByEmail(String email);
 
    // То же, но исключая клиента с указанным id, для проверки при редактировании
    boolean existsByEmailAndIdNot(String email, Long id);
}
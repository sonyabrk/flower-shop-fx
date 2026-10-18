package flowershop;

import flowershop.repository.BouquetRepository;
import flowershop.repository.BouquetRepositoryJdbc;
import flowershop.repository.CustomerRepository;
import flowershop.repository.CustomerRepositoryJdbc;
import flowershop.repository.OrderRepository;
import flowershop.repository.OrderRepositoryJdbc;
import flowershop.repository.StatisticsRepositoryJdbc;
import flowershop.service.BouquetService;
import flowershop.service.CustomerService;
import flowershop.service.OrderService;
import flowershop.service.StatisticsService;

// Единственное место, где создаются репозитории и сервисы. Контроллеры берут сервисы отсюда
public final class AppContext {

    private static AppContext instance;

    private final CustomerService customerService;
    private final BouquetService bouquetService;
    private final OrderService orderService;
    private final StatisticsService statisticsService;

    private AppContext() {
        CustomerRepository customerRepository = new CustomerRepositoryJdbc();
        BouquetRepository bouquetRepository = new BouquetRepositoryJdbc();
        OrderRepository orderRepository = new OrderRepositoryJdbc();

        customerService = new CustomerService(customerRepository, orderRepository);
        bouquetService = new BouquetService(bouquetRepository, orderRepository);
        orderService = new OrderService(orderRepository, customerRepository, bouquetRepository);
        statisticsService = new StatisticsService(new StatisticsRepositoryJdbc());
    }

    public static synchronized AppContext get() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    public CustomerService customerService() { return customerService; }

    public BouquetService bouquetService() { return bouquetService; }

    public OrderService orderService() { return orderService; }

    public StatisticsService statisticsService() { return statisticsService; }
}
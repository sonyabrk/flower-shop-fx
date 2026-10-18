package flowershop.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TabPane;

import java.util.List;

public class MainController {

    @FXML private TabPane tabPane;

    // Контроллеры вложенных вкладок: имя поля = fx:id из <fx:include> + "Controller"
    @FXML private OrdersController ordersTabController;
    @FXML private CustomersController customersTabController;
    @FXML private BouquetsController bouquetsTabController;
    @FXML private StatisticsController statisticsTabController;

    @FXML
    private void initialize() {
        List<Refreshable> tabs = List.of(
                ordersTabController, customersTabController,
                bouquetsTabController, statisticsTabController);

        // При переходе на вкладку данные перезагружаются, поэтому изменения в других вкладках видны сразу
        tabPane.getSelectionModel().selectedIndexProperty()
                .addListener((obs, oldIndex, newIndex) -> tabs.get(newIndex.intValue()).refresh());

        ordersTabController.refresh();
    }
}
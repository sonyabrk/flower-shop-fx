package flowershop.controller;

import flowershop.AppContext;
import flowershop.dto.OrderStatistics;
import flowershop.model.OrderStatus;
import flowershop.service.StatisticsService;
import flowershop.util.Formats;
import flowershop.util.FxTasks;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsController implements Refreshable {

    private final StatisticsService statisticsService = AppContext.get().statisticsService();

    @FXML private Label totalOrdersLabel;
    @FXML private Label totalCustomersLabel;
    @FXML private Label totalBouquetsLabel;
    @FXML private Label revenueLabel;
    @FXML private Label averageLabel;
    @FXML private Label minLabel;
    @FXML private Label maxLabel;
    @FXML private Label popularLabel;
    @FXML private Label cancelledLabel;
    @FXML private PieChart statusChart;

    @Override
    public void refresh() {
        FxTasks.load(statisticsService::getStatistics, this::show);
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    private void show(OrderStatistics stats) {
        totalOrdersLabel.setText(String.valueOf(stats.totalOrders()));
        totalCustomersLabel.setText(String.valueOf(stats.totalCustomers()));
        totalBouquetsLabel.setText(String.valueOf(stats.totalBouquets()));
        revenueLabel.setText(Formats.money(stats.deliveredRevenue()));
        averageLabel.setText(Formats.money(stats.averageOrderTotal()));
        minLabel.setText(Formats.money(stats.minOrderTotal()));
        maxLabel.setText(Formats.money(stats.maxOrderTotal()));
        popularLabel.setText(stats.mostPopularBouquet() == null ? "—" : stats.mostPopularBouquet());
        cancelledLabel.setText(String.format(Locale.forLanguageTag("ru-RU"), "%.1f %%", stats.cancelledPercent()));

        List<PieChart.Data> slices = new ArrayList<>();
        for (Map.Entry<OrderStatus, Long> entry : stats.ordersByStatus().entrySet()) {
            if (entry.getValue() > 0) {
                slices.add(new PieChart.Data(
                        Formats.status(entry.getKey()) + " — " + entry.getValue(), entry.getValue()));
            }
        }
        statusChart.setData(FXCollections.observableArrayList(slices));
    }
}
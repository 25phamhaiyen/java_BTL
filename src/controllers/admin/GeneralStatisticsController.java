package controllers.admin;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import repository.BookingRepository;
import repository.CustomerRepository;
import repository.RevenueRepository;

public class GeneralStatisticsController {

    @FXML
    private BarChart<String, Number> revenueChart;

    @FXML
    private CategoryAxis revenueXAxis;

    @FXML
    private NumberAxis revenueYAxis;

    @FXML
    private BarChart<String, Number> bookingsChart;

    @FXML
    private CategoryAxis bookingsXAxis;

    @FXML
    private NumberAxis bookingsYAxis;

    @FXML
    private BarChart<String, Number> customersChart;

    @FXML
    private CategoryAxis customersXAxis;

    @FXML
    private NumberAxis customersYAxis;

    @FXML
    private RadioButton rbWeek, rbMonth, rbYear;

    @FXML
    private Label lblRevenueStats, lblBookingsStats, lblNewCustomersStats;

    private final RevenueRepository revenueRepository = new RevenueRepository();
    private final BookingRepository bookingRepository = new BookingRepository();
    private final CustomerRepository customerRepository = new CustomerRepository();

    @FXML
    public void initialize() {
        // Tạo nhóm cho các RadioButton
        ToggleGroup timeGroup = new ToggleGroup();
        rbWeek.setToggleGroup(timeGroup);
        rbMonth.setToggleGroup(timeGroup);
        rbYear.setToggleGroup(timeGroup);

        // Chọn mặc định
        rbWeek.setSelected(true);

        // Gắn sự kiện thay đổi
        timeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                RadioButton selected = (RadioButton) newValue;
                if (selected == rbWeek) {
                    loadChartDataByTimeUnit("WEEK");
                } else if (selected == rbMonth) {
                    loadChartDataByTimeUnit("MONTH");
                } else if (selected == rbYear) {
                    loadChartDataByTimeUnit("YEAR");
                }
            }
        });

        loadChartDataByTimeUnit("WEEK");
    }


    private void loadChartDataByTimeUnit(String timeUnit) {
        // Lấy dữ liệu từ repository và cập nhật biểu đồ
        XYChart.Series<String, Number> revenueSeries = revenueRepository.getRevenueData(timeUnit);
        XYChart.Series<String, Number> bookingsSeries = bookingRepository.getBookingData(timeUnit);
        XYChart.Series<String, Number> customersSeries = customerRepository.getCustomerData(timeUnit);

        revenueChart.getData().clear();
        revenueChart.getData().add(revenueSeries);

        bookingsChart.getData().clear();
        bookingsChart.getData().add(bookingsSeries);

        customersChart.getData().clear();
        customersChart.getData().add(customersSeries);

        // Gọi cập nhật label theo timeUnit
        updateStatisticsLabels(timeUnit);
    }


    private void updateStatisticsLabels(String timeUnit) {
        double totalRevenue = revenueRepository.getRevenueTotal(timeUnit);
        int totalBookings = bookingRepository.getTotalBookings(timeUnit);
        int totalCustomers = customerRepository.getTotalNewCustomers(timeUnit);

        lblRevenueStats.setText("Doanh thu: " + formatCurrency(totalRevenue));
        lblBookingsStats.setText("Số lượng đặt lịch mới: " + totalBookings);
        lblNewCustomersStats.setText("Khách hàng mới: " + totalCustomers);
    }



    private String formatCurrency(double amount) {
        // Định dạng số tiền theo kiểu tiền tệ Việt Nam
        java.text.NumberFormat currencyFormat = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("vi", "VN"));
        return currencyFormat.format(amount);
    }
}
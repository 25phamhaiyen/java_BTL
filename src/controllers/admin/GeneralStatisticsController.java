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

        // Gắn sự kiện thay đổi cho RadioButton
        timeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == rbWeek) {
                loadChartDataByTimeUnit("WEEK");
            } else if (newValue == rbMonth) {
                loadChartDataByTimeUnit("MONTH");
            } else if (newValue == rbYear) {
                loadChartDataByTimeUnit("YEAR");
            }
        });

        // Load dữ liệu mặc định (theo tuần)
        loadChartDataByTimeUnit("WEEK");
    }

    private void loadChartDataByTimeUnit(String timeUnit) {
        // Lấy dữ liệu từ repository và cập nhật biểu đồ
        XYChart.Series<String, Number> revenueSeries = revenueRepository.getRevenueData(timeUnit);
        XYChart.Series<String, Number> bookingsSeries = bookingRepository.getBookingData(timeUnit);
        XYChart.Series<String, Number> customersSeries = customerRepository.getCustomerData(timeUnit);

        // Cập nhật dữ liệu cho biểu đồ doanh thu
        revenueChart.getData().clear();
        revenueChart.getData().add(revenueSeries);

        // Cập nhật dữ liệu cho biểu đồ số lượng đặt lịch
        bookingsChart.getData().clear();
        bookingsChart.getData().add(bookingsSeries);

        // Cập nhật dữ liệu cho biểu đồ khách hàng mới
        customersChart.getData().clear();
        customersChart.getData().add(customersSeries);

        // Cập nhật nhãn thống kê
        updateStatisticsLabels();
    }

    private void updateStatisticsLabels() {
        // Lấy dữ liệu tổng hợp từ repository
        double totalRevenue = revenueRepository.getMonthlyRevenue();
        int totalBookings = bookingRepository.getMonthlyBookings();
        int totalCustomers = customerRepository.getMonthlyNewCustomers();

        // Cập nhật nhãn thống kê
        lblRevenueStats.setText("Doanh thu: " + formatCurrency(totalRevenue));
        lblBookingsStats.setText("Số lượng đặt lịch: " + totalBookings);
        lblNewCustomersStats.setText("Khách hàng mới: " + totalCustomers);
    }

    private String formatCurrency(double amount) {
        // Định dạng số tiền theo kiểu tiền tệ Việt Nam
        java.text.NumberFormat currencyFormat = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("vi", "VN"));
        return currencyFormat.format(amount);
    }
}
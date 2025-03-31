package tests;

import dao.OrderDAO;
import entity.Customer;
import entity.HappenStatus;
import entity.Order;
import entity.Staff;
import Enum.TypeOrder;

import java.sql.Timestamp;
import java.util.List;

public class testOrderDAO {
    public static void main(String[] args) {
        OrderDAO orderDAO = OrderDAO.getInstance();

        Customer customer = new Customer();
        customer.setCustomerID(1);

        Staff staff = new Staff();
        staff.setStaffID(2);

        HappenStatus happenStatus = new HappenStatus();
        happenStatus.setHappenStatusID(1);

        // 1️ Thêm đơn hàng mới
        Order newOrder = new Order(
            0,
            new Timestamp(System.currentTimeMillis()),
            new Timestamp(System.currentTimeMillis() + 86400000),
            TypeOrder.APPOINTMENT,
            0.0,
            customer,
            staff,
            happenStatus
        );

        int result = orderDAO.insert(newOrder);
        if (result > 0) {
            System.out.println(" Thêm đơn hàng thành công! OrderID = " + newOrder.getOrderId());

            // 2️ Kiểm tra cập nhật đơn hàng
            newOrder.setTotal(1000.0);
            int updateResult = orderDAO.update(newOrder);
            System.out.println(updateResult > 0 ? " Cập nhật đơn hàng thành công!" : " Lỗi khi cập nhật đơn hàng.");

            // 3️ Lấy đơn hàng theo ID
            Order foundOrder = orderDAO.selectById(newOrder.getOrderId());
            System.out.println(foundOrder != null ? " Tìm thấy đơn hàng: " + foundOrder : " Không tìm thấy đơn hàng.");

            // 4️ Lấy danh sách đơn hàng theo điều kiện (lọc theo customerID)
            List<Order> filteredOrders = orderDAO.selectByCondition("Customer_ID = ?", newOrder.getCustomer().getCustomerID());
            System.out.println(" Danh sách đơn hàng của khách hàng ID=" + newOrder.getCustomer().getCustomerID());
            for (Order order : filteredOrders) {
                System.out.println(order);
            }

            // 5️⃣ Xóa đơn hàng
            int deleteResult = orderDAO.delete(newOrder);
            System.out.println(deleteResult > 0 ? " Xóa đơn hàng thành công!" : " Lỗi khi xóa đơn hàng.");
        } else {
            System.out.println("Lỗi khi thêm đơn hàng.");
        }
    }
}

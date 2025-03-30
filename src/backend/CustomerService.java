package backend;

import dao.CustomerDAO;
import entity.Customer;
import exception.BusinessException;
import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class CustomerService {
    private final CustomerDAO customerDAO;

    public CustomerService() {
        this.customerDAO = new CustomerDAO();
    }

    // Kiểm tra dữ liệu khách hàng trước khi thêm hoặc cập nhật
    private void validateCustomer(Customer customer) {
        if (customer == null) {
            throw new BusinessException("Khách hàng không được null.");
        }
        if (customer.getFirstName() == null || customer.getFirstName().trim().isEmpty()) {
            throw new BusinessException("Tên khách hàng không được để trống.");
        }
        if (!customer.getPhoneNumber().matches("\\d{10}")) {
            throw new BusinessException("Số điện thoại phải gồm 10 chữ số.");
        }
        if (customer.getCitizenNumber() == null || !customer.getCitizenNumber().matches("\\d{12}")) {
            throw new BusinessException("CMND/CCCD phải gồm 12 chữ số.");
        }
        if (customer.getAddress() == null || customer.getAddress().trim().isEmpty()) {
            throw new BusinessException("Địa chỉ không được để trống.");
        }
    }

    // Thêm khách hàng mới
    public void addCustomer(Customer customer) {
        validateCustomer(customer);
        int result = customerDAO.insert(customer);
        if (result == 0) {
            throw new BusinessException("Không thể thêm khách hàng.");
        }
    }


    // Cập nhật thông tin khách hàng
    public void updateCustomer(Customer customer) {
        validateCustomer(customer);
        int result = customerDAO.update(customer);
        if (result == 0) {
            throw new BusinessException("Không thể cập nhật khách hàng.");
        }
    }
    
    

    // Xóa khách hàng theo ID
    public void deleteCustomer(int customerID) {
        Customer customer = customerDAO.selectById(customerID);
        if (customer == null) {
            throw new BusinessException("Không tìm thấy khách hàng với ID: " + customerID);
        }
        customerDAO.delete(customer);
    }




    // Lấy danh sách tất cả khách hàng
    public List<Customer> getAllCustomers() {
        List<Customer> customers = customerDAO.selectAll();
        if (customers.isEmpty()) {
            throw new BusinessException("Không có khách hàng nào trong hệ thống.");
        }
        return customers;
    }


    // Tìm khách hàng theo ID
    public Customer getCustomerById(int customerId) throws SQLException {
        Customer customer = customerDAO.selectById(customerId);
		if (customer == null) {
		    throw new BusinessException("Không tìm thấy khách hàng với ID: " + customerId);
		}
		return customer;
    }

    // Tìm khách hàng theo số điện thoại
    public Customer findCustomerByPhoneNumber(String phoneNumber) {
        List<Customer> customers = customerDAO.selectByCondition("phoneNumber = ?", phoneNumber);
        if (customers.isEmpty()) {
            throw new BusinessException("Không tìm thấy khách hàng với số điện thoại: " + phoneNumber);
        }
        return customers.get(0);
    }


    // Xóa toàn bộ khách hàng (kèm reset AUTO_INCREMENT)
    public void deleteAllCustomers() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            customerDAO.deleteAll(conn);
            customerDAO.resetAutoIncrement(conn);

            conn.commit();
        } catch (SQLException e) {
            throw new BusinessException("Lỗi khi xóa tất cả khách hàng: " + e.getMessage(), e);
        }
    }

}

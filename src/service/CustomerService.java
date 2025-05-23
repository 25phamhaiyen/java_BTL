package service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

import exception.BusinessException;
import model.Customer;
import repository.CustomerRepository;
import utils.DatabaseConnection;

public class CustomerService {

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$"); // Regex kiểm tra email

	private final CustomerRepository customerRepository;

	public CustomerService() {
		this.customerRepository = new CustomerRepository();
	}

	private static CustomerService instance;

	public static CustomerService getInstance() {
		if (instance == null) {
			instance = new CustomerService();
		}
		return instance;
	}

	// Kiểm tra dữ liệu khách hàng trước khi thêm hoặc cập nhật
	// Cập nhật method validateCustomer trong CustomerService.java
	private void validateCustomer(Customer customer) {
		if (customer == null) {
			throw new BusinessException("Khách hàng không được null.");
		}

		// Kiểm tra tên
		if (customer.getFullName() == null || customer.getFullName().trim().isEmpty()) {
			throw new BusinessException("Tên khách hàng không được để trống.");
		}
		if (customer.getFullName().length() > 100) {
			throw new BusinessException("Tên khách hàng không được quá 100 ký tự.");
		}
		if (!customer.getFullName().matches("^[a-zA-ZÀ-ỹ\\s]+$")) {
			throw new BusinessException("Tên khách hàng chỉ được chứa chữ cái và khoảng trắng.");
		}

		// Kiểm tra số điện thoại
		if (customer.getPhone() == null || !customer.getPhone().matches("^0\\d{9}$")) {
			throw new BusinessException("Số điện thoại phải bắt đầu bằng 0 và có đúng 10 chữ số.");
		}

		// Kiểm tra địa chỉ
		if (customer.getAddress() == null || customer.getAddress().trim().isEmpty()) {
			throw new BusinessException("Địa chỉ không được để trống.");
		}
		if (customer.getAddress().length() > 200) {
			throw new BusinessException("Địa chỉ không được quá 200 ký tự.");
		}

		// Kiểm tra email
		if (customer.getEmail() == null || !EMAIL_PATTERN.matcher(customer.getEmail()).matches()) {
			throw new BusinessException("Email không hợp lệ!");
		}
		if (customer.getEmail().length() > 100) {
			throw new BusinessException("Email không được quá 100 ký tự.");
		}

		// Kiểm tra giới tính
		if (customer.getGender() == null) {
			throw new BusinessException("Giới tính không được để trống.");
		}

		// Kiểm tra điểm (nếu được cập nhật)
		if (customer.getPoint() < 0) {
			throw new BusinessException("Điểm không thể âm.");
		}
	}

	// Thêm khách hàng mới
	public void addCustomer(Customer customer) {
		validateCustomer(customer);
		int result = customerRepository.insert(customer);
		if (result == 0) {
			throw new BusinessException("Không thể thêm khách hàng.");
		}
	}

	// Cập nhật thông tin khách hàng
	public void updateCustomer(Customer customer) {
		validateCustomer(customer);
		int result = customerRepository.update(customer);
		if (result == 0) {
			throw new BusinessException("Không thể cập nhật khách hàng.");
		}
	}

	// Xóa khách hàng theo ID
	public void deleteCustomer(int customerID) {
		Customer customer = customerRepository.selectById(customerID);
		if (customer == null) {
			throw new BusinessException("Không tìm thấy khách hàng với ID: " + customerID);
		}
		customerRepository.delete(customer);
	}

	// Lấy danh sách tất cả khách hàng
	public List<Customer> getAllCustomers() {
		List<Customer> customers = customerRepository.selectAll();
		if (customers.isEmpty()) {
			throw new BusinessException("Không có khách hàng nào trong hệ thống.");
		}
		return customers;
	}

	// Tìm khách hàng theo ID
	public Customer getCustomerById(int customerId) throws SQLException {
		Customer customer = customerRepository.selectById(customerId);
		if (customer == null) {
			throw new BusinessException("Không tìm thấy khách hàng với ID: " + customerId);
		}
		return customer;
	}

	// Tìm khách hàng theo số điện thoại
	public Customer findCustomerByPhoneNumber(String phoneNumber) {
		List<Customer> customers = customerRepository.selectByCondition("phone = ?", phoneNumber);
		if (customers.isEmpty()) {
			throw new BusinessException("Không tìm thấy khách hàng với số điện thoại: " + phoneNumber);
		}
		return customers.get(0);
	}

	// Xóa toàn bộ khách hàng (kèm reset AUTO_INCREMENT)
	public void deleteAllCustomers() {
		try (Connection conn = DatabaseConnection.getConnection()) {
			conn.setAutoCommit(false);

			customerRepository.deleteAll(conn);
			customerRepository.resetAutoIncrement(conn);

			conn.commit();
		} catch (SQLException e) {
			throw new BusinessException("Lỗi khi xóa tất cả khách hàng: " + e.getMessage(), e);
		}
	}

	public int getOrderCountByCustomerId(int customerId) {
		Customer customer = customerRepository.selectById(customerId);
		if (customer == null) {
			throw new BusinessException("Không tìm thấy khách hàng với ID: " + customerId);
		}
		return customerRepository.getOrderCountByCustomerId(customerId);
	}

}

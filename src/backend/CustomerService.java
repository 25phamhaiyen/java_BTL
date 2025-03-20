package backend;

import dao.CustomerDAO;
import entity.Customer;
import utils.DBUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class CustomerService {
    private CustomerDAO customerDAO;

    public CustomerService() {
        this.customerDAO = CustomerDAO.getInstance();
    }

    public boolean validateCustomer(Customer customer) {
        if (customer.getLastName().isEmpty() || customer.getFirstName().isEmpty()) {
            System.out.println("Tên khách hàng không hợp lệ.");
            return false;
        }
        if (!customer.getPhoneNumber().matches("\\d{10}")) {
            System.out.println("Số điện thoại phải có đúng 10 số.");
            return false;
        }
        if (!customer.getCitizenNumber().matches("\\d{12}")) {
            System.out.println("CMND/CCCD phải có đúng 12 số.");
            return false;
        }
        if (customer.getAddress().isEmpty()) {
            System.out.println("Địa chỉ không được để trống.");
            return false;
        }
        return true;
    }

    public int addCustomer(Customer customer) {
        if (!validateCustomer(customer)) {
            return 0;
        }
        return customerDAO.insert(customer);
    }

    public int updateCustomer(Customer customer) {
        if (!validateCustomer(customer)) {
            return 0;
        }
        return customerDAO.update(customer);
    }

    public int deleteCustomer(Customer customer) {
        return customerDAO.delete(customer);
    }

    public ArrayList<Customer> getAllCustomers() {
        return customerDAO.selectAll();
    }

    public Customer getCustomerById(Customer customer) {
        return customerDAO.selectById(customer);
    }
}

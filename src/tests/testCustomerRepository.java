package tests;

import java.util.List;

import enums.GenderEnum;
import model.Account;
import model.Customer;
import repository.AccountRepository;
import repository.CustomerRepository;

public class testCustomerRepository {
	public static void main(String[] args) {
//		
		CustomerRepository customerRepository = CustomerRepository.getInstance();
		
		
		AccountRepository accountRepository = AccountRepository.getInstance();
		Account account = new Account();
		account = accountRepository.getAccountByUsername("user123");
        // 1️⃣ Thêm khách hàng mới (INSERT)
        Customer newCustomer = new Customer(0, "Nguyễn", "Thiện", "0321654988", 
                                            GenderEnum.MALE, "012345688932", 
                                            "Hà Nội", account);
        int insertResult = customerRepository.insert(newCustomer);
        System.out.println("Insert Result: " + insertResult);

        // 2️⃣ Lấy danh sách tất cả khách hàng (SELECT ALL)
        List<Customer> customers = customerRepository.selectAll();
        System.out.println("Danh sách khách hàng:");
        for (Customer c : customers) {
            System.out.println(c);
        }

        // 3️⃣ Cập nhật thông tin khách hàng (UPDATE)
        if (!customers.isEmpty()) {
            Customer updateCustomer = customers.get(0); // Lấy khách hàng đầu tiên
            updateCustomer.setAddress("TP. Hồ Chí Minh");
            updateCustomer.setPhoneNumber("0999888777");

            int updateResult = customerRepository.update(updateCustomer);
            System.out.println("Update Result: " + updateResult);
        }

        // 4️⃣ Tìm khách hàng theo ID (SELECT BY ID)
        if (!customers.isEmpty()) {
            Customer foundCustomer = customerRepository.selectById(customers.get(0).getCustomerID());
            System.out.println("Khách hàng tìm thấy: " + foundCustomer);
        }

        // 5️⃣ Tìm khách hàng theo điều kiện (SELECT BY CONDITION)
        List<Customer> maleCustomers = customerRepository.selectByCondition("sex = ?", GenderEnum.MALE.getCode());
        System.out.println("Danh sách khách hàng nam:");
        for (Customer c : maleCustomers) {
            System.out.println(c);
        }

//         6️⃣ Xóa khách hàng (DELETE)
//        if (!customers.isEmpty()) {
//            Customer deleteCustomer = customers.get(customers.size() - 1); // Xóa khách hàng cuối cùng
//            int deleteResult = customerDAO.delete(deleteCustomer);
//            System.out.println("Delete Result: " + deleteResult);
//        }
        
	}
}

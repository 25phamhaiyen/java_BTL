package test;

import java.util.ArrayList;

import dao.CustomerDAO;
import entity.Customer;

public class testCustomerDAO {
	public static void main(String[] args) {
//		Customer c1 =new Customer( 5 , "Nguyễn", "Thiện", "0321654988", 1, "012345688932", "");
		
//		CustomerDAO.getInstance().insert(c1);
//		CustomerDAO.getInstance().update(c1);
//		CustomerDAO.getInstance().delete(c1);
		
//		ArrayList<Customer> list = CustomerDAO.getInstance().selectAll();
//		for(Customer cus : list ) {
//			System.out.println(cus.toString());
//		}
		
//		Customer c2 = new Customer();
//		c2.setCustomer_ID(1);
//		Customer c3 = CustomerDAO.getInstance().selectById(c2);
//		System.out.println(c3.toString());
		
		ArrayList<Customer> list2 = CustomerDAO.getInstance().selectByCondition("sex=1");
		for(Customer cus : list2 ) {
			System.out.println(cus.toString());
		}
	}
}

package test;

import dao.CustomerDAO;
import entity.Customer;

public class testCustomerDAO {
	public static void main(String[] args) {
		Customer c1 =new Customer( 5 , "Nguyễn", "Thiện", "0321654988", 1, "012345688932", "");
		
//		CustomerDAO.getInstance().insert(c1);
//		CustomerDAO.getInstance().update(c1);
		CustomerDAO.getInstance().delete(c1);
		
	}
}

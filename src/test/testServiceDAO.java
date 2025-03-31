package test;

import java.util.List;

import Enum.TypeServiceEnum;
import dao.ServiceDAO;
import entity.Service;

public class testServiceDAO {
	public static void main(String[] args) {
		ServiceDAO dao = ServiceDAO.getInstance();

		// Thêm dịch vụ VIP
		Service vipService = new Service(0, "Dịch vụ VIP", 500.0, TypeServiceEnum.VIP, "Dịch vụ cao cấp");
		int vipInsertResult = dao.insert(vipService);
		System.out.println("Insert VIP result: " + vipInsertResult);
		System.out.println("VIP Service ID: " + vipService.getServiceID());

		// Thêm dịch vụ BASIC
		Service basicService = new Service(0, "Dịch vụ Cơ bản", 200.0, TypeServiceEnum.BASIC, "Dịch vụ phổ thông");
		int basicInsertResult = dao.insert(basicService);
		System.out.println("Insert BASIC result: " + basicInsertResult);
		System.out.println("BASIC Service ID: " + basicService.getServiceID());

		// Tìm kiếm dịch vụ theo ID
		Service foundVip = dao.selectById(vipService.getServiceID());
		Service foundBasic = dao.selectById(basicService.getServiceID());

		System.out.println("Found VIP Service: " + foundVip);
		System.out.println("Found BASIC Service: " + foundBasic);

		// Cập nhật giá dịch vụ VIP
		if (foundVip != null) {
			foundVip.setCostPrice(550.0);
			int updateResult = dao.update(foundVip);
			System.out.println("Update VIP result: " + updateResult);
		}

		// Lấy toàn bộ danh sách dịch vụ
		List<Service> services = dao.selectAll();
		System.out.println("All Services:");
		for (Service service : services) {
			System.out.println(service);
		}

		// ❌ Xóa dịch vụ BASIC
		if (foundBasic != null) {
			int deleteResult = dao.delete(foundBasic);
			System.out.println("Delete BASIC result: " + deleteResult);
		}

		// Kiểm tra danh sách sau khi xóa
		services = dao.selectAll();
		System.out.println("All Services after delete:");
		for (Service service : services) {
			System.out.println(service);
		}
	}
}

package test;

import java.util.List;

import Enum.TypeServiceEnum;
import dao.TypeServiceDAO;
import entity.TypeService;

public class testTypeServiceDAO {
	public static void main(String[] args) {
		TypeServiceDAO dao = TypeServiceDAO.getInstance();

		// 🔹 Thêm loại dịch vụ VIP
		TypeService vipService = new TypeService(0, TypeServiceEnum.VIP);
		int vipInsertResult = dao.insert(vipService);
		System.out.println("Insert VIP result: " + vipInsertResult);
		System.out.println("VIP TypeService ID: " + vipService.getTypeServiceID());

		// 🔹 Thêm loại dịch vụ BASIC
		TypeService basicService = new TypeService(0, TypeServiceEnum.BASIC);
		int basicInsertResult = dao.insert(basicService);
		System.out.println("Insert BASIC result: " + basicInsertResult);
		System.out.println("BASIC TypeService ID: " + basicService.getTypeServiceID());

		// 🔍 Tìm kiếm loại dịch vụ theo ID (1 và 2)
		TypeService foundVip = dao.selectById(1);
		TypeService foundBasic = dao.selectById(2);

		System.out.println("Found VIP TypeService: " + foundVip);
		System.out.println("Found BASIC TypeService: " + foundBasic);

		// 📜 Lấy toàn bộ danh sách loại dịch vụ
		List<TypeService> services = dao.selectAll();
		System.out.println("All TypeServices:");
		for (TypeService service : services) {
			System.out.println(service);
		}
	}
}

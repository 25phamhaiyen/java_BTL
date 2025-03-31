package test;

import java.util.List;

import Enum.PaymentStatusEnum;
import dao.PaymentStatusDAO;
import entity.PaymentStatus;

public class testPaymentStatusDAO {
	public static void main(String[] args) {
		PaymentStatusDAO dao = PaymentStatusDAO.getInstance();

		// Thêm trạng thái thanh toán PENDING
		PaymentStatus pendingStatus = new PaymentStatus(0, PaymentStatusEnum.PENDING);
		int insertPending = dao.insert(pendingStatus);
		System.out.println("Insert PENDING result: " + insertPending);
		System.out.println("PENDING ID: " + pendingStatus.getPaymentStatusID());

		// Thêm trạng thái thanh toán PAID
		PaymentStatus paidStatus = new PaymentStatus(0, PaymentStatusEnum.PAID);
		int insertPaid = dao.insert(paidStatus);
		System.out.println("Insert PAID result: " + insertPaid);
		System.out.println("PAID ID: " + paidStatus.getPaymentStatusID());

		// Tìm kiếm trạng thái theo ID
		PaymentStatus foundStatus = dao.selectById(pendingStatus.getPaymentStatusID());
		System.out.println("Found PENDING Status: " + foundStatus);

		// Cập nhật trạng thái từ PENDING -> FAILED
		if (foundStatus != null) {
			foundStatus.setStatus(PaymentStatusEnum.FAILED);
			int updateResult = dao.update(foundStatus);
			System.out.println("Update PENDING -> FAILED result: " + updateResult);
		}

		// Lấy toàn bộ danh sách trạng thái thanh toán
		List<PaymentStatus> allStatuses = dao.selectAll();
		System.out.println("All Payment Statuses:");
		for (PaymentStatus status : allStatuses) {
			System.out.println(status);
		}

		// Xóa trạng thái PAID
		if (paidStatus != null) {
			int deleteResult = dao.delete(paidStatus);
			System.out.println("Delete PAID result: " + deleteResult);
		}

		// Kiểm tra danh sách sau khi xóa
		allStatuses = dao.selectAll();
		System.out.println("All Payment Statuses after delete:");
		for (PaymentStatus status : allStatuses) {
			System.out.println(status);
		}
	}
}

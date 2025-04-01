package tests;

import java.util.List;

import model.TypePet;
import repository.TypePetRepository;

public class testTypePetRepository {
	public static void main(String[] args) {
		TypePetRepository typePetRepository = TypePetRepository.getInstance();

		// --- Test Insert ---
		System.out.println("--- Test Insert ---");
		TypePet newType = new TypePet(0, "Thú cưng mới");
		int insertResult = typePetRepository.insert(newType);
		System.out.println("Thêm thành công? " + (insertResult > 0) + ", ID: " + newType.getTypePetID());

		// --- Test Select All ---
		System.out.println("\n--- Test Select All ---");
		List<TypePet> typePetList = typePetRepository.selectAll();
		for (TypePet tp : typePetList) {
			System.out.println(tp);
		}

		// --- Test Select By ID ---
		System.out.println("\n--- Test Select By ID ---");
		TypePet foundType = typePetRepository.selectById(newType.getTypePetID());
		System.out.println(foundType != null ? foundType : "Không tìm thấy loại thú cưng");

		// --- Test Update ---
		System.out.println("\n--- Test Update ---");
		if (foundType != null) {
			foundType.setTypeName("Tên thú cưng mới cập nhật");
			int updateResult = typePetRepository.update(foundType);
			System.out.println("Cập nhật thành công? " + (updateResult > 0));

			// Kiểm tra lại thông tin
			TypePet updatedType = typePetRepository.selectById(foundType.getTypePetID());
			System.out.println(updatedType);
		}

		// --- Test Delete ---
		System.out.println("\n--- Test Delete ---");
		int deleteResult = typePetRepository.delete(newType);
		System.out.println("Xóa thành công? " + (deleteResult > 0));

		// Kiểm tra sau khi xóa
		TypePet checkDeleted = typePetRepository.selectById(newType.getTypePetID());
		System.out.println(checkDeleted == null ? "Đã xóa thành công" : "Xóa thất bại");
	}
}

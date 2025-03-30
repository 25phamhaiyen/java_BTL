package test;

import java.util.List;

import Enum.GenderEnum;
import dao.PetDAO;
import entity.Customer;
import entity.Pet;
import entity.TypePet;

public class testPetDAO {
	public static void main(String[] args) {
		PetDAO petDAO = PetDAO.getInstance();

		// Test thêm mới Pet
		System.out.println("--- Test Insert ---");
		Customer customer = new Customer(1, "Nguyen", "Tuan", "0123456789", GenderEnum.MALE, "123456789", "Hanoi",
				null);
		TypePet typePet = new TypePet(1, "Dog");
		Pet pet = new Pet(101, "Lucky", 3, customer, typePet);
		petDAO.insert(pet);

		// Test cập nhật Pet
		System.out.println("--- Test Update ---");
		pet.setPetName("Lucky Updated");
		petDAO.update(pet);

		// Test lấy tất cả Pet
		System.out.println("--- Test Select All ---");
		List<Pet> petList = petDAO.selectAll();
		for (Pet p : petList) {
			System.out.println(p);
		}

		// Test lấy Pet theo ID
		System.out.println("--- Test Select By ID ---");
		Pet foundPet = petDAO.selectById(new Pet(1010, null, 0, null, null));
		System.out.println(foundPet);

		// Test xóa Pet
		System.out.println("--- Test Delete ---");
		petDAO.delete(pet);
	}
}

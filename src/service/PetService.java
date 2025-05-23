package service;

import model.Customer;
import model.Pet;
import model.PetType;
import repository.PetRepository;
import repository.PetTypeRepository;

import java.time.LocalDate; 
import java.util.List;

import enums.GenderEnum;

public class PetService {

	private PetRepository petRepository;

	public PetService() {
		petRepository = PetRepository.getInstance();
	}

	// Validation method
	private void validatePet(Pet pet) {
		if (pet == null) {
			throw new IllegalArgumentException("Thú cưng không được null.");
		}

		// Kiểm tra tên thú cưng
		if (pet.getName() == null || pet.getName().trim().isEmpty()) {
			throw new IllegalArgumentException("Tên thú cưng không được để trống.");
		}
		if (pet.getName().length() > 50) {
			throw new IllegalArgumentException("Tên thú cưng không được quá 50 ký tự.");
		}
		if (!pet.getName().matches("^[a-zA-ZÀ-ỹ0-9\\s]+$")) {
			throw new IllegalArgumentException("Tên thú cưng chỉ được chứa chữ cái, số và khoảng trắng.");
		}

		// Kiểm tra loại thú cưng
		if (pet.getTypePet() == null || pet.getTypePet().getTypePetID() <= 0) {
			throw new IllegalArgumentException("Loại thú cưng không hợp lệ.");
		}

		// Kiểm tra giới tính
		if (pet.getGender() == null) {
			throw new IllegalArgumentException("Giới tính thú cưng không được để trống.");
		}

		// Kiểm tra ngày sinh
		if (pet.getDob() == null) {
			throw new IllegalArgumentException("Ngày sinh thú cưng không được để trống.");
		}
		if (pet.getDob().isAfter(LocalDate.now())) {
			throw new IllegalArgumentException("Ngày sinh không thể là ngày trong tương lai.");
		}
		if (pet.getDob().isBefore(LocalDate.of(1900, 1, 1))) {
			throw new IllegalArgumentException("Ngày sinh không hợp lệ (quá xa).");
		}

		// Kiểm tra cân nặng
		if (pet.getWeight() <= 0) {
			throw new IllegalArgumentException("Cân nặng phải lớn hơn 0.");
		}
		if (pet.getWeight() > 200) {
			throw new IllegalArgumentException("Cân nặng không hợp lệ (quá lớn).");
		}

		// Kiểm tra chủ sở hữu
		if (pet.getOwner() == null || pet.getOwner().getId() <= 0) {
			throw new IllegalArgumentException("Chủ sở hữu không hợp lệ.");
		}

		// Kiểm tra ghi chú (nếu có)
		if (pet.getNote() != null && pet.getNote().length() > 500) {
			throw new IllegalArgumentException("Ghi chú không được quá 500 ký tự.");
		}
	}

	// Thêm thú cưng
	public int addPet(Pet pet) {
		// Kiểm tra dữ liệu trước khi thêm
		validatePet(pet);

		// Thực hiện thêm thú cưng vào DB thông qua PetRepository
		return petRepository.insert(pet);
	}

	// Cập nhật thông tin thú cưng (chỉ 1 method này)
	public int updatePet(Pet pet) {
		// Kiểm tra dữ liệu trước khi cập nhật
		if (pet == null || pet.getPetId() <= 0) {
			throw new IllegalArgumentException("ID thú cưng không hợp lệ.");
		}

		validatePet(pet);

		// Thực hiện cập nhật thú cưng vào DB thông qua PetRepository
		return petRepository.update(pet);
	}

	// Xóa thú cưng
	public int deletePet(int petID) {
		if (petID <= 0) {
			System.out.println("Dữ liệu không hợp lệ.");
			return 0;
		}
		// Tạo đối tượng Pet tạm để gọi phương thức delete
		Pet pet = new Pet();
		pet.setPetId(petID);
		return petRepository.delete(pet);
	}

	// Lấy tất cả thú cưng
	public List<Pet> getAllPets() {
		return petRepository.selectAll();
	}

	// Lấy thú cưng theo ID
	public Pet getPetById(int petID) {
		Pet pet = new Pet();
		pet.setPetId(petID);
		return petRepository.selectById(pet);
	}

	// Lấy thú cưng theo điều kiện
	public List<Pet> getPetsByCondition(String condition, Object... params) {
		return petRepository.selectByCondition(condition, params);
	}

	public String getPetNamesByCustomerId(int customerId) {
		List<String> petNames = petRepository.getPetNamesByCustomerId(customerId);
		return String.join(", ", petNames);
	}

	public List<PetType> getAllPetTypes() {
		PetTypeRepository petTypeRepo = new PetTypeRepository();
		return petTypeRepo.selectAll();
	}

	// Sửa phương thức này để sử dụng getPetsByCustomerId
	public Pet getPetByCustomerId(int customerId) {
		// Lấy danh sách thú cưng của khách hàng
		List<Pet> pets = petRepository.getPetsByCustomerId(customerId);
		// Trả về thú cưng đầu tiên nếu có
		if (pets != null && !pets.isEmpty()) {
			return pets.get(0);
		}
		// Trả về null nếu không có thú cưng nào
		return null;
	}

	public List<Pet> findPetsByCustomerId(int customerId) {
        return petRepository.getPetsByCustomerId(customerId);
    }
}
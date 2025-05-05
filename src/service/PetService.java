package service;

import model.Customer;
import model.Pet;
import model.PetType;
import repository.PetRepository;
import repository.PetTypeRepository;
import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import enums.GenderEnum;
import javafx.util.Callback;

public class PetService {

    private PetRepository petRepository;

    public PetService() {
        petRepository = PetRepository.getInstance();
    }

    // Thêm thú cưng
    public int addPet(Pet pet) {
        // Kiểm tra dữ liệu trước khi thêm
        if (pet == null || pet.getName().isEmpty() || pet.getWeight() <= 0) {
            System.out.println("Dữ liệu không hợp lệ.");
            return 0;
        }
        // Thực hiện thêm thú cưng vào DB thông qua PetRepository
        return petRepository.insert(pet);
    }

    // Cập nhật thông tin thú cưng
    public int updatePet(Pet pet) {
        // Kiểm tra dữ liệu trước khi cập nhật
        if (pet == null || pet.getPetId()<= 0 || pet.getName().isEmpty()) {
            System.out.println("Dữ liệu không hợp lệ.");
            return 0;
        }
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
    
 // Trong PetService.java
    public List<PetType> getAllPetTypes() {
        PetTypeRepository petTypeRepo = new PetTypeRepository();
        return petTypeRepo.selectAll(); 
    }
    public List<Pet> findPetsByCustomerId(int customerId) {
        return petRepository.getPetsByCustomerId(customerId);
    }


}

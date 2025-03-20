package Enum;

public enum TypePetEnum {
	HAMSTER(1, "Chuột Hamster"), HEDGEHOG(2, "Nhím kiểng"), SUGAR_GLIDER(3, "Sóc bay Úc"), RABBIT(4, "Thỏ"),
	SQUIRREL_MONKEY(5, "Khỉ đuôi sóc"), AUSTRALIAN_DRAGON(6, "Rồng Australia"), CAPYBARA(7, "Chuột lang nước"),
	DOG(8, "Chó"), CAT(9, "Mèo"), OTHER(99, "Other");

	private final int id;
	private final String name;

	TypePetEnum(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public static TypePetEnum fromId(int id) {
		for (TypePetEnum type : TypePetEnum.values()) {
			if (type.getId() == id) {
				return type;
			}
		}
		return OTHER;
	}
	@Override
    public String toString() {
        return name;
    }
}

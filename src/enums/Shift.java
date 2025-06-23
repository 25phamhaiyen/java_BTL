package enums;


public enum Shift {

	MORNING(0), AFTERNOON(1), EVENING(2), NOSHIFT(3);

	private final int code;

	Shift(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static Shift fromCode(int code) {
		for (Shift shift : Shift.values()) {
			if (shift.getCode() == code) {
				return shift;
			}
		}
		throw new IllegalArgumentException("Invalid shift code: " + code);
	}
	@Override
	public String toString() {
	    return switch (this) {
	        case MORNING -> "Ca sáng";
	        case AFTERNOON -> "Ca chiều";
	        case EVENING -> "Ca tối";
	        case NOSHIFT -> "Không có ca";
	    };
	}


}
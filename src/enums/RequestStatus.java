package enums;

public enum RequestStatus {
	PENDING("Chờ phê duyệt"), APPROVED("Đã chấp thuận"), REJECTED("Đã từ chối");

	private final String description;

	RequestStatus(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
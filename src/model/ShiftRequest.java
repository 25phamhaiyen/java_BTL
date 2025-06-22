package model;

import java.time.LocalDate;

import enums.RequestStatus;
import enums.RequestType;
import enums.Shift;

public class ShiftRequest {
	private int id;
    private Staff staff;
    private LocalDate requestDate;
    private Shift shift;
    private RequestType type;
    private RequestStatus status;
    private String reason;
    
	public ShiftRequest(int id, Staff staff, LocalDate requestDate, Shift shift, RequestType type, RequestStatus status,
			String reason) {
		super();
		this.id = id;
		this.staff = staff;
		this.requestDate = requestDate;
		this.shift = shift;
		this.type = type;
		this.status = status;
		this.reason = reason;
	}

	public ShiftRequest() {
		super();
	}
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Staff getStaff() {
		return staff;
	}

	public void setStaff(Staff staff) {
		this.staff = staff;
	}

	public LocalDate getRequestDate() {
		return requestDate;
	}

	public void setRequestDate(LocalDate requestDate) {
		this.requestDate = requestDate;
	}

	public Shift getShift() {
		return shift;
	}

	public void setShift(Shift shift) {
		this.shift = shift;
	}

	public RequestType getType() {
		return type;
	}

	public void setType(RequestType type) {
		this.type = type;
	}

	public RequestStatus getStatus() {
		return status;
	}

	public void setStatus(RequestStatus status) {
		this.status = status;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	@Override
	public String toString() {
		return "ShiftRequest [id=" + id + ", staff=" + staff + ", requestDate=" + requestDate + ", shift=" + shift
				+ ", type=" + type + ", status=" + status + ", reason=" + reason + "]";
	}
    
}

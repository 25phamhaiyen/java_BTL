package model;

import java.sql.Timestamp;
import java.time.LocalDate;
import enums.RequestStatus;

public class LeaveRequest {
	private int leaveRequestId;
	private Staff staff;
	private LocalDate leaveDate;
	private String reason;
	private RequestStatus status;
	private Staff approvedBy;
	private Timestamp requestDate;
	private Timestamp responseDate;
	private String note;

	public LeaveRequest() {
	}

	public LeaveRequest(int leaveRequestId, Staff staff, LocalDate leaveDate, String reason, RequestStatus status,
			Staff approvedBy, Timestamp requestDate, Timestamp responseDate, String note) {
		this.leaveRequestId = leaveRequestId;
		this.staff = staff;
		this.leaveDate = leaveDate;
		this.reason = reason;
		this.status = status;
		this.approvedBy = approvedBy;
		this.requestDate = requestDate;
		this.responseDate = responseDate;
		this.note = note;
	}

	public int getLeaveRequestId() {
		return leaveRequestId;
	}

	public void setLeaveRequestId(int leaveRequestId) {
		this.leaveRequestId = leaveRequestId;
	}

	public Staff getStaff() {
		return staff;
	}

	public void setStaff(Staff staff) {
		this.staff = staff;
	}

	public LocalDate getLeaveDate() {
		return leaveDate;
	}

	public void setLeaveDate(LocalDate leaveDate) {
		this.leaveDate = leaveDate;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public RequestStatus getStatus() {
		return status;
	}

	public void setStatus(RequestStatus status) {
		this.status = status;
	}

	public Staff getApprovedBy() {
		return approvedBy;
	}

	public void setApprovedBy(Staff approvedBy) {
		this.approvedBy = approvedBy;
	}

	public Timestamp getRequestDate() {
		return requestDate;
	}

	public void setRequestDate(Timestamp requestDate) {
		this.requestDate = requestDate;
	}

	public Timestamp getResponseDate() {
		return responseDate;
	}

	public void setResponseDate(Timestamp responseDate) {
		this.responseDate = responseDate;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@Override
	public String toString() {
		return "LeaveRequest{" + "leaveRequestId=" + leaveRequestId + ", staff="
				+ (staff != null ? staff.getFullName() : "null") + ", leaveDate=" + leaveDate + ", reason='" + reason
				+ '\'' + ", status=" + status + ", approvedBy="
				+ (approvedBy != null ? approvedBy.getFullName() : "null") + ", requestDate=" + requestDate
				+ ", responseDate=" + responseDate + ", note='" + note + '\'' + '}';
	}
}
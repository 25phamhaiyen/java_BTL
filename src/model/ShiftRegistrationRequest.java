package model;

import java.sql.Timestamp;
import java.time.LocalDate;
import enums.Shift;
import enums.RequestStatus;

public class ShiftRegistrationRequest {
    private int requestId;
    private Staff staff;
    private LocalDate workDate;
    private Shift shift;
    private String location;
    private String note;
    private RequestStatus status;
    private Staff approvedBy;
    private Timestamp requestDate;
    private Timestamp responseDate;

    public ShiftRegistrationRequest() {}

    public ShiftRegistrationRequest(int requestId, Staff staff, LocalDate workDate, Shift shift,
                                   String location, String note, RequestStatus status, Staff approvedBy,
                                   Timestamp requestDate, Timestamp responseDate) {
        this.requestId = requestId;
        this.staff = staff;
        this.workDate = workDate;
        this.shift = shift;
        this.location = location;
        this.note = note;
        this.status = status;
        this.approvedBy = approvedBy;
        this.requestDate = requestDate;
        this.responseDate = responseDate;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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

    @Override
    public String toString() {
        return "ShiftRegistrationRequest{" +
                "requestId=" + requestId +
                ", staff=" + (staff != null ? staff.getFullName() : "null") +
                ", workDate=" + workDate +
                ", shift=" + shift +
                ", location='" + location + '\'' +
                ", note='" + note + '\'' +
                ", status=" + status +
                ", approvedBy=" + (approvedBy != null ? approvedBy.getFullName() : "null") +
                ", requestDate=" + requestDate +
                ", responseDate=" + responseDate +
                '}';
    }
}
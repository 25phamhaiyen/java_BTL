package model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

import enums.Shift;

public class WorkSchedule {
	private int scheduleID;
	private Staff staff;
	private LocalDate workDate;
	private Shift shift;
	private LocalTime startTime;
	private LocalTime endTime;
	private String location;
	private String task;
	private String note;

	// Default constructor
	public WorkSchedule() {
	}

	// Constructor for use in ScheduleService
	public WorkSchedule(int scheduleID, Staff staff, LocalDate workDate, Shift shift, LocalTime startTime,
			LocalTime endTime, String location, String task, String note) {
		this.scheduleID = scheduleID;
		this.staff = staff;
		this.workDate = workDate;
		this.shift = shift;
		this.startTime = startTime;
		this.endTime = endTime;
		this.location = location;
		this.task = task;
		this.note = note;
	}

	public WorkSchedule(Staff staff, LocalDate workDate, Shift shift, String note) {
		this.staff = staff;
		this.workDate = workDate;
		this.shift = shift;
		this.note = note;
	}

	public WorkSchedule(int scheduleID, Staff staff, LocalDate workDate, Shift shift, String note) {
		this.scheduleID = scheduleID;
		this.staff = staff;
		this.workDate = workDate;
		this.shift = shift;
		this.note = note;
	}

	// Getters and Setters
	public int getScheduleID() {
		return scheduleID;
	}

	public void setScheduleID(int scheduleID) {
		this.scheduleID = scheduleID;
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

	public LocalTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		WorkSchedule that = (WorkSchedule) o;
		return scheduleID == that.scheduleID && Objects.equals(staff, that.staff)
				&& Objects.equals(workDate, that.workDate) && shift == that.shift
				&& Objects.equals(startTime, that.startTime) && Objects.equals(endTime, that.endTime)
				&& Objects.equals(location, that.location) && Objects.equals(task, that.task)
				&& Objects.equals(note, that.note);
	}

	@Override
	public int hashCode() {
		return Objects.hash(scheduleID, staff, workDate, shift, startTime, endTime, location, task, note);
	}

	@Override
	public String toString() {
		return "WorkSchedule{" + "scheduleID=" + scheduleID + ", staff=" + (staff != null ? staff.getId() : null)
				+ ", workDate=" + workDate + ", shift=" + shift + ", startTime=" + startTime + ", endTime=" + endTime
				+ ", location='" + location + '\'' + ", task='" + task + '\'' + ", note='" + note + '\'' + '}';
	}
}
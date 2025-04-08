package model;

import java.util.Date;

public class WorkSchedule {
    private int scheduleID;
    private int staffID;
    private Date workDate;
    private String shift;
    private String note;

    public WorkSchedule(int scheduleID, int staffID, Date workDate, String shift, String note) {
        this.scheduleID = scheduleID;
        this.staffID = staffID;
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

    public int getStaffID() {
        return staffID;
    }

    public void setStaffID(int staffID) {
        this.staffID = staffID;
    }

    public Date getWorkDate() {
        return workDate;
    }

    public void setWorkDate(Date workDate) {
        this.workDate = workDate;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

package entity;

import Enum.StatusCode;

public class HappenStatus {
    private int happenStatusID;
    private StatusCode statusCode;
    private String statusName;

    public HappenStatus() {}

    public HappenStatus(int happenStatusID, StatusCode statusCode, String statusName) {
        this.happenStatusID = happenStatusID;
        this.statusCode = statusCode;
        this.statusName = statusName;
    }

    public int getHappenStatusID() { return happenStatusID; }
    public void setHappenStatusID(int happenStatusID) { this.happenStatusID = happenStatusID; }

    public StatusCode getStatusCode() { return statusCode; }
    public void setStatusCode(StatusCode statusCode) { this.statusCode = statusCode; }

    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }

    @Override
    public String toString() {
        return "HappenStatus: ID: " + happenStatusID +
                "\n\t\t\tStatus Code: " + statusCode +
                "\n\t\t\tStatus Name: " + statusName;
    }
}

package entity;

import java.util.Date;

public class HappenStatus {
	private int happenStatusID;
	private int statusCode;
	private String statusName;
	public HappenStatus() {
		super();
	}
	public HappenStatus(int happenStatusID, int statusCode, String statusName) {
		super();
		this.happenStatusID = happenStatusID;
		this.statusCode = statusCode;
		this.statusName = statusName;
	}
	public int getHappenStatusID() {
		return happenStatusID;
	}
	public void setHappenStatusID(int happenStatusID) {
		this.happenStatusID = happenStatusID;
	}
	public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	public String getStatusName() {
		return statusName;
	}
	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}
	@Override
	public String toString() {
		return "HappenStatus: ID: " + happenStatusID + "\n\t\t\tStatus Code: " + statusCode + "\\n\\t\\t\\tStatus Name: "
				+ statusName;
	}
	
}

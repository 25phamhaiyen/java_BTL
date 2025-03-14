package entity;

public class Service {
	private int serviceID;
	private String serviceName;
	private double costPrice;
	private int typeSeviceID;
	private String description ; // mo ta
	public Service() {
		super();
	}
	public Service(int serviceID, String serviceName, double costPrice, int typeSeviceID, String description) {
		super();
		this.serviceID = serviceID;
		this.serviceName = serviceName;
		this.costPrice = costPrice;
		this.typeSeviceID = typeSeviceID;
		this.description = description;
	}
	public int getServiceID() {
		return serviceID;
	}
	public void setServiceID(int serviceID) {
		this.serviceID = serviceID;
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public double getCostPrice() {
		return costPrice;
	}
	public void setCostPrice(double costPrice) {
		this.costPrice = costPrice;
	}
	public int getTypeSeviceID() {
		return typeSeviceID;
	}
	public void setTypeSeviceID(int typeSeviceID) {
		this.typeSeviceID = typeSeviceID;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@Override
	public String toString() {
		return "Service: ID: " + serviceID + "\n\tName: " + serviceName + "\n\tCost Price: " + costPrice
				+ "\n\tType Sevice ID: " + typeSeviceID + "\n\tDescription: " + description;
	}
	
	
}

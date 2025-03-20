package entity;

import Enum.TypeServiceEnum;

public class Service {
    private int serviceID;
    private String serviceName;
    private double costPrice;
    private TypeServiceEnum typeService; 
    private String description;

    public Service() {
        super();
    }

    public Service(int serviceID, String serviceName, double costPrice, TypeServiceEnum typeService, String description) {
        super();
        this.serviceID = serviceID;
        this.serviceName = serviceName;
        this.costPrice = costPrice;
        this.typeService = typeService;
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

    public TypeServiceEnum getTypeService() {
        return typeService;
    }

    public void setTypeService(TypeServiceEnum typeService) {
        this.typeService = typeService;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Service: ID: " + serviceID +
               "\n\tName: " + serviceName +
               "\n\tCost Price: " + costPrice +
               "\n\tType Service: " + typeService.getDescription() +
               "\n\tDescription: " + description;
    }
}

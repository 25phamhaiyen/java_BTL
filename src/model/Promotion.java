package model;

import java.util.Date;

public class Promotion {
    private int promotionID;
    private String name;
    private String description;
    private int requiredPoints;
    private double discountPercent;
    private Date startDate;
    private Date endDate;
    
    

    public Promotion(int promotionID, String name, String description, int requiredPoints, double discountPercent, Date startDate, Date endDate) {
        this.promotionID = promotionID;
        this.name = name;
        this.description = description;
        this.requiredPoints = requiredPoints;
        this.discountPercent = discountPercent;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public int getPromotionID() {
        return promotionID;
    }

    public void setPromotionID(int promotionID) {
        this.promotionID = promotionID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRequiredPoints() {
        return requiredPoints;
    }

    public void setRequiredPoints(int requiredPoints) {
        this.requiredPoints = requiredPoints;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}

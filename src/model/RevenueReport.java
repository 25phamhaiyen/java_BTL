package model;

public class RevenueReport {
	private String label;
	private double revenue;

	public RevenueReport(String label, double revenue) {
		this.label = label;
		this.revenue = revenue;
	}

	public String getLabel() {
		return label;
	}

	public double getRevenue() {
		return revenue;
	}
}

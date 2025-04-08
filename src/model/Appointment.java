package model;

public class Appointment {
    private String appointmentId;
    private String customerName;
    private String petName;
    private String service;
    private String time;
    private String status;

    public Appointment(String appointmentId, String customerName, String petName, String service, String time, String status) {
        this.appointmentId = appointmentId;
        this.customerName = customerName;
        this.petName = petName;
        this.service = service;
        this.time = time;
        this.status = status;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getPetName() {
        return petName;
    }

    public String getService() {
        return service;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }
}
package model;

import java.time.LocalDate;

import enums.Shift;

public class ShiftAssignment {
    private LocalDate date;
    private Shift shift;

    public ShiftAssignment(LocalDate date, Shift shift) {
        this.date = date;
        this.shift = shift;
    }

    public LocalDate getDate() {
        return date;
    }

    public Shift getShift() {
        return shift;
    }
}

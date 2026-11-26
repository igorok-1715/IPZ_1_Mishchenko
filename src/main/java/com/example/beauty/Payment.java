
package com.example.beauty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Payment {

    private String type;
    private String master;
    private String details;
    private double amount;
    private LocalDateTime dateTime;

    public Payment(String type, String master,
                   String details, double amount,
                   LocalDateTime dateTime) {

        this.type = type;
        this.master = master;
        this.details = details;
        this.amount = amount;
        this.dateTime = dateTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getFormattedDateTime() {
        return dateTime.format(
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        );
    }
}

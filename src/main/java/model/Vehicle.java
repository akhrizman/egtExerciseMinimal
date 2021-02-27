package main.java.model;

import java.math.BigDecimal;

public class Vehicle {

    private int year;
    private String make;
    private String model;
    private BigDecimal msrp;


    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public BigDecimal getMsrp() {
        return msrp;
    }

    public void setMsrp(BigDecimal msrp) {
        this.msrp = msrp;
    }

    public BigDecimal getListPrice(BigDecimal tax) {
        return msrp.multiply(tax);
    }

    public String getMakeModel() {
        return String.format("%s %s", getPrettyMake(), getPrettyModel());
    }

    private String getPrettyMake() {
        return (make != null && !make.isBlank()) ? make.substring(0, 1).toUpperCase() + make.substring(1) : "???";
    }

    private String getPrettyModel() {
        return (model != null && !model.isBlank()) ? model.substring(0, 1).toUpperCase() + model.substring(1) : "???";
    }
}

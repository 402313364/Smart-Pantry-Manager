package com.smartpantry.manager.data;

/**
 * One ingredient the user currently has at home.
 * expiryDate is optional and stored as yyyy-MM-dd, or null when the user skips it.
 */
public class PantryItem {

    private long id;
    private String name;
    private double quantity;
    private String unit;
    private String expiryDate;

    public PantryItem() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getQuantityLabel() {
        return formatQuantity(quantity) + " " + unit;
    }

    public static String formatQuantity(double quantity) {
        if (Math.abs(quantity - Math.rint(quantity)) < 0.001d) {
            return String.valueOf((long) Math.rint(quantity));
        }
        return String.valueOf(quantity);
    }
}

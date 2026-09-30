package com.smartpantry.manager.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PantryItem {

    private long id;
    private String name;
    private double quantity;
    private String unit;
    private String expiryDate; // yyyy-MM-dd or null

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

    // true if the date is already past, or it falls in the next `days` days
    public boolean expiresWithinDays(int days) {
        if (expiryDate == null || expiryDate.isEmpty()) {
            return false;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            format.setLenient(false);
            Date expiry = format.parse(expiryDate);
            if (expiry == null) {
                return false;
            }
            Calendar limit = Calendar.getInstance();
            limit.set(Calendar.HOUR_OF_DAY, 23);
            limit.set(Calendar.MINUTE, 59);
            limit.set(Calendar.SECOND, 59);
            limit.add(Calendar.DAY_OF_MONTH, days);
            return !expiry.after(limit.getTime());
        } catch (ParseException ex) {
            return false;
        }
    }

    public String getQuantityLabel() {
        return formatQuantity(quantity) + " " + unit;
    }

    public static String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }
}

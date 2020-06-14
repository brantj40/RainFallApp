package com.garagedev.rainfallapp;

import androidx.annotation.NonNull;

import java.util.Locale;


// This is a class just to hold data as an object
// this class is package-private
class MonthData {

    private String month;
    private String timeCode;
    private Double rainFall;
    private String classification;

    // no arg constructor
    public MonthData(){}

    public MonthData(String month, String timeCode, Double rainFall) {
        this.month = month;
        this.timeCode = timeCode;
        this.rainFall = rainFall;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getTimeCode() {
        return timeCode;
    }

    public void setTimeCode(String timeCode) {
        this.timeCode = timeCode;
    }

    public Double getRainFall() {
        return rainFall;
    }

    public void setRainFall(Double rainFall) {
        this.rainFall = rainFall;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    @NonNull
    @Override
    public String toString() {

        // COL_x are variables from MainActivity.java class.
        // they are package-private and static so they can be called directly

        String s = String.format(Locale.ENGLISH,
                "%1$"+MainActivity.COL_1+"s %2$"+MainActivity.COL_2+"s %3$"+MainActivity.COL_3+"s %4$"+MainActivity.COL_4+"s", month, timeCode, rainFall, classification );

        System.out.println(s);
        return s;
    }

}

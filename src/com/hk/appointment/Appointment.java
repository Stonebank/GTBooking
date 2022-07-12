package com.hk.appointment;

import java.util.ArrayList;

public class Appointment {

    private final String day;
    private final String month;
    private final String year;

    private final String day_xpath;

    private final ArrayList<String> time_xpath;

    public Appointment(String day, String month, String year, String day_xpath) {
        this.day = day;
        this.month = month;
        this.year = year;
        this.day_xpath = day_xpath;
        this.time_xpath = new ArrayList<>();
        System.out.println(day + " " + month + " " + year);
    }

    public String getDay() {
        return day;
    }

    public String getDay_xpath() {
        return day_xpath;
    }

    public String getMonth() {
        return month;
    }

    public String getYear() {
        return year;
    }

    public ArrayList<String> getTime_xpath() {
        return time_xpath;
    }

}

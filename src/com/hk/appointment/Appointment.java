package com.hk.appointment;

import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;

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

    // TODO: REWRITE METHOD
    public static String findAppointmentTime(Logger logger, String input, ChromeDriver driver, Appointment selected_day, ArrayList<String> time_xpath) {
        for (String xpath : time_xpath) {
            var time_element = driver.findElement(By.xpath(selected_day.getDay_xpath())).findElement(By.xpath(xpath));
            if (input.equalsIgnoreCase(time_element.getText()))
                return xpath;
        }
        logger.error("ERROR! Input was not an available appointment.");
        return null;
    }

    public static Appointment findAppointmentDay(Logger logger, String input, ArrayList<Appointment> days) {
        for (Appointment day : days) {
            if (input.equalsIgnoreCase(day.getDay()) || input.equalsIgnoreCase(day.getDay() + " " + day.getMonth()) || input.equalsIgnoreCase(day.getDay() + " " + day.getMonth() + " " + day.getYear()))
                return day;
        }
        logger.error("ERROR! " + input + " is not an available appointment day.");
        return null;
    }

}

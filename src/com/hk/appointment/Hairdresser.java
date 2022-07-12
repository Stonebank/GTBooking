package com.hk.appointment;

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;

import java.util.ArrayList;

public record Hairdresser(String name, WebElement element) {

    public Hairdresser(String name, WebElement element) {
        this.name = name;
        this.element = element;
        System.out.println(name.substring(0, 1).toUpperCase() + name.substring(1));
    }

    public static Hairdresser findHairDresser(Logger logger, String input, ArrayList<Hairdresser> hairdressers) {
        for (Hairdresser hairdresser : hairdressers) {
            if (input.equalsIgnoreCase(hairdresser.name()))
                return hairdresser;
        }
        logger.error("ERROR! " + input + " is not found as a hairdresser.");
        return null;
    }

}

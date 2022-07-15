package com.hk;

import com.hk.appointment.Appointment;
import com.hk.appointment.Hairdresser;
import com.hk.profile.Profile;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class GTBooker {

    // TODO: FIX HEADLESS FEATURE

    private final ChromeDriver driver;

    private final Logger logger;
    private final Scanner scanner;

    private final ArrayList<Hairdresser> hairdresser;
    private final ArrayList<Appointment> appointments;

    private final File windows_driver = new File("resources/driver/chromedriver.exe");
    private final File mac_driver = new File("resources/driver/chromedriver");

    private Profile profile;

    private String appointment_hairdresser;
    private String appointment_day;
    private String appointment_time;
    private String appointment_length;
    private String appointment_price;

    private boolean has_selected_profile;

    public GTBooker() {

        this.logger = LoggerFactory.getLogger(GTBooker.class);

        var os = System.getProperty("os.name").toLowerCase();

        if ((os.contains("windows") && !windows_driver.exists()) || (os.contains("mac") && !mac_driver.exists())) {
            logger.error("ERROR! chromedriver not found for " + os + ". Exiting application...");
            System.exit(0);
        }

        System.setProperty("webdriver.chrome.driver", os.contains("windows") ? windows_driver.getPath() : mac_driver.getPath());

        logger.info("Initializing scanner for input");
        this.scanner = new Scanner(System.in);

        logger.info("Initializing datastructures...");
        this.hairdresser = new ArrayList<>();
        this.appointments = new ArrayList<>();

        logger.info("Initializing arguments for the driver...");
        var options = new ChromeOptions();
        options.setHeadless(false);
        options.addArguments("--window-position=-32000, -32000");
        options.addArguments("--disable-gpu");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36");

        logger.info("Initializing driver for " + os + ".");
        this.driver = new ChromeDriver(options);

        logger.info("SELECT A PROFILE OR TYPE n, no, no thanks or skip to enter details manually! Available profiles:");
        Arrays.stream(Profile.values()).forEach(System.out::println);

        while (!has_selected_profile) {
            var input = scanner.nextLine();
            if (input == null) {
                logger.error("ERROR! You must select a profile or type n, no, no thanks or skip to enter details manually.");
                continue;
            }

            switch (input.toLowerCase()) {
                case "no", "n", "skip", "no thanks" -> has_selected_profile = true;
            }

            this.profile = Profile.getProfile(logger, input);
            has_selected_profile = true;

        }

    }

    public void openPage() {
        driver.get("https://golden-touch-gt.planway.com/");
        waitForPage("Opening page and finding hairdressers...", 1000);
    }

    public void selectHairdresser() {
        logger.info("SELECT A HAIRDRESSER:");

        for (int i = 0; i < 10; i++) {
            try {
                var hairdresser_element = driver.findElement(By.xpath("//*[@id=\"booking_form\"]/div[2]/div[2]/div/div/div[" + i + "]"));
                if (hairdresser_element == null || !hairdresser_element.getText().toLowerCase().contains("kadhem") || hairdresser_element.getText().isBlank() || hairdresser_element.getText().isEmpty())
                    continue;
                var name = hairdresser_element.getText().toLowerCase().replace("vælg", "").replace("kadhem", "").replaceAll("\\s+", "");
                hairdresser.add(new Hairdresser(name, hairdresser_element));
            } catch (NoSuchElementException | StaleElementReferenceException ignored) {

            }
        }

        Hairdresser selected_hairdresser = null;

        while (selected_hairdresser == null) {
            var input = scanner.nextLine();
            if (input == null) {
                logger.error("ERROR! Input is empty. You must select a hairdresser.");
                continue;
            }

            selected_hairdresser = Hairdresser.findHairDresser(logger, input, hairdresser);

            if (selected_hairdresser != null) {
                appointment_hairdresser = selected_hairdresser.name();
                performAction(selected_hairdresser.element()).click().build().perform();
            }

        }

    }

    public void selectServiceAndConfirm() {
        var wait_for_service = waitForClickableElement("Waiting for service selection...", "//*[@id=\"booking_form\"]/div[2]/div[3]/div/div[1]/div[2]/div/div[1]/div/div[1]/span");
        performAction(wait_for_service).click().build().perform();

        var wait_for_confirmation = waitForClickableElement("Waiting for confirmation button...", "//*[@id=\"goFurtherServiceBtn\"]/a");
        performAction(wait_for_confirmation).click().build().perform();

        logger.info("Confirming services: Hair & bread");
    }

    public void confirmExtraService() {

        var price = waitForVisibleElement("Appointment price", "//*[@id=\"booking_form\"]/div[2]/div[3]/div/div[3]/div/div[2]/span/currency");
        appointment_price = price.getText();

        var length = waitForVisibleElement("Appointment length", "//*[@id=\"booking_form\"]/div[2]/div[4]/div/div[3]/div/div[2]/minute");
        appointment_length = length.getText();

        var wait_for_confirmation = waitForClickableElement("Waiting for confirmation button...", "//*[@id=\"booking_form\"]/div[2]/div[4]/div/div[2]/a");
        performAction(wait_for_confirmation).click().build().perform();

        logger.info("Confirming services: No additional service");
    }

    public void findAppointmentDays() {
        waitForPage("Finding available bookings...", 3000);
        for (int i = 0; i < 10; i++) {
            try {
                var day_element = driver.findElement(By.xpath("//*[@id=\"datepicker\"]/div/div[1]/table/tbody/tr[5]/td[" + i + "]"));
                var date_element = driver.findElement(By.xpath("//*[@id=\"datepicker\"]/div/div[1]/table/thead/tr[2]/th[2]"));
                var month = date_element.getText().split(" ")[0];
                var year = date_element.getText().split(" ")[1];
                if (day_element == null || !day_element.isEnabled() || day_element.getText().isBlank() || day_element.getText().isEmpty())
                    continue;
                var disabled = day_element.getCssValue("color").contains("153");
                if (!disabled)
                    appointments.add(new Appointment(day_element.getText(), month, year,"//*[@id=\"datepicker\"]/div/div[1]/table/tbody/tr[5]/td[" + i + "]"));
            } catch (NoSuchElementException | StaleElementReferenceException ignored) {

            }
        }
    }

    public void findAppointments() {
        for (Appointment appointment : appointments) {
            performAction(driver.findElement(By.xpath(appointment.getDay_xpath()))).click().build().perform();
            //var table_element = driver.findElement(By.xpath("//*[@id=\"booking_form\"]/div[2]/div[5]/div/div/div[2]/div[2]"));
            //var wait_for_table = new WebDriverWait(driver, Duration.ofMillis(10_000)).until(ExpectedConditions.visibilityOf(table_element));
            waitForPage("Loading available appointments for " + appointment.getDay() + " " + appointment.getMonth() + "...", 3000);
            for (int i = 0; i < 100; i++) {
                try {
                    var time = driver.findElement(By.xpath(appointment.getDay_xpath())).findElement(By.xpath("//*[@id=\"booking_form\"]/div[2]/div[5]/div/div/div[2]/div[2]/div/div/span[" + i + "]"));
                    if (time == null || time.getText() == null || !time.isEnabled() || time.getText().isEmpty() || time.getText().isBlank())
                        continue;
                    appointment.getTime_xpath().add("//*[@id=\"booking_form\"]/div[2]/div[5]/div/div/div[2]/div[2]/div/div/span[" + i + "]");
                    System.out.print(time.getText() + " ");
                } catch (NoSuchElementException | StaleElementReferenceException ignored) {

                }
            }
            System.out.println("\n" + appointment.getDay() + " " + appointment.getMonth() + " has " + appointment.getTime_xpath().size() + " appointments available");
        }

        logger.info("SELECT AN APPOINTMENT DAY:");

        Appointment selected_day = null;

        while (selected_day == null) {
            var input = scanner.nextLine();
            if (input == null) {
                logger.error("ERROR! Input must be the day of the appointment");
                continue;
            }
            selected_day = Appointment.findAppointmentDay(logger, input, appointments);
            if (selected_day != null) {
                appointment_day = selected_day.getDay() + " " + selected_day.getMonth();
                performAction(driver.findElement(By.xpath(selected_day.getDay_xpath()))).click().build().perform();
            }
        }

        waitForPage("Waiting for appointments to load for " + selected_day.getDay() + " " + selected_day.getMonth(), 3000);
        logger.info("SELECT AN APPOINTMENT TIME:");

        for (String time_xpath : selected_day.getTime_xpath()) {
            var time_element = driver.findElement(By.xpath(selected_day.getDay_xpath())).findElement(By.xpath(time_xpath));
            System.out.print(time_element.getText() + " ");
        }

        String selected_appointment = null;
        while (selected_appointment == null) {
            var input = scanner.nextLine();
            if (input == null) {
                logger.error("ERROR! Input must be an appointment time from the selected day.");
                return;
            }
            selected_appointment = Appointment.findAppointmentTime(logger, input, driver, selected_day, selected_day.getTime_xpath());
            if (selected_appointment != null) {
                var selected_time = driver.findElement(By.xpath(selected_day.getDay_xpath())).findElement(By.xpath(selected_appointment));
                appointment_time = selected_time.getText();
                performAction(selected_time).click().build().perform();
            }
        }

    }

    public void enterPhoneNumber() {

        var phone_field = driver.findElement(By.xpath("//*[@id=\"booking_form\"]/div[2]/div[6]/div/div[2]/div[1]/div/div/input"));
        var wait_for_button = waitForClickableElement("Waiting for button...", "//*[@id=\"booking_form\"]/div[2]/div[6]/div/div[2]/div[2]/div/a");

        if (profile != null && has_selected_profile) {
            logger.info("Entering phone number for profile: " + profile);
            phone_field.sendKeys(profile.getPhone_number());
        }

        if (profile == null && has_selected_profile) {
            logger.info("ENTER A PHONE NUMBER:");
            String phone_number = null;
            while (phone_number == null) {
                var input = scanner.nextLine();
                if (input == null) {
                    logger.error("ERROR! Input must be a 8 digit danish phone number.");
                    continue;
                }
                if (input.length() != 8) {
                    logger.error("ERROR! Phone number must be 8 digit.");
                    continue;
                }
                phone_number = input;
            }
            phone_field.sendKeys(phone_number);
        }

        performAction(wait_for_button).click().build().perform();

        logger.info("Confirming reservation...");

    }

    public void acceptTOS() {

        var wait_for_tos = new WebDriverWait(driver, Duration.ofMillis(10000)).until(ExpectedConditions.elementToBeClickable(By.className("acceptConditions")));
        performAction(wait_for_tos).click().build().perform();

        var wait_for_button = waitForClickableElement("Waiting for confirmation button...", "//*[@id=\"booking_form\"]/div[2]/div[6]/div/div[4]/div[8]/div[1]/a");
        performAction(wait_for_button).click().build().perform();
    }

    public void acceptMarketingAndFinish() {

        var wait_for_marketing = waitForClickableElement("Waiting for marketing button...", "//*[@id=\"marketingaccept\"]/div/div/div/div/div[1]/a");
        performAction(wait_for_marketing).click().build().perform();

        try {
            var wait_for_finish = waitForClickableElement("Waiting for finish button...", "//*[@id=\"booking_form\"]/div[2]/div[7]/div/div[8]/button");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", wait_for_finish);
        } catch (WebDriverException ignored) {

        }

        System.out.println("Booking successful! You will receive an e-mail and SMS as confirmation for this appointment.");
        System.out.println("Date: " + appointment_day + ", " + appointment_time);
        System.out.println("Hairdresser: " + appointment_hairdresser);
        System.out.println("Price: " + appointment_price + " DKK");
        System.out.println("Length: " + appointment_length + " min");

        driver.manage().deleteAllCookies();
        driver.quit();

    }

    private WebElement waitForVisibleElement(String element_description, String xpath) {
        logger.info(element_description);
        return new WebDriverWait(driver, Duration.ofMillis(10_000)).until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath(xpath))));
    }

    private WebElement waitForClickableElement(String element_description, String xpath) {
        logger.info(element_description);
        return new WebDriverWait(driver, Duration.ofMillis(10_000)).until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
    }

    private Actions performAction(WebElement element) {
        return new Actions(driver).moveToElement(element);
    }

    private void waitForPage(String reason, long milliseconds) {
        try {
            logger.info(reason);
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

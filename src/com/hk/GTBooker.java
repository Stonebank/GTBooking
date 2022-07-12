package com.hk;

import com.hk.appointment.Appointment;
import com.hk.appointment.Hairdresser;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Scanner;

public class GTBooker {

    private final ChromeDriver driver;

    private final Logger logger;
    private final Scanner scanner;

    private final ArrayList<Hairdresser> hairdresser;
    private final ArrayList<Appointment> appointments;

    private final File windows_driver = new File("resources/driver/chromedriver.exe");
    private final File mac_driver = new File("resources/driver/chromedriver");

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

        logger.info("Initializing driver for " + os + ".");
        this.driver = new ChromeDriver(options);

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

            if (selected_hairdresser != null)
                selected_hairdresser.element().click();

        }

    }

    public void selectServiceAndConfirm() {
        var wait_for_service = new WebDriverWait(driver, Duration.ofMillis(10000)).until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"booking_form\"]/div[2]/div[3]/div/div[1]/div[2]/div/div[1]/div/div[1]/span")));
        wait_for_service.click();

        var wait_for_confirmation = new WebDriverWait(driver, Duration.ofMillis(10000)).until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"goFurtherServiceBtn\"]/a")));
        wait_for_confirmation.click();

        logger.info("Confirming services: Hair & bread");
    }

    public void confirmExtraService() {
        var wait_for_confirmation = new WebDriverWait(driver, Duration.ofMillis(10000)).until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"booking_form\"]/div[2]/div[4]/div/div[2]/a")));
        wait_for_confirmation.click();

        var total = driver.findElement(By.xpath("//*[@id=\"booking_form\"]/div[2]/div[3]/div/div[3]/div/div[2]/span/currency")).getText();
        var minutes = driver.findElement(By.xpath("//*[@id=\"booking_form\"]/div[2]/div[3]/div/div[3]/div/div[2]/minute")).getText();

        System.out.println(total + " " + minutes);

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
            driver.findElement(By.xpath(appointment.getDay_xpath())).click();
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

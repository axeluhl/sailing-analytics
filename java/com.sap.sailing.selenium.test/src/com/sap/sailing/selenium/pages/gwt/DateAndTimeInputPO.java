package com.sap.sailing.selenium.pages.gwt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.pages.PageArea;

public class DateAndTimeInputPO extends PageArea {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MMM d, y");
    private static final DateFormat TIME_FORMAT_MINUTES = new SimpleDateFormat("h:mm a");
    private static final DateFormat TIME_FORMAT_SECONDS = new SimpleDateFormat("h:mm:ss a");
    private static final DateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat ISO_TIME_FORMAT_MINUTES = new SimpleDateFormat("HH:mm");
    private static final DateFormat ISO_TIME_FORMAT_SECONDS = new SimpleDateFormat("HH:mm:ss");
    private static final DateFormat ISO_DATE_TIME_FORMAT_MINUTES = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    private static final DateFormat ISO_DATE_TIME_FORMAT_SECONDS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private final WebElement element;

    private DateAndTimeInputPO(WebDriver driver, WebElement element) {
        super(driver, element);
        this.element = element;
    }

    /**
     * Sets the provided {@link Date date value} to the underlying {@link WebElement}.
     * 
     * @param date
     *            the {@link Date date value} to set to the input field
     * @param enterSeconds
     *            <code>true</code> to enter seconds fragment as well, <code>false</code> otherwise
     * 
     * @see DateFormat#format(Date)
     * @see WebElement#sendKeys(CharSequence...)
     */
    public void setValue(Date date, boolean enterSeconds) {
        if (isFieldOfType(element, "datetime-local")) {
            this.setValueNative(element, date, enterSeconds ? ISO_DATE_TIME_FORMAT_SECONDS : ISO_DATE_TIME_FORMAT_MINUTES);
        } else {
            final WebElement dateInput = element.findElement(new BySeleniumId("dateInput"));
            final WebElement timeInput = element.findElement(new BySeleniumId("timeInput"));
            if (isFieldOfType(dateInput, "date")) {
                this.setValueNative(dateInput, date, ISO_DATE_FORMAT);
            } else {
                this.setValue(dateInput, date, DATE_FORMAT);
            }
            
            if (isFieldOfType(timeInput, "time")) {
                this.setValueNative(timeInput, date, enterSeconds ? ISO_TIME_FORMAT_SECONDS : ISO_TIME_FORMAT_MINUTES);
            } else {
                this.setValue(timeInput, date, enterSeconds ? TIME_FORMAT_SECONDS : TIME_FORMAT_MINUTES);
            }
        }
    }
    
    private boolean isFieldOfType(WebElement inputToCheck, String type) {
        return type.equals(inputToCheck.getAttribute("type"));
    }

    private void setValue(WebElement input, Date date, DateFormat format) {
        final String value = format.format(date);
        input.clear();
        input.sendKeys(value);
        input.sendKeys("\t"); // ensure popup closing!
    }
    
    private void setValueNative(WebElement input, Date date, DateFormat format) {
        final String value = format.format(date);
        final JavascriptExecutor javascriptExecutor = ((JavascriptExecutor) driver);
        javascriptExecutor.executeScript("arguments[0].value = arguments[1];", input, value);
    }

    public static DateAndTimeInputPO create(WebDriver driver, WebElement element) {
        return new DateAndTimeInputPO(driver, element);
    }
}

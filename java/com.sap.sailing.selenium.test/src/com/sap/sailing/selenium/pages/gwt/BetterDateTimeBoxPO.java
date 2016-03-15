package com.sap.sailing.selenium.pages.gwt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.pages.PageObject;

/**
 * {@link PageObject} implementation for better date time boxes.
 */
public class BetterDateTimeBoxPO extends TextBoxPO {
    
    private static final DateFormat TIME_FORMAT_HOURS_MINUTES = new SimpleDateFormat("dd/MM/yyyy hh:mm");
    private static final DateFormat TIME_FORMAT_HOURS_MINUTES_SECONDS = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    
    private final DateFormat timeFormat;

    /**
     * Factory method to create a {@link BetterDateTimeBoxPO} using {@link DateFormat} <code>dd/MM/yyyy hh:mm</code> by
     * default.
     * 
     * @param driver the web driver to use
     * @param element the element representing the date time box on the page
     * @return a new {@link BetterDateTimeBoxPO} instance
     */
    public static BetterDateTimeBoxPO create(WebDriver driver, WebElement element) {
        return new BetterDateTimeBoxPO(driver, element, TIME_FORMAT_HOURS_MINUTES);
    }
    /**
     * Factory method to create a {@link BetterDateTimeBoxPO}.
     * 
     * @param driver the web driver to use
     * @param element the element representing the date time box on the page
     * @param includeSeconds <code>true</code> to use the {@link DateFormat} <code>dd/MM/yyyy hh:mm:ss</code>,
     *                  <code>false</code> to use the default {@link DateFormat} <code>dd/MM/yyyy hh:mm</code>
     * @return a new {@link BetterDateTimeBoxPO} instance
     */
    public static BetterDateTimeBoxPO create(WebDriver driver, WebElement element, boolean includeSeconds) {
        DateFormat timeFormat = includeSeconds ? TIME_FORMAT_HOURS_MINUTES_SECONDS : TIME_FORMAT_HOURS_MINUTES;
        return new BetterDateTimeBoxPO(driver, element, timeFormat);
    }
    
    /**
     * @see TextBoxPO#TextBoxPO(WebDriver, WebElement)
     */
    protected BetterDateTimeBoxPO(WebDriver driver, WebElement element, DateFormat timeFormat) {
        super(driver, element);
        this.timeFormat = timeFormat;
    }
    
    /**
     * Sets the underlying {@link WebElement}s text by formatting the given {@link Date} using the internal date format
     * (<code>dd/MM/yyyy hh:mm</code> or <code>dd/MM/yyyy hh:mm:ss</code>).
     * 
     * @param date the {@link Date} object to set
     * 
     * @see DateFormat#format(Date)
     * @see #setText(String)
     */
    public void setDate(Date date) {
        this.setText(timeFormat.format(date));
    }
    
    @Override
    public void appendText(String text) {
        super.appendText(text);
        // Send tabulator key after changing text to ensure the date time boxes datepicker popup is closed.
        // Otherwise, the popup potentially overlaps other controls, which can cause unexpected errors.
        super.appendText("\t");
    }
    
}

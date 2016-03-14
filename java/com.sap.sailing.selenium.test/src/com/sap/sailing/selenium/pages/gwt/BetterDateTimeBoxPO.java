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
    
    private static final DateFormat DATE_TIME_BOX_FORMAT = new SimpleDateFormat("dd/MM/yyyy hh:mm");
    
    /**
     * Factory method to create a {@link BetterDateTimeBoxPO}.
     * 
     * @param driver the web driver to use
     * @param element the element representing the date time box on the page
     * @return a new {@link BetterDateTimeBoxPO} instance
     */
    public static BetterDateTimeBoxPO create(WebDriver driver, WebElement element) {
        return new BetterDateTimeBoxPO(driver, element);
    }
    
    /**
     * @see TextBoxPO#TextBoxPO(WebDriver, WebElement)
     */
    protected BetterDateTimeBoxPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    /**
     * Sets the underlying {@link WebElement}s text by formatting the given {@link Date} using the internal date format
     * (<code>dd/MM/yyyy hh:mm</code>).
     * 
     * @param date the {@link Date} object to set
     * 
     * @see DateFormat#format(Date)
     * @see #setText(String)
     */
    public void setDate(Date date) {
        this.setText(DATE_TIME_BOX_FORMAT.format(date));
    }
    
    @Override
    public void appendText(String text) {
        super.appendText(text);
        // Send tabulator key after changing text to ensure the date time boxes datepicker popup is closed.
        // Otherwise, the popup potentially overlaps other controls, which can cause unexpected errors.
        super.appendText("\t");
    }
    
}

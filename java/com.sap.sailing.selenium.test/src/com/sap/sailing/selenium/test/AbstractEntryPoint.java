package com.sap.sailing.selenium.test;

import org.openqa.selenium.WebDriver;

/**
 * <p>Base class for page objects representing an entry point. This class uses a customized initialization, which waits
 *   for the completion of the GWT bootstrap process before the annotated fields are initialized.</p>
 * 
 * @author
 *   D049941
 */
public class AbstractEntryPoint extends PageObject {
    /**
     * <p>Creates a new page object with the given web driver. In GWT an entry point is connected to a HTML page in
     *   which the code for the application is executed, whereby the page is represented by the web driver.</p>
     * 
     * @param driver
     *   The web driver to use.
     */
    public AbstractEntryPoint(WebDriver driver) {
        super(driver);
    }
    
    /**
     * <p>Initialize the page object. Since the GWT bootstrap process can take a bit of time, the global Ajax counter is
     *   initialized to be 1 and acts as a semaphore for bootstrap. Therefore an entry point waits until the counter
     *   reach zero, which indicates the completion of the bootstrap.</p>
     */
    @Override
    protected void initElements() {
        waitForAjaxRequests(super.getTimeOut(), 5);
        
        super.initElements();
    }
}

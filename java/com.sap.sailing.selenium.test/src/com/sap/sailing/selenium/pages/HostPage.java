package com.sap.sailing.selenium.pages;

import org.openqa.selenium.WebDriver;

/**
 * <p>Base class for page objects representing an entry point. This class uses a customized initialization, which waits
 *   for the completion of the GWT bootstrap process before the annotated fields are initialized.</p>
 * 
 * @author
 *   D049941
 */
public class HostPage extends PageObject {
    protected static final String GWT_CODE_SERVER_PARAMETER_NAME = "gwt.codesvr"; //$NON-NLS-1$
    
    protected static final String NO_CODE_SERVER_PARAMTER_VALUE = ""; //$NON-NLS-1$
    
    /**
     * </p>The default timeout of 60 seconds for the initialization of the page object.</p>
     */
    protected static final int DEFAULT_PAGE_LOAD_TIMEOUT = 60;
    
    public static final String getGWTCodeServer() {
        String codeServer = System.getProperty(GWT_CODE_SERVER_PARAMETER_NAME);
        if (codeServer == null) {
            return NO_CODE_SERVER_PARAMTER_VALUE;
        }
        return GWT_CODE_SERVER_PARAMETER_NAME + "=" + codeServer;
    }
    
    /**
     * <p>Creates a new page object with the given web driver. In GWT an entry point is connected to a HTML page in
     *   which the code for the application is executed, whereby the page is represented by the web driver.</p>
     * 
     * @param driver
     *   The web driver to use.
     */
    public HostPage(WebDriver driver) {
        super(driver);
    }
    
    /**
     * <p>Initialize the page object. Since the GWT bootstrap process can take a bit of time, the global Ajax counter is
     *   initialized to be 1 and acts as a semaphore for bootstrap. Therefore an entry point waits until the counter
     *   reach zero, which indicates the completion of the bootstrap.</p>
     */
    @Override
    protected void initElements() {
        waitForAjaxRequests(getPageLoadTimeOut(), 5);
        
        super.initElements();
    }
    
    protected int getPageLoadTimeOut() {
        return DEFAULT_PAGE_LOAD_TIMEOUT;
    }
}

package com.sap.sailing.selenium.pages;

import java.net.URI;
import java.net.URISyntaxException;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * <p>Base class for page objects representing an entry point. This class uses a customized initialization, which waits
 *   for the completion of the GWT bootstrap process before the annotated fields are initialized.</p>
 * 
 * @author
 *   D049941
 */
public abstract class HostPage extends PageObject {
    protected static final String GWT_CODE_SERVER_PARAMETER_NAME = "gwt.codesvr"; //$NON-NLS-1$
    
    protected static final String NO_CODE_SERVER_PARAMTER_VALUE = ""; //$NON-NLS-1$
    
    /**
     * </p>The default timeout of 120 seconds for the initialization of the page object.</p>
     */
    protected static final int DEFAULT_PAGE_LOAD_TIMEOUT_IN_SECONDS = 120;
    
    public static final String getGWTCodeServerAndLocale() {
        StringBuilder queryBuilder = new StringBuilder("locale=en");
        String codeServer = System.getProperty(GWT_CODE_SERVER_PARAMETER_NAME);
        if (codeServer != null) {
            queryBuilder.append("&").append(GWT_CODE_SERVER_PARAMETER_NAME).append("=").append(codeServer);
        }
        return queryBuilder.toString();
    }
    
    protected final static <T extends HostPage> T goToUrl(HostPageSupplier<T> supplier, WebDriver driver, String url) {
        try {
            goToPage(driver, new URI(url));
            return supplier.get(driver); 
        } catch (URISyntaxException exc) {
            throw new IllegalArgumentException(exc);
        }
    }
    
    private static final void goToPage(WebDriver driver, URI uri) throws URISyntaxException {
        String scheme = uri.getScheme(), userInfo = uri.getUserInfo(), host = uri.getHost(); 
        String path = uri.getPath(), query = getGWTCodeServerAndLocale(), fragment = uri.getFragment();
        driver.get(new URI(scheme, userInfo, host, uri.getPort(), path, query, fragment).toString());
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
        waitForAjaxRequests(getPageLoadTimeOutInSeconds(), 5 /* seconds */);
        super.initElements();
    }
    
    protected int getPageLoadTimeOutInSeconds() {
        return DEFAULT_PAGE_LOAD_TIMEOUT_IN_SECONDS;
    }
    
    protected interface HostPageSupplier<T extends HostPage> {
        T get(WebDriver driver);
    }
    
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    protected void scrollToTop() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
    }
}

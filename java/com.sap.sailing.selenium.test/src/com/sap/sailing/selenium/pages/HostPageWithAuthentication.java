package com.sap.sailing.selenium.pages;

import org.openqa.selenium.WebDriver;

import com.sap.sailing.selenium.pages.authentication.AuthenticationMenuPO;

/**
 * Extended {@link HostPage} implementation which provides user authentication.
 */
public class HostPageWithAuthentication extends HostPage {

    /**
     * @see HostPage#HostPage(WebDriver)
     */
    public HostPageWithAuthentication(WebDriver driver) {
        super(driver);
    }
    
    /**
     * Access the pages {@link AuthenticationMenuPO authentication menu}.
     * 
     * @return the pages {@link AuthenticationMenuPO authentication menu} handle
     */
    public AuthenticationMenuPO getAuthenticationMenu() {
        return new AuthenticationMenuPO(driver, findElementBySeleniumId("authenticationMenu"));
    }

}

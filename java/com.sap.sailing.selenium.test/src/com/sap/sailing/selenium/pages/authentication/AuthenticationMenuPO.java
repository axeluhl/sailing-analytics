package com.sap.sailing.selenium.pages.authentication;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.common.CSSHelper;

public class AuthenticationMenuPO extends PageArea {
    
    private static final String CSS_CLASS_PREFIX = "com-sap-sse-security-ui-authentication-generic-sapheader-SAPHeaderWithAuthenticationResources-HeaderWithAuthenticationCss-";
    private static final String CSS_CLASS_OPEN = CSS_CLASS_PREFIX + "usermanagement_open";
    private static final String CSS_CLASS_LOGGED_IN = CSS_CLASS_PREFIX + "usermanagement_loggedin";

    public AuthenticationMenuPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public boolean isOpen() {
        return CSSHelper.hasCSSClass(getWebElement(), CSS_CLASS_OPEN);
    }
    
    public boolean isLoggedIn() {
        return CSSHelper.hasCSSClass(getWebElement(), CSS_CLASS_LOGGED_IN);
    }
    
    public AuthenticationViewPO showAuthenticationView() {
        getWebElement().click();
        return new AuthenticationViewPO(driver, findElementBySeleniumId(driver, "authenticationView"));
    }
    
}

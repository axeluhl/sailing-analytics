package com.sap.sailing.selenium.pages.authentication;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;

public class AuthenticationViewPO extends PageArea {
    
    @FindBy(how = BySeleniumId.class, using = "signInForm")
    private WebElement signInFormPanel;

    public AuthenticationViewPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public boolean isSignInFormDisplayed() {
        return signInFormPanel != null && signInFormPanel.isDisplayed();
    }
    
    public SignInViewPO getSignInView() {
        if (!isSignInFormDisplayed()) throw new RuntimeException("Sign in form not displayed!");
        return new SignInViewPO(driver, signInFormPanel);
    }
}

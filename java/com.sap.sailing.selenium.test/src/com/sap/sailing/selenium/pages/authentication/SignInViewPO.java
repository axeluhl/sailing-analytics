package com.sap.sailing.selenium.pages.authentication;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;

public class SignInViewPO extends PageArea {

    @FindBy(how = BySeleniumId.class, using = "loginTextField")
    private WebElement loginTextField;
        
    @FindBy(how = BySeleniumId.class, using = "passwordTextField")
    private WebElement passwordTextField;
    
    @FindBy(how = BySeleniumId.class, using = "signInAnchor")
    private WebElement signInAnchor;
    
    @FindBy(how = BySeleniumId.class, using = "signUpAnchor")
    private WebElement signUpAnchor;
    
    @FindBy(how = BySeleniumId.class, using = "forgotPasswordAnchor")
    private WebElement forgotPasswordAnchor;
    
    public SignInViewPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void doLogin(String username, String password) {
        loginTextField.clear();
        loginTextField.sendKeys(username);
        passwordTextField.clear();
        passwordTextField.sendKeys(password);
        signInAnchor.click();
    }
    
    public void goToSignUpView() {
        signUpAnchor.click();
    }
    
    public void goToForgotPasswordView() {
        forgotPasswordAnchor.click();
    }
}

package com.sap.sailing.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;

public class LoginPage extends HostPage {
    @FindBy(how = BySeleniumId.class, using = "username")
    private WebElement usernameField;

    @FindBy(how = BySeleniumId.class, using = "password")
    private WebElement passwordField;

    @FindBy(how = BySeleniumId.class, using = "login")
    private WebElement loginButton;

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void login(String username, String password) {
        usernameField.clear();
        usernameField.sendKeys(username);
        passwordField.click();
        passwordField.sendKeys(password);
        loginButton.click();
    }
}

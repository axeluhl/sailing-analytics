package com.sap.sailing.selenium.test.adminconsole.pages;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.support.ui.FluentWait;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.ElementSearchConditions;
import com.sap.sailing.selenium.core.FindBy;

import com.sap.sailing.selenium.test.PageObject;

public class AdminConsolePage extends PageObject {
    private static final String PAGE_TITLE = "SAP Sailing Analytics Administration Console";
    
    private static final MessageFormat TAB_EXPRESSION = new MessageFormat(
            ".//div[@class=\"gwt-TabBarItem\" and @role=\"tab\"]/div[text()=\"{0}\"]/..");
    
    private static final String TRACTRAC_TAB_LABEL = "TracTrac Events";
    private static final String TRACTRAC_TAB_IDENTIFIER = "TracTracEventManagement";
    
    public static AdminConsolePage goToPage(WebDriver driver, String root) {
        driver.get(root + "gwt/AdminConsole.html");
        
//        FluentWait<WebDriver> wait = new FluentWait<>(driver);
//        wait.withTimeout(5, TimeUnit.SECONDS);
//        wait.pollingEvery(100, TimeUnit.MILLISECONDS);
//        
//        Alert alert = wait.until(new Function<WebDriver, Alert>() {
//            @Override
//            public Alert apply(WebDriver context) {
//                TargetLocator locator = context.switchTo();
//                
//                return locator.alert();
//            }
//        });
//        alert.authenticateUsing(new UserAndPassword("user", "password"));
        
        return new AdminConsolePage(driver);
    }
    
    //@FindBy(how = How.XPATH, using = "//*[@selenium-id='AdministrationTabs']")
    @FindBy(how = BySeleniumId.class, using = "AdministrationTabs")
    private WebElement tabPanel;
    
    private AdminConsolePage(WebDriver driver) {
        super(driver);
        
        if(!PAGE_TITLE.equals(driver.getTitle())) {
            throw new IllegalStateException("This is not the administration console");
        }
    }
    
    public TracTracEventManagementPanel goToTracTracEvents() {
        return new TracTracEventManagementPanel(this.driver, gotToTab(TRACTRAC_TAB_LABEL, TRACTRAC_TAB_IDENTIFIER));
    }
    
    private WebElement gotToTab(String label, final String id) {
        String expression = TAB_EXPRESSION.format(new Object[] {label});
        WebElement tab = this.tabPanel.findElement(By.xpath(expression));
        
        tab.click();
        
        // Wait for the tab to become visible due to the used animations.
        FluentWait<WebElement> wait = new FluentWait<>(this.tabPanel);
        wait.withTimeout(30, TimeUnit.SECONDS);
        wait.pollingEvery(5, TimeUnit.SECONDS);
        
        WebElement content = wait.until(ElementSearchConditions.visibilityOfElementLocated(new BySeleniumId(id)));
                
        return content;
    }
}

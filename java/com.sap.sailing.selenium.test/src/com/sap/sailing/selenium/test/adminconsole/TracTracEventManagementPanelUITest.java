package com.sap.sailing.selenium.test.adminconsole;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Function;

import com.sap.sailing.selenium.test.AbstractSeleniumTest;

import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.support.ui.FluentWait;

public class TracTracEventManagementPanelUITest extends AbstractSeleniumTest {
    @Test
    public void test() {
        WebDriver driver = getWebDriver();
        
        System.out.println("Driver used for test is: " + driver.getClass().getName());
        
        driver.get(getContextRoot() + "gwt/AdminConsole.html");
        
        for(String handle : driver.getWindowHandles()) {
            System.out.println("Window handle: " + handle);
            //System.out.println("Title for window: " +driver.switchTo().window(handle).getTitle());
        }
        
        System.out.println("Title of page is: " + driver.getTitle());
        
        assertEquals("Unexpected page title", "SAP Sailing Analytics Administration Console", driver.getTitle());
        
        System.out.println("First step passed");
        
        WebElement tracTracTab = driver.findElement(By.xpath("//div[@class='gwt-TabBarItem' and @role='tab']/div[text()='TracTrac Events']/.."));
        tracTracTab.click();
        
        FluentWait<WebDriver> wait = new FluentWait<>(driver);
        wait.withTimeout(30, TimeUnit.SECONDS);
        wait.pollingEvery(5, TimeUnit.SECONDS);
        wait.ignoring(NoSuchElementException.class);
        
        WebElement urlField = wait.until(new Function<WebDriver, WebElement>() {
            @Override
            public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath("//div[@class='gwt-Label' and text() = 'JSON URL:']/../../td/input"));
            }
        });
        urlField.clear();
        urlField.sendKeys("http://germanmaster.traclive.dk/events/event_20120615_KielerWoch/jsonservice.php");
        
        WebElement listRacesButton = driver.findElement(By.xpath("//button[@class='gwt-Button' and text() = 'List Races']"));
        listRacesButton.click();
        
//        wait.withTimeout(5, TimeUnit.MINUTES);
//        wait.pollingEvery(10, TimeUnit.SECONDS);
//        List<WebElement> trackableRaces = wait.until(new Function<WebDriver, List<WebElement>>() {
//            @Override
//            public List<WebElement> apply(WebDriver driver) {
//                List<WebElement> elements = driver.findElements(By.xpath("//th[text() = 'Event']/../../../tbody/tr"));
//                return elements.size() >= 90 ? elements : null;
//            }
//        });
//        
//        assertTrue("No trackable races found", trackableRaces.size() > 0);
    }
}

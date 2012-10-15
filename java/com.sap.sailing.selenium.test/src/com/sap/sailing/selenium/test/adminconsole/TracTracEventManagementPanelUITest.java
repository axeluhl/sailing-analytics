package com.sap.sailing.selenium.test.adminconsole;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
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
    private static final String BMW_CUP_JSON_URL = "http://kml.skitrac.traclive.dk/events/event_20120803_BMWCup/jsonservice.php";
    @Test
    public void test() throws Exception {
        WebDriver driver = getWebDriver();
        
        driver.get(getContextRoot() + "gwt/AdminConsole.html");
        
        assertEquals("Title of the page does not match.", "SAP Sailing Analytics Administration Console", driver.getTitle());
        
        WebElement tabPanel = findElementBySeleniumId("AdministrationTabs");
        WebElement tracTracTab = tabPanel.findElement(By.xpath("//div[@class='gwt-TabBarItem' and @role='tab']/div[text()='TracTrac Events']/.."));
        WebElement managementPanel = findElementBySeleniumId(tabPanel, "TracTracEventManagement");

        tracTracTab.click();
        
        FluentWait<WebElement> wait = new FluentWait<>(managementPanel);
        wait.withTimeout(30, TimeUnit.SECONDS);
        wait.pollingEvery(5, TimeUnit.SECONDS);
        wait.ignoring(NoSuchElementException.class);
        
        WebElement urlField = wait.until(new Function<WebElement, WebElement>() {
            @Override
            public WebElement apply(WebElement element) {
                return findElementBySeleniumId(element, "JSONURL");
            }
        });
        urlField.clear();
        urlField.sendKeys(BMW_CUP_JSON_URL);
        
        WebElement listRacesButton = findElementBySeleniumId(managementPanel, "ListRaces");
        listRacesButton.click();
        
        wait.withTimeout(5, TimeUnit.MINUTES);
        wait.pollingEvery(10, TimeUnit.SECONDS);
        List<WebElement> trackableRaces = wait.until(new Function<WebElement, List<WebElement>>() {
            @Override
            public List<WebElement> apply(WebElement element) {
                WebElement availableRacesTabel = findElementBySeleniumId(element, "RacesTable");
                List<WebElement> elements = availableRacesTabel.findElements(By.xpath("//tbody/tr/td[1]/div[text()='BMW Cup']/../.."));
                return elements.size() > 0 ? elements : null;
            }
        });
        
        assertTrue("There should be 12 trackable races available.", trackableRaces.size() == 12);
        
        WebElement trackWindCheckbox = findElementBySeleniumId(managementPanel, "TrackWind");
        trackWindCheckbox.click();
        
        WebElement cupRace1 = trackableRaces.get(0);
        cupRace1.click();
        
//        WebElement startTrackingButton = findElementBySeleniumId(managementPanel, "StartTracking");
//        startTrackingButton.click();
//        
//        wait.withTimeout(5, TimeUnit.MINUTES);
//        wait.pollingEvery(10, TimeUnit.SECONDS);
//        List<WebElement> trackedRaces = wait.until(new Function<WebElement, List<WebElement>>() {
//            @Override
//            public List<WebElement> apply(WebElement element) {
//                //WebElement trackedRacesTabel = driver.findElement(By.id(GWT_DEBUG_PREFIX + "RacesTable"));
//                List<WebElement> elements = new ArrayList<>();//trackedRacesTabel.findElements(By.xpath("//tbody/tr/td[1]/div[text()='BMW CUP']/../.."));
//                return elements.size() > 0 ? elements : null;
//            }
//        });
        
        // TODO: Check, remove (and check remove)!
    }
}

package com.sap.sailing.selenium.pages.home.event;

import java.text.MessageFormat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.pages.HostPage;
import com.sap.sailing.selenium.pages.HostPageWithAuthentication;
import com.sap.sailing.selenium.pages.PageObject;
import com.sap.sailing.selenium.pages.home.regatta.RegattaListItemPO;

public class EventPage extends HostPageWithAuthentication {
    
    private static final MessageFormat REGATTA_LIST_ITEM_BY_REGATTA_NAME = new MessageFormat(
            ".//span[@selenium-id=\"RegattaNameSpan\" and text()=\"{0}\"]/ancestor::div[@selenium-id=\"RegattaListItemPanel\"]");
    private static final String EVENT_HEADER_IDENTIFIER = "EventHeaderPanel";
    
    /**
     * Navigates to the given home URL and provides the corresponding {@link PageObject}.
     * 
     * @param driver the {@link WebDriver} to use
     * @param url the desired destination URL
     * @return the {@link PageObject} for the home page
     */
    public static EventPage goToEventUrl(WebDriver driver, String url) {
        return HostPage.goToUrl(EventPage::new, driver, url);
    }

    private EventPage(WebDriver driver) {
        super(driver);
    }
    
    @Override
    protected void verify() {
        WebDriverWait wait = new WebDriverWait(driver, DEFAULT_WAIT_TIMEOUT_SECONDS);
        wait.until(ExpectedConditions.presenceOfElementLocated(new BySeleniumId(EVENT_HEADER_IDENTIFIER)));
    }
    
    public EventHeaderPO getEventHeader() {
        return getPO(EventHeaderPO::new, EVENT_HEADER_IDENTIFIER);
    }
    
    public RegattaListItemPO getRegattaListItem(String regattaName) {
        String xpathExpression = REGATTA_LIST_ITEM_BY_REGATTA_NAME.format(new Object[]{regattaName});
        return new RegattaListItemPO(driver, driver.findElement(By.xpath(xpathExpression)));
    }

}

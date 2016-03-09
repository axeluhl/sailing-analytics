package com.sap.sailing.selenium.pages.adminconsole.event;

import java.util.Date;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;

public class EventConfigurationPanelPO extends PageArea {
    
    @FindBy(how = BySeleniumId.class, using = "RefreshEventsButton")
    private WebElement refreshEventsButton;
    
    @FindBy(how = BySeleniumId.class, using = "CreateEventButton")
    private WebElement createEventButton;
    
    @FindBy(how = BySeleniumId.class, using = "RemoveEventsButton")
    private WebElement removeEventsButton;

    public EventConfigurationPanelPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void createPublicEvent(String name, String venue, Date startDate, Date endDate) {
        createEventButton.click();
        EventCreateDialogPO dialog = new EventCreateDialogPO(driver, findElementBySeleniumId(driver, "EventCreateDialog"));
        dialog.createEvent(name, "", venue, startDate, endDate, true);
    }

}

package com.sap.sailing.selenium.pages.adminconsole.event;

import java.util.Date;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.common.ConfirmDialogPO;
import com.sap.sailing.selenium.pages.gwt.CellTablePO;
import com.sap.sailing.selenium.pages.gwt.DataEntryPO;
import com.sap.sailing.selenium.pages.gwt.GenericCellTablePO;

public class EventConfigurationPanelPO extends PageArea {
    
    public static class EventEntryPO extends DataEntryPO {

        public EventEntryPO(CellTablePO<?> table, WebElement element) {
            super(table, element);
        }
        
        @Override
        public String getIdentifier() {
            return getColumnContent("Event");
        }
        
        public String getEventURL() {
            return getWebElement().findElement(By.xpath(".//td/div/a")).getAttribute("href");
        }        
    }
    
    @FindBy(how = BySeleniumId.class, using = "RefreshEventsButton")
    private WebElement refreshEventsButton;
    
    @FindBy(how = BySeleniumId.class, using = "CreateEventButton")
    private WebElement createEventButton;
    
    @FindBy(how = BySeleniumId.class, using = "RemoveEventsButton")
    private WebElement removeEventsButton;
    
    @FindBy(how = BySeleniumId.class, using = "EventsCellTable")
    private WebElement eventsCellTable;

    public EventConfigurationPanelPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void createSimplePublicEvent(String name, String venue, Date startDate, Date endDate) {
        createEventButton.click();
        EventCreateDialogPO createDialog = getDialog(EventCreateDialogPO::new, "EventCreateDialog");
        createDialog.createEvent(name, "", venue, startDate, endDate, true);
        ConfirmDialogPO confirmDialog = getDialog(ConfirmDialogPO::new, "CreateDefaultLeaderboardGroupConfirmDialog");
        confirmDialog.pressNo();
    }
    
    public EventEntryPO getEventEntry(String event) {
        return getEventsTable().getEntry(event);
    }
    
    private CellTablePO<EventEntryPO> getEventsTable() {
        return new GenericCellTablePO<>(this.driver, this.eventsCellTable, EventEntryPO.class);
    }
    
}

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
    
    private static final String ID_EVENT_CREATE_DIALOG = "EventCreateDialog";
    private static final String ID_CREATE_DEFAULT_LEADERBOARD_GROUP_CONFIRM_DIALOG = "CreateDefaultLeaderboardGroupConfirmDialog";
    
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
    
    public void createEmptyEvent(String name, String desc, String venue, Date startDate, Date endDate, boolean isPublic) {
        createEventButton.click();
        EventCreateDialogPO createDialog = getPO(EventCreateDialogPO::new, ID_EVENT_CREATE_DIALOG);
        createDialog.createEvent(name, desc, venue, startDate, endDate, isPublic);
        ConfirmDialogPO confirmDialog = getPO(ConfirmDialogPO::new, ID_CREATE_DEFAULT_LEADERBOARD_GROUP_CONFIRM_DIALOG);
        confirmDialog.pressNo();
    }
    
    public EventEntryPO getEventEntry(String event) {
        return getEventsTable().getEntry(event);
    }
    
    private CellTablePO<EventEntryPO> getEventsTable() {
        return new GenericCellTablePO<>(this.driver, this.eventsCellTable, EventEntryPO.class);
    }
    
}

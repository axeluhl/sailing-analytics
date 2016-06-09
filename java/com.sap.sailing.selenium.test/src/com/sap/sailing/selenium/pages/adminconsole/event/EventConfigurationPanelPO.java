package com.sap.sailing.selenium.pages.adminconsole.event;

import java.util.Date;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardGroupCreateDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaCreateDialogPO;
import com.sap.sailing.selenium.pages.common.ConfirmDialogPO;
import com.sap.sailing.selenium.pages.gwt.CellTablePO;
import com.sap.sailing.selenium.pages.gwt.DataEntryPO;
import com.sap.sailing.selenium.pages.gwt.GenericCellTablePO;

public class EventConfigurationPanelPO extends PageArea {
    
    private static final String ID_EVENT_CREATE_DIALOG = "EventCreateDialog";
    private static final String ID_REGATTA_CREATE_DIALOG = "RegattaCreateDialog";
    private static final String ID_CREATE_DEFAULT_LEADERBOARD_GROUP_DIALOG = "LeaderboardGroupCreateDialog";
    private static final String ID_CREATE_DEFAULT_LEADERBOARD_GROUP_CONFIRM_DIALOG = "CreateDefaultLeaderboardGroupConfirmDialog";
    private static final String ID_CREATE_DEFAULT_REGATTA_CONFIRM_DIALOG = "CreateDefaultRegattaDialog";
    private static final String ID_CREATE_DEFAULT_REGATTA_LEADERBOARD_CONFIRM_DIALOG = "CreateDefaultRegattaLeaderboardDialog";
    private static final String ID_LINK_LEADERBORAD_TO_GROUP_DIALOG = "LinkRegattaLeaderboardToLeaderboardGroupOfEventDialog";
    
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
        createEvent(name, desc, venue, startDate, endDate, isPublic).pressNo();
    }
    
    public void createEventWithDefaultLeaderboardGroupRegattaAndDefaultLeaderboard(String eventName, String eventDesc,
            String venue, Date eventStartDate, Date eventEndDate, boolean isPublic, String regattaName, String boatClass,
            Date regattaStartDate, Date regattaEndDate) {
        createEvent(eventName, eventDesc, venue, eventStartDate, eventEndDate, isPublic).pressYes();
        getPO(LeaderboardGroupCreateDialogPO::new, ID_CREATE_DEFAULT_LEADERBOARD_GROUP_DIALOG).pressOk();
        getPO(ConfirmDialogPO::new, ID_CREATE_DEFAULT_REGATTA_CONFIRM_DIALOG).pressYes();
        createRegatta(regattaName, boatClass, regattaStartDate, regattaEndDate).pressOk();
        getPO(ConfirmDialogPO::new, ID_LINK_LEADERBORAD_TO_GROUP_DIALOG).pressOk();
    }
    
    public void createEventWithExistingLeaderboardGroups(String eventName, String eventDesc, String venue,
            Date eventStartDate, Date eventEndDate, boolean isPublic, String... leaderboardGroupNames) {
        createEventButton.click();
        EventCreateDialogPO createDialog = getPO(EventCreateDialogPO::new, ID_EVENT_CREATE_DIALOG);
        createDialog.setValues(eventName, eventDesc, venue, eventStartDate, eventEndDate, isPublic);
        WebElement leaderboardGroupsTab = createDialog.goToLeaderboardGroupsTab();
        for (final String leaderboardGroupName : leaderboardGroupNames) {
            createDialog.addLeaderboardGroup(leaderboardGroupsTab, leaderboardGroupName);
        }
        createDialog.pressOk();
        getPO(ConfirmDialogPO::new, ID_CREATE_DEFAULT_LEADERBOARD_GROUP_CONFIRM_DIALOG).pressNo();
    }
    
    public EventEntryPO getEventEntry(String event) {
        return getEventsTable().getEntry(event);
    }
    
    private CellTablePO<EventEntryPO> getEventsTable() {
        return new GenericCellTablePO<>(this.driver, this.eventsCellTable, EventEntryPO.class);
    }
    
    private ConfirmDialogPO createEvent(String name, String desc, String venue, Date start, Date end, boolean isPublic) {
        createEventButton.click();
        EventCreateDialogPO createDialog = getPO(EventCreateDialogPO::new, ID_EVENT_CREATE_DIALOG);
        createDialog.setValues(name, desc, venue, start, end, isPublic);
        createDialog.pressOk();
        return getPO(ConfirmDialogPO::new, ID_CREATE_DEFAULT_LEADERBOARD_GROUP_CONFIRM_DIALOG);
    }
    
    private ConfirmDialogPO createRegatta(String name, String boatClass, Date start, Date end) {
        RegattaCreateDialogPO createDialog = getPO(RegattaCreateDialogPO::new, ID_REGATTA_CREATE_DIALOG);
        createDialog.setValues(name, boatClass, start, end);
        createDialog.pressOk();
        return getPO(ConfirmDialogPO::new, ID_CREATE_DEFAULT_REGATTA_LEADERBOARD_CONFIRM_DIALOG);
    }
    
}

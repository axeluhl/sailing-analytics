package com.sap.sailing.selenium.pages.adminconsole.event;

import java.util.Date;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;
import com.sap.sailing.selenium.pages.gwt.CheckBoxPO;
import com.sap.sailing.selenium.pages.gwt.DateAndTimeInputPO;
import com.sap.sailing.selenium.pages.gwt.TextAreaPO;
import com.sap.sailing.selenium.pages.gwt.TextBoxPO;

public class EventCreateDialogPO extends DataEntryDialogPO {

    private static final String LEADERBOARD_GROUPS_TAB_ID = "LeaderboardGroupsTab";
    private static final String COURSE_AREAS_TAB_ID = "CourseAreasTab";
    private static final String IMAGES_TAB_ID = "ImagesTab";
    private static final String VIDEOS_TAB_ID = "VideosTab";

    @FindBy(how = BySeleniumId.class, using = "NameTextBox")
    private WebElement nameTextBox;
    
    @FindBy(how = BySeleniumId.class, using = "DescriptionTextArea")
    private WebElement descriptionTextArea;
    
    @FindBy(how = BySeleniumId.class, using = "VenueTextBox")
    private WebElement venueTextBox;
    
    @FindBy(how = BySeleniumId.class, using = "StartDateTimeBox")
    private WebElement startDateTimeBox;
    
    @FindBy(how = BySeleniumId.class, using = "EndDateTimeBox")
    private WebElement endDateTimeBox;
    
    @FindBy(how = BySeleniumId.class, using = "IsPublicCheckBox")
    private WebElement isPublicCheckBox;
    
    @FindBy(how = BySeleniumId.class, using = "EventDialogTabs")
    private WebElement eventDialogTabPanel;
    
    EventCreateDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    void setValues(String name, String description, String venue, Date start, Date end, boolean isPublic) {
        TextBoxPO.create(driver, nameTextBox).appendText(name);
        TextAreaPO.create(driver, descriptionTextArea).appendText(description);
        TextBoxPO.create(driver, venueTextBox).appendText(venue);
        DateAndTimeInputPO.create(driver, startDateTimeBox).setValue(start, false);
        DateAndTimeInputPO.create(driver, endDateTimeBox).setValue(end, false);
        CheckBoxPO.create(driver, isPublicCheckBox).setSelected(isPublic);
    }
    
    void addLeaderboardGroup(WebElement leaderboardGroupsTab, String leaderboardGroupName) {
        // assumes that the Leaderboard groups tab is already active
        new Select(findElementBySeleniumId(leaderboardGroupsTab, "SelectionListBox")).selectByVisibleText(leaderboardGroupName);
        findElementBySeleniumId(leaderboardGroupsTab, "AddButton").click();
    }
    
    WebElement goToTab(String tabName, String id) {
        return goToTab(eventDialogTabPanel, tabName, id, TabPanelType.TAB_LAYOUT_PANEL);
    }
    
    WebElement goToLeaderboardGroupsTab() {
        return goToTab("Leaderboard groups", LEADERBOARD_GROUPS_TAB_ID);
    }

    CourseAreasTabPO goToCourseAreasTab() {
        return new CourseAreasTabPO(driver, goToTab("Course areas", COURSE_AREAS_TAB_ID));
    }

    WebElement goToImagesTab() {
        return goToTab("Images", IMAGES_TAB_ID);
    }

    WebElement goToVideosTab() {
        return goToTab("Videos", VIDEOS_TAB_ID);
    }
}

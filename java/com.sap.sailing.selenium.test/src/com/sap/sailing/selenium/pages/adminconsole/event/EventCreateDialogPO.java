package com.sap.sailing.selenium.pages.adminconsole.event;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;
import com.sap.sailing.selenium.pages.gwt.CheckBoxPO;

public class EventCreateDialogPO extends DataEntryDialogPO {

    private static final DateFormat DATE_TIME_BOX_FORMAT = new SimpleDateFormat("dd/mm/yyyy hh:mm");

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
    
    EventCreateDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    void createEvent(String name, String description, String venue, Date start, Date end, boolean isPublic) {
        setTextValue(nameTextBox, name);
        setTextValue(descriptionTextArea, description);
        setTextValue(venueTextBox, venue);
        setTextValue(startDateTimeBox, DATE_TIME_BOX_FORMAT.format(start));
        setTextValue(endDateTimeBox, DATE_TIME_BOX_FORMAT.format(end));
        CheckBoxPO checkBox = new CheckBoxPO(driver, isPublicCheckBox);
        checkBox.setSelected(isPublic);
        pressOk();
    }
    
    private void setTextValue(WebElement webElement, String value) {
        webElement.clear();
        webElement.sendKeys(value);
    }

}

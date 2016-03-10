package com.sap.sailing.selenium.pages.adminconsole.event;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;
import com.sap.sailing.selenium.pages.gwt.BetterDateTimeBoxPO;
import com.sap.sailing.selenium.pages.gwt.CheckBoxPO;
import com.sap.sailing.selenium.pages.gwt.TextAreaPO;
import com.sap.sailing.selenium.pages.gwt.TextBoxPO;

public class EventCreateDialogPO extends DataEntryDialogPO {

    private static final DateFormat DATE_TIME_BOX_FORMAT = new SimpleDateFormat("dd/MM/yyyy hh:mm");

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
        TextBoxPO.create(driver, nameTextBox).appendText(name);
        TextAreaPO.create(driver, descriptionTextArea).appendText(description);
        TextBoxPO.create(driver, venueTextBox).appendText(venue);
        BetterDateTimeBoxPO.create(driver, startDateTimeBox).setText(DATE_TIME_BOX_FORMAT.format(start));
        BetterDateTimeBoxPO.create(driver, endDateTimeBox).setText(DATE_TIME_BOX_FORMAT.format(end));
        CheckBoxPO.create(driver, isPublicCheckBox).setSelected(isPublic);
        pressOk();
    }

}

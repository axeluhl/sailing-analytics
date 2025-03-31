package com.sap.sailing.selenium.pages.adminconsole.leaderboard;

import java.util.Date;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;
import com.sap.sailing.selenium.pages.gwt.DateAndTimeInputPO;

public class SetStartTimeDialogPO extends DataEntryDialogPO {
    
    @FindBy(how = BySeleniumId.class, using = "StartTimeTimeBox")
    private WebElement startTimeTimeBox;
    @FindBy(how = BySeleniumId.class, using = "AuthorNameTextBox")
    private WebElement authorNameTextBox;
    @FindBy(how = BySeleniumId.class, using = "AuthorPriorityIntegerBox")
    private WebElement authorPriorityIntegerBox;
    @FindBy(how = BySeleniumId.class, using = "RacingProcedureListBox")
    private WebElement RacingProcedureListBox;
    @FindBy(how = BySeleniumId.class, using = "AnvancePassIdCheckBox")
    private WebElement advancePassIdCheckBox;

    public SetStartTimeDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void setStartTimeValue(Date startTime) {
        DateAndTimeInputPO.create(driver, startTimeTimeBox).setValue(startTime, true);
    }

    public void pressSetStartTime() {
        this.pressOk();
    }
    
}

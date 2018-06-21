package com.sap.sailing.selenium.pages.autoplay;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;

public class AutoPlayUpcomingView extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "upComingDataLabel")
    private WebElement upComingDataLabel;

    public AutoPlayUpcomingView(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public String getText() {
        return upComingDataLabel.getText();
    }

}

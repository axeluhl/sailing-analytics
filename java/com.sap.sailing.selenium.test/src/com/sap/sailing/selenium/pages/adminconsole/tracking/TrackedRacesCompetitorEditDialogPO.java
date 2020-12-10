package com.sap.sailing.selenium.pages.adminconsole.tracking;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;
import com.sap.sailing.selenium.pages.gwt.CheckBoxPO;

public class TrackedRacesCompetitorEditDialogPO extends DataEntryDialogPO {
    @FindBy(how = BySeleniumId.class, using = "NameTextBox")
    private WebElement nameTextBox;
    
    @FindBy(how = BySeleniumId.class, using = "ShortNameTextBox")
    private WebElement shortNameTextBox;
    
    @FindBy(how = BySeleniumId.class, using = "OkButton")
    private WebElement okButton;

    private final CheckBoxPO withBoatCheckBox;

    public TrackedRacesCompetitorEditDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
        final List<WebElement> withBoatCheckBoxElement = driver.findElements(new BySeleniumId("WithBoatCheckBox"));
        if (withBoatCheckBoxElement.isEmpty()) {
            withBoatCheckBox = null;
        } else {
            withBoatCheckBox = CheckBoxPO.create(driver, withBoatCheckBoxElement.get(0));
        }
    }

    public void setNameTextBox(String name) {
        this.nameTextBox.clear();
        this.nameTextBox.sendKeys(name);
    }

    public void setShortNameTextBox(String name) {
        this.shortNameTextBox.clear();
        this.shortNameTextBox.sendKeys(name);
    }
    
    public void setWithBoat(boolean withBoat) {
        if (withBoatCheckBox != null) {
            this.withBoatCheckBox.setSelected(withBoat);
        }
    }
}

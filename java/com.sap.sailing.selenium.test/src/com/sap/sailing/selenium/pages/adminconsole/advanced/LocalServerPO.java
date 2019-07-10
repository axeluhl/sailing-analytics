package com.sap.sailing.selenium.pages.adminconsole.advanced;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;

public class LocalServerPO extends PageArea {

    public LocalServerPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    @FindBy(how = BySeleniumId.class, using = "isSelfServiceServerCheckbox")
    private WebElement isSelfServiceServerCheckbox;

    public void setSelfServiceServer(boolean selfService) {
        if (selfService != isSelfServiceServerCheckbox.isSelected()) {
            isSelfServiceServerCheckbox.click();
            awaitServerConfigurationUpdated();
        }
    }

    private void awaitServerConfigurationUpdated() {
        while (isServerConfigurationUpdating()) {
        }
    }

    public boolean isServerConfigurationUpdating() {
        final String updating = isSelfServiceServerCheckbox.getAttribute("updating");
        return "true".equals(updating);
    }

}

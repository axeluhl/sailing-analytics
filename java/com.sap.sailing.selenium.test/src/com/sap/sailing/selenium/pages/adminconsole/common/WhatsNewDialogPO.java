package com.sap.sailing.selenium.pages.adminconsole.common;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;

public class WhatsNewDialogPO extends DataEntryDialogPO {

    public WhatsNewDialogPO(final WebDriver driver, final WebElement element) {
        super(driver, element);
    }

    public void pressShowChangelog() {
        super.pressOk();
    }

}

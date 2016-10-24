package com.sap.sailing.selenium.pages.common;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ConfirmDialogPO extends DataEntryDialogPO {

    public ConfirmDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void pressYes() {
        super.pressOk();
    }
    
    public void pressNo() {
        super.pressCancel();
    }

}

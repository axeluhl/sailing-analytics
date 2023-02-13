package com.sap.sailing.selenium.pages.adminconsole.advanced;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.gwt.ListBoxPO;

public class FileStoragePO extends PageArea {
    public final static String SERVICE_LOCAL_STORAGE_VALUE = "Local Storage";

    public FileStoragePO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    @FindBy(how = BySeleniumId.class, using = "servicesListBox")
    private WebElement getServicesListBox;

    public void setLocalStorageService(String selfService) {
        ListBoxPO.create(driver, getServicesListBox).selectOptionByValue(selfService);
    }

}

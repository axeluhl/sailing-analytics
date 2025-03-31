package com.sap.sailing.selenium.pages.home.event;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;

public class MediaPO extends PageArea {
    
    @FindBy(how = BySeleniumId.class, using = "mediaAddButton")
    private WebElement mediaAddButton;

    public MediaPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public MediaUploadDialogPO clickAddMediaButton() {
        mediaAddButton.click();
        return getPO(MediaUploadDialogPO::new, "mediaUploadContent");
    }

}

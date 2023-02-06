package com.sap.sailing.selenium.pages.adminconsole.event;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;

public class VideosTabPO extends PageArea {

    @FindBy(how = BySeleniumId.class, using = "addVideoBtn")
    private WebElement addVideoBtn;
    
    VideosTabPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public UploadVideosDialogPO clickAddVideoBtn() {
        this.waitForElement("addVideoBtn");
        addVideoBtn.click();
        return getPO(UploadVideosDialogPO::new, "createVideoDialog");
    }
}

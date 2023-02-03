package com.sap.sailing.selenium.pages.adminconsole.event;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;

public class UploadVideosDialogPO extends PageArea {

    @FindBy(how = BySeleniumId.class, using = "urlTextBox")
    private WebElement urlTextBox;

    @FindBy(how = BySeleniumId.class, using = "mimeTypeListBox")
    private WebElement mimeTypeListBox;
    
    UploadVideosDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void enterUrl(String url) {
        urlTextBox.sendKeys(url);
        this.getWebElement().click();
    }
    
    public String getMimeTypeString() {
        return mimeTypeListBox.getText();
    }
}

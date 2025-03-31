package com.sap.sailing.selenium.pages.adminconsole.event;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.gwt.ListBoxPO;

public class UploadVideosDialogPO extends PageArea {

    @FindBy(how = BySeleniumId.class, using = "urlTextBox")
    private WebElement urlTextBox;

    @FindBy(how = BySeleniumId.class, using = "mimeTypeListBox")
    private WebElement mimeTypeListBox;

    @FindBy(how = BySeleniumId.class, using = "OkButton")
    private WebElement okButton;
    
    UploadVideosDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void enterUrl(String url) {
        urlTextBox.clear();
        urlTextBox.sendKeys(url);
        urlTextBox.sendKeys(Keys.TAB);
    }
    
    public void getMimeTypeString(String expectedValue) {
        waitUntil(() -> expectedValue.equals(ListBoxPO.create(driver, mimeTypeListBox).getSelectedOptionValue()));
    }
    
    public void pressOk() {
        okButton.click();
    }
}

package com.sap.sailing.selenium.pages.adminconsole.resultimporturls;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;

public class ResultImportUrlsAddDialogPO extends DataEntryDialogPO {

    @FindBy(how = BySeleniumId.class, using = "ResultImportUrlAddDialogTextBox")
    private WebElement urlTextBox;

    @FindBy(how = BySeleniumId.class, using = "OkButton")
    private WebElement okButton;

    public ResultImportUrlsAddDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public void setUrl(String name) {
        this.urlTextBox.clear();
        this.urlTextBox.sendKeys(name);
    }

}

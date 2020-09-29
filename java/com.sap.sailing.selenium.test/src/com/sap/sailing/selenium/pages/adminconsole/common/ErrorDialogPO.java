package com.sap.sailing.selenium.pages.adminconsole.common;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;

public class ErrorDialogPO extends PageArea {

    @FindBy(how = BySeleniumId.class, using = "ErrorDialogTitle")
    private WebElement title;
    
    @FindBy(how = BySeleniumId.class, using = "ErrorDialogCloseButton")
    private WebElement close;

    public ErrorDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public String getTitle() {
        return title.getText();
    }

    public void assertTitleContainsTextAndClose(String titlePart) {
        final String titleText = getTitle();
        if (!titleText.contains(titlePart)) {
            throw new RuntimeException("The expected title '" + titlePart + "' does not math the actual title '"
                    + titleText + "' in the error box.");
        }
        close.click();
    }

}

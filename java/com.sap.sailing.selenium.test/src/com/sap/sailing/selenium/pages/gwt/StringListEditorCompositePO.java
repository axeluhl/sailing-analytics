package com.sap.sailing.selenium.pages.gwt;

import org.openqa.selenium.ElementNotSelectableException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;

public class StringListEditorCompositePO extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "AddButton")
    private WebElement addValueButton;
    @FindBy(how = BySeleniumId.class, using = "InputSuggestBox")
    private WebElement valueInput;
    
    private StringListEditorCompositePO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public static StringListEditorCompositePO create(WebDriver driver, WebElement permissionsInput) {
        return new StringListEditorCompositePO(driver, permissionsInput);
    }
    
    public void addNewValue(String value) {
        SuggestBoxPO.create(driver, valueInput).appendText(value);
        addValueButton.click();
    }
    
    public void clickAddButtonAndExpectPermissionError() {
        if (!addValueButton.isEnabled()) {
            throw new ElementNotSelectableException("Add Button was disabled");
        } else {
            addValueButton.click();
        }
    }
}

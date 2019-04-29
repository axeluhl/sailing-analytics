package com.sap.sailing.selenium.pages.datamining;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;

public class DataMiningPanelPO extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "RunDataminingQueryButton")
    private WebElement runButton;
    
    @FindBy(how = BySeleniumId.class, using = "ExtractionFunctionSuggestBox")
    private WebElement extractionFunctionsuggestBox;
    
    public final WebDriver driver;

    public DataMiningPanelPO(WebDriver driver, WebElement element) {
        super(driver, element);
        this.driver = driver;
    }
    
    public boolean isRunButtonAvailable() {
        return this.runButton != null && runButton.isDisplayed();
    }
    
    public boolean isExtractionFunctionSuggestBoxAvailable() {
        return extractionFunctionsuggestBox != null && extractionFunctionsuggestBox.isDisplayed();
    }
}

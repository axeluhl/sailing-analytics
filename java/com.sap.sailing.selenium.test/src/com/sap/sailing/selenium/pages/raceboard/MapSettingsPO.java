package com.sap.sailing.selenium.pages.raceboard;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;
import com.sap.sailing.selenium.pages.gwt.CheckBoxPO;

public class MapSettingsPO extends DataEntryDialogPO {

    public MapSettingsPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void waitForWindUpUntil(boolean expected) {
        WebElement element = findElementBySeleniumId("windUpCheckBox");
        new CheckBoxPO(driver, element).waitForElementUntil(expected);
    }

    public void setWindUp(boolean selected) {
        WebElement element = findElementBySeleniumId("windUpCheckBox");
        new CheckBoxPO(driver, element).setSelected(selected);
    }

    public boolean isWindUp() {
        WebElement element = findElementBySeleniumId("windUpCheckBox");
        return new CheckBoxPO(driver, element).isSelected();
    }
    
    public void setShowOnlySelectedCompetitors(boolean selected) {
        WebElement element = findElementBySeleniumId("showOnlySelectedCompetitorsCheckBox");
        new CheckBoxPO(driver, element).setSelected(selected);
    }
    
    public boolean isShowOnlySelectedCompetitors() {
        WebElement element = findElementBySeleniumId("showOnlySelectedCompetitorsCheckBox");
        return new CheckBoxPO(driver, element).isSelected();
    }
    
    public void setShowWindStreamletOverlay(boolean selected) {
        WebElement element = findElementBySeleniumId("showWindStreamletOverlayCheckBox");
        new CheckBoxPO(driver, element).setSelected(selected);
    }
    
    public boolean isShowWindStreamletOverlay() {
        WebElement element = findElementBySeleniumId("showWindStreamletOverlayCheckBox");
        return new CheckBoxPO(driver, element).isSelected();
    }
    
    public void setTransparentHoverlines(boolean selected) {
        WebElement element = findElementBySeleniumId("transparentHoverlinesCheckBox");
        new CheckBoxPO(driver, element).setSelected(selected);
    }
    
    public boolean isTransparentHoverlines() {
        WebElement element = findElementBySeleniumId("transparentHoverlinesCheckBox");
        return new CheckBoxPO(driver, element).isSelected();
    }
    
    public void setShowSimulationOverlay(boolean selected) {
        WebElement element = findElementBySeleniumId("showSimulationOverlayCheckBox");
        new CheckBoxPO(driver, element).setSelected(selected);
    }
    
    public boolean isShowSimulationOverlay() {
        WebElement element = findElementBySeleniumId("showSimulationOverlayCheckBox");
        return new CheckBoxPO(driver, element).isSelected();
    }
    
    public boolean isShowSimulationOverlayCheckBoxVisible() {
        List<WebElement> foundElements = findElementsBySeleniumId(context, "showSimulationOverlayCheckBox");
        return !foundElements.isEmpty();
    }
}

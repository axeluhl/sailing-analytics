package com.sap.sailing.selenium.pages.raceboard;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;
import com.sap.sailing.selenium.pages.gwt.CheckBoxPO;

public class MapSettingsPO extends DataEntryDialogPO {

    @FindBy(how = BySeleniumId.class, using = "windUpCheckBox")
    private WebElement windUpCheckBox;
    
    @FindBy(how = BySeleniumId.class, using = "showOnlySelectedCompetitorsCheckBox")
    private WebElement showOnlySelectedCompetitorsCheckBox;

    public MapSettingsPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public void setWindUp(boolean selected) {
        new CheckBoxPO(driver, windUpCheckBox).setSelected(selected);
    }

    public boolean isWindUp() {
        return new CheckBoxPO(driver, windUpCheckBox).isSelected();
    }
    
    public void setShowOnlySelectedCompetitors(boolean selected) {
        new CheckBoxPO(driver, showOnlySelectedCompetitorsCheckBox).setSelected(selected);
    }
    
    public boolean isShowOnlySelectedCompetitors() {
        return new CheckBoxPO(driver, showOnlySelectedCompetitorsCheckBox).isSelected();
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

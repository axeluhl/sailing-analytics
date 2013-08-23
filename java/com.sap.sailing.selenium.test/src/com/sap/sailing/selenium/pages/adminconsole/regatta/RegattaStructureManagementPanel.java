package com.sap.sailing.selenium.pages.adminconsole.regatta;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaList.RegattaDescriptor;

public class RegattaStructureManagementPanel extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "AddRegattaButton")
    WebElement addRegattaButton;
    
    @FindBy(how = BySeleniumId.class, using = "RemoveRegattaButton")
    WebElement removeRegattaButton;
    
    @FindBy(how = BySeleniumId.class, using = "RegattaListSection")
    WebElement regattaList;
    
    @FindBy(how = BySeleniumId.class, using = "RegattaDetailsSection")
    WebElement regattaDetails;
    
    public RegattaStructureManagementPanel(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public RegattaCreationDialog startRegattaCreation() {
        this.addRegattaButton.click();
        
        WebElement dialog = findElementBySeleniumId(this.driver, "CreateRegattaDialog");
        
        return new RegattaCreationDialog(this.driver, dialog);
    }
    
    public void createRegatta(RegattaDescriptor regatta) {
        RegattaCreationDialog dialog = startRegattaCreation();
        dialog.setRegattaName(regatta.getName());
        dialog.setBoatClass(regatta.getBoatClass());
        dialog.pressOk();
    }
    
    public void removeRegatta(RegattaDescriptor regatta) {
        // Select the regatta and click the remove button
    }
    
    public void removeRegattas(List<RegattaDescriptor> regattas) {
        // Select all regattas and click the remove button
    }
    
    public RegattaList getRegattaList() {
        return new RegattaList(this.driver, this.regattaList);
    }
    
    public RegattaDetails getRegattaDetails() {
        return new RegattaDetails(this.driver, this.regattaDetails);
    }
}

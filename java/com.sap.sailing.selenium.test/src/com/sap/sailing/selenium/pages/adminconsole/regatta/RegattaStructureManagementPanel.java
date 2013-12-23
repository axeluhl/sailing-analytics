package com.sap.sailing.selenium.pages.adminconsole.regatta;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;

import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaListComposite.RegattaDescriptor;

public class RegattaStructureManagementPanel extends PageArea {
    public static final String DEFAULT_SERIES_NAME = "Default"; //$NON-NLS-1$
    
    @FindBy(how = BySeleniumId.class, using = "AddRegattaButton")
    WebElement addRegattaButton;
    
    @FindBy(how = BySeleniumId.class, using = "RemoveRegattaButton")
    WebElement removeRegattaButton;
    
    @FindBy(how = BySeleniumId.class, using = "RegattaListComposite")
    WebElement regattaList;
    
    @FindBy(how = BySeleniumId.class, using = "RegattaDetailsComposite")
    WebElement regattaDetails;
    
    public RegattaStructureManagementPanel(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public RegattaCreateDialog startRegattaCreation() {
        this.addRegattaButton.click();
        
        WebElement dialog = findElementBySeleniumId(this.driver, "RegattaCreateDialog"); //$NON-NLS-1$
        
        return new RegattaCreateDialog(this.driver, dialog);
    }
    
    /**
     * <p>Creates a regatta with 1 series (named "Default").</p>
     * 
     * @param regatta
     */
    public void createRegatta(RegattaDescriptor regatta) {
        RegattaCreateDialog createRegattaDialog = startRegattaCreation();
        createRegattaDialog.setRegattaName(regatta.getName());
        createRegattaDialog.setBoatClass(regatta.getBoatClass());
        
        SeriesCreateDialog addSeriesDialog = createRegattaDialog.addSeries();
        addSeriesDialog.setSeriesName(DEFAULT_SERIES_NAME);
        
        addSeriesDialog.pressOk();
        
        // QUESTION: How do we handle an error (here or in the dialog)?
        createRegattaDialog.pressOk();
    }
    
    public void removeRegatta(RegattaDescriptor regatta) {
        // Select the regatta and click the remove button
    }
    
    public void removeRegattas(List<RegattaDescriptor> regattas) {
        // Select all regattas and click the remove button
    }
    
    public RegattaListComposite getRegattaList() {
        return new RegattaListComposite(this.driver, this.regattaList);
    }
    
    public void selectRegatta(RegattaDescriptor regatta) {
        
    }
    
    public RegattaDetailsComposite getRegattaDetails(RegattaDescriptor regatta) {
        RegattaListComposite regattaList = getRegattaList();
        regattaList.selectRegatta(regatta);
        
        return getRegattaDetails();
    }
    
    public RegattaDetailsComposite getRegattaDetails() {
        if(this.regattaDetails.isDisplayed())
            return new RegattaDetailsComposite(this.driver, this.regattaDetails);
        
        return null;
    }
}

package com.sap.sailing.selenium.pages.adminconsole.regatta;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;
import com.sap.sailing.selenium.pages.gwt.CheckBoxPO;

public class RegattaEditDialogPO extends DataEntryDialogPO {
    @FindBy(how = BySeleniumId.class, using = "RacingProcedureConfigurationCheckBox")
    private WebElement regattaConfigurationCheckbox;
    
    @FindBy(how = BySeleniumId.class, using = "RacingProcedureConfigurationEditButton")
    private WebElement regattaConfigurationButton;

    @FindBy(how = BySeleniumId.class, using = "EventListBox")
    private WebElement eventListBox;

    @FindBy(how = BySeleniumId.class, using = "UseStartTimeInferenceCheckBox")
    private WebElement useStartTimeInferenceCheckbox;

    @FindBy(how = BySeleniumId.class, using = "CanBoatsOfCompetitorsChangePerRaceCheckBox")
    private WebElement canBoatsOfCompetitorsChangePerRaceCheckbox;
    
    @FindBy(how = BySeleniumId.class, using = "CourseAreaListBox")
    private WebElement courseAreaListBox;
    
    @FindBy(how = BySeleniumId.class, using = "AddSeriesButton")
    private WebElement addSeriesButton;
    
    @FindBy(how = BySeleniumId.class, using = "registrationLinkWithQRCodeOpenButton")
    private WebElement registrationLinkWithQRCodeOpenButton;

    public RegattaEditDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public void setUseStartTimeInference(boolean b) {
        CheckBoxPO checkbox = new CheckBoxPO(driver, useStartTimeInferenceCheckbox);
        checkbox.setSelected(b);
    }

    public void setCanBoatsOfCompetitorsChangePerRaceCheckBox(boolean b) {
        CheckBoxPO checkbox = new CheckBoxPO(driver, canBoatsOfCompetitorsChangePerRaceCheckbox);
        checkbox.setSelected(b);
    }

    public SeriesCreateDialogPO addSeries() {
        this.addSeriesButton.click();
        
        WebElement dialog = findElementBySeleniumId(this.driver, "SeriesCreateDialog");
        
        return new SeriesCreateDialogPO(this.driver, dialog);
    }
    
    public RegistrationLinkWithQRCodeDialogPO configureRegistrationURL() {
        registrationLinkWithQRCodeOpenButton.click();
        WebElement dialog = findElementBySeleniumId(this.driver, "RegistrationLinkWithQRCodeDialog");
        return new RegistrationLinkWithQRCodeDialogPO(this.driver, dialog);
    }
    
    public void addSeries(String seriesName) {
        SeriesCreateDialogPO seriesCreateDialog = addSeries();
        seriesCreateDialog.setSeriesName(seriesName);
        seriesCreateDialog.pressOk();
    }
}

package com.sap.sailing.selenium.pages.leaderboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.common.CSSHelper;
import com.sap.sailing.selenium.pages.gwt.CheckBoxPO;
import com.sap.sailing.selenium.pages.gwt.RadioButtonPO;

public class LeaderboardSettingsPanelPO extends PageArea {
    
    @FindBy(how = BySeleniumId.class, using = "RaceSelectionSettingsPanel")
    private WebElement raceSelectionSettingsPanel;
    
    @FindBy(how = BySeleniumId.class, using = "ExplicitRaceSelectionRadioButton")
    private WebElement explicitRaceSelectionRadioButton;
    
    @FindBy(how = BySeleniumId.class, using = "MostCurrentRacesSelectionRadioButton")
    private WebElement mostCurrentRacesSelectionRadioButton;
    
    @FindBy(how = BySeleniumId.class, using = "OverallDetailsSettingsPanel")
    private WebElement overallDetailsSettingsPanel;
    
    @FindBy(how = BySeleniumId.class, using = "RaceDetailsSettingsPanel")
    private WebElement raceDetailsSettingsPanel;
    
    @FindBy(how = BySeleniumId.class, using = "LegDetailsSettingsPanel")
    private WebElement legDetailsSettingsPanel;
    
    @FindBy(how = BySeleniumId.class, using = "ManeuverSettingsPanel")
    private WebElement maneuverSettingsPanel;

    @FindBy(how = BySeleniumId.class, using = "TimingSettingsPanel")
    private WebElement timingSettingsPanel;
    
    
    
    public LeaderboardSettingsPanelPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void setRacesToDisplay(List<String> races) {
        RadioButtonPO button = new RadioButtonPO(this.driver, this.explicitRaceSelectionRadioButton);
        
        if(!button.isSelected()) {
            button.setSelected(true);
        }
        
        for(WebElement element : findAllCheckboxesForSettings(this.raceSelectionSettingsPanel)) {
            CheckBoxPO checkbox = new CheckBoxPO(this.driver, element);
            checkbox.setSelected(races.contains(checkbox.getLabel()));
        }
    }
    
    public void setNumberOfRacesToDisplay(int numberOfRaces) {
        RadioButtonPO button = new RadioButtonPO(this.driver, this.mostCurrentRacesSelectionRadioButton);

        if(!button.isSelected()) {
            button.setSelected(true);
        }
        
        WebElement textField = findElementBySeleniumId(this.raceSelectionSettingsPanel, "NumberOfMostCurrentRacesIntegerBox");
        textField.clear();
        textField.sendKeys(Integer.toString(numberOfRaces));
    }
    
    public boolean isNumberOfRacesToDisplaySelected() {
        RadioButtonPO button = new RadioButtonPO(this.driver, this.mostCurrentRacesSelectionRadioButton);
        return button.isSelected();
    }
    
    public int getNumberOfRacesToDisplaySelected() {
        WebElement textField = findElementBySeleniumId(this.raceSelectionSettingsPanel, "NumberOfMostCurrentRacesIntegerBox");
        return Integer.valueOf(textField.getAttribute("value"));
    }
    
    public void setRefreshInterval(int seconds) {
        WebElement textField = findElementBySeleniumId(this.timingSettingsPanel, "RefreshIntervalLongBox");
        textField.clear();
        textField.sendKeys(Integer.toString(seconds));
    }
    
    public int getRefreshInterval() {
        WebElement textField = findElementBySeleniumId(this.timingSettingsPanel, "RefreshIntervalLongBox");
        return Integer.valueOf(textField.getAttribute("value"));
    }
    
    public void selectDetailsAndDeselectOther(DetailCheckboxInfo...detailsToSelect) {
        Arrays.sort(detailsToSelect);
        for (DetailCheckboxInfo detail : DetailCheckboxInfo.values()) {
            boolean selected = Arrays.binarySearch(detailsToSelect, detail) >= 0;
            setCheckboxValue(detail.getId(), selected);
        }
    }
    
    public DetailCheckboxInfo[] getSelectedDetails() {
        List<DetailCheckboxInfo> selectedDetails = new ArrayList<>();
        for (DetailCheckboxInfo detail : DetailCheckboxInfo.values()) {
            String checkboxId = detail.getId();
            if(getCheckboxValue(checkboxId)) {
                selectedDetails.add(detail);
            }
        }
        return selectedDetails.toArray(new DetailCheckboxInfo[selectedDetails.size()]);
    }
    
    public void showAllOverallDetails() {
        setSelectionForAllSettings(this.overallDetailsSettingsPanel, true);
    }
    
    public void showAllRaceDetails() {
        setSelectionForAllSettings(this.raceDetailsSettingsPanel, true);
    }
    
    public void showAllLegDetails() {
        setSelectionForAllSettings(this.legDetailsSettingsPanel, true);
    }
    
    public void showAllManeuverDetails() {
        setSelectionForAllSettings(this.maneuverSettingsPanel, true);
    }
    
    private void setSelectionForAllSettings(WebElement settingsPanel, boolean selected) {
        for(WebElement element : findAllCheckboxesForSettings(settingsPanel)) {
            CheckBoxPO checkbox = new CheckBoxPO(this.driver, element);
            checkbox.setSelected(selected);
        }
    }
    
    private List<WebElement> findAllCheckboxesForSettings(WebElement settingsPanel) {
        List<WebElement> e = settingsPanel.findElements(By.xpath(".//span[" + CSSHelper.containsCSSClassPredicate("gwt-CheckBox") + "]"));
        return e;
    }
    
    public void setCheckboxValue(String checkboxId, boolean selected) {
        WebElement element = findElementBySeleniumId(checkboxId);
        CheckBoxPO checkbox = new CheckBoxPO(driver, element);
        checkbox.setSelected(selected);
    }
    
    public boolean getCheckboxValue(String checkboxId) {
        WebElement element = findElementBySeleniumId(checkboxId);
        CheckBoxPO checkbox = new CheckBoxPO(driver, element);
        return checkbox.isSelected();
    }
    
}

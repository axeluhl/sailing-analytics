package com.sap.sailing.selenium.pages.regattaoverview;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;
import com.sap.sailing.selenium.pages.gwt.CheckBoxPO;

public class RegattaOverviewSettingsDialogPO extends DataEntryDialogPO {

    @FindBy(how = BySeleniumId.class, using = "ShowOnlyRacesOfSameDayCheckBox")
    private WebElement showOnlyRacesOfSameDayCheckBox;

    @FindBy(how = BySeleniumId.class, using = "ShowOnlyCurrentlyRunningRacesCheckBox")
    private WebElement showOnlyCurrentlyRunningRacesCheckBox;

    @FindBy(how = BySeleniumId.class, using = "CourseAreaPanel")
    private WebElement courseAreaPanel;

    @FindBy(how = BySeleniumId.class, using = "RegattaNamesPanel")
    private WebElement regattaNamesPanel;

    private final List<WebElement> courseAreaCheckBoxes;
    private final List<WebElement> regattaNamesCheckBoxes;

    public RegattaOverviewSettingsDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
        courseAreaCheckBoxes = courseAreaPanel.findElements(By.className(CheckBoxPO.CSS_CLASS));
        regattaNamesCheckBoxes = regattaNamesPanel.findElements(By.className(CheckBoxPO.CSS_CLASS));
    }

    public void setShowOnlyRacesOfSameDay(boolean selected) {
        new CheckBoxPO(driver, showOnlyRacesOfSameDayCheckBox).setSelected(selected);
    }

    public boolean isShowOnlyRacesOfSameDay() {
        return new CheckBoxPO(driver, showOnlyRacesOfSameDayCheckBox).isSelected();
    }

    public void setShowOnlyCurrentlyRunningRaces(boolean selected) {
        new CheckBoxPO(driver, showOnlyCurrentlyRunningRacesCheckBox).setSelected(selected);
    }

    public boolean isShowOnlyCurrentlyRunningRaces() {
        return new CheckBoxPO(driver, showOnlyCurrentlyRunningRacesCheckBox).isSelected();
    }

    private List<String> getSelectedCheckBoxLabels(List<WebElement> checkBoxList) {
        List<String> selectedCheckBoxLabes = new ArrayList<>();
        for (WebElement element : checkBoxList) {
            CheckBoxPO checkbox = CheckBoxPO.create(driver, element);
            if (checkbox.isSelected()) {
                selectedCheckBoxLabes.add(checkbox.getLabel());
            }
        }
        return selectedCheckBoxLabes;
    }

    private List<String> getAvailableCheckBoxLabels(List<WebElement> checkBoxList) {
        List<String> selectedCheckBoxLabes = new ArrayList<>();
        for (WebElement element : checkBoxList) {
            CheckBoxPO checkbox = CheckBoxPO.create(driver, element);
            selectedCheckBoxLabes.add(checkbox.getLabel());
        }
        return selectedCheckBoxLabes;
    }

    private void selectCheckBoxLabelsAndDeselectOther(List<WebElement> checkBoxList, List<String> checkBoxLabels) {
        for (WebElement element : checkBoxList) {
            CheckBoxPO checkbox = CheckBoxPO.create(driver, element);
            if (checkBoxLabels.contains(checkbox.getLabel())) {
                checkbox.setSelected(true);
            } else {
                checkbox.setSelected(false);
            }
        }
    }

    public List<String> getSelectedCourseAreas() {
        return getSelectedCheckBoxLabels(courseAreaCheckBoxes);
    }

    public List<String> getSelectedRegattaNames() {
        return getSelectedCheckBoxLabels(regattaNamesCheckBoxes);
    }

    public List<String> getAvailableCourseAreas() {
        return getAvailableCheckBoxLabels(courseAreaCheckBoxes);
    }

    public List<String> getAvailableRegattaNames() {
        return getAvailableCheckBoxLabels(regattaNamesCheckBoxes);
    }

    public void selectCourseAreasAndDeselectOther(List<String> courseAreaLabelsToSelect) {
        selectCheckBoxLabelsAndDeselectOther(courseAreaCheckBoxes, courseAreaLabelsToSelect);
    }

    public void selectRegattaNamesAndDeselectOther(List<String> courseAreaLabelsToSelect) {
        selectCheckBoxLabelsAndDeselectOther(regattaNamesCheckBoxes, courseAreaLabelsToSelect);
    }

}

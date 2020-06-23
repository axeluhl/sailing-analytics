package com.sap.sailing.selenium.pages.adminconsole;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.gwt.CheckBoxPO;

public class CourseAreaSelectionPO extends PageArea {
    private final List<CheckBoxPO> courseAreaCheckBoxes;
    
    public static CourseAreaSelectionPO create(WebDriver driver, WebElement element) {
        return new CourseAreaSelectionPO(driver, element);
    }
    
    public CourseAreaSelectionPO(WebDriver driver, WebElement element) {
        super(driver, element);
        courseAreaCheckBoxes = new ArrayList<>();
        for (final WebElement checkbox : element.findElements(By.className("gwt-CheckBox"))) {
            courseAreaCheckBoxes.add(CheckBoxPO.create(driver, checkbox));
        }
    }
    
    public Iterable<String> getSelectedCourseAreaNames() {
        final List<String> result = new ArrayList<>();
        for (final CheckBoxPO checkbox : courseAreaCheckBoxes) {
            if (checkbox.isSelected()) {
                result.add(checkbox.getLabel());
            }
        }
        return result;
    }

    public void selectCourseAreaByLabel(String courseAreaName) {
        for (final CheckBoxPO checkbox : courseAreaCheckBoxes) {
            if (checkbox.getLabel().equals(courseAreaName)) {
                checkbox.setSelected(true);
            }
        }
    }
}

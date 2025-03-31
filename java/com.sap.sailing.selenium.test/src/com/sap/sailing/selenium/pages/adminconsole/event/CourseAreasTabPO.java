package com.sap.sailing.selenium.pages.adminconsole.event;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.gwt.SuggestBoxPO;

public class CourseAreasTabPO extends PageArea {

    @FindBy(how = BySeleniumId.class, using = "InputSuggestBox")
    private WebElement newCourseInputSuggestBox;
    
    @FindBy(how = BySeleniumId.class, using = "AddButton")
    private WebElement addCourseButton;
    
    CourseAreasTabPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    void addNewCourse(String name) {
        SuggestBoxPO.create(driver, newCourseInputSuggestBox).appendText(name);
        addCourseButton.click();
    }
    
}

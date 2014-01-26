package com.sap.sailing.selenium.test.adminconsole.pages;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.test.PageArea;

public class TracTracStartTrackingPanel extends PageArea {
    private static final Logger logger = Logger.getLogger(TracTracStartTrackingPanel.class.getName());
    
    @FindBy(how = BySeleniumId.class, using = "TrackWind")
    private WebElement trackWindCheckbox;

    @FindBy(how = BySeleniumId.class, using = "FilterRaces")
    private WebElement filterTrackableRacesField;
    
    @FindBy(how = BySeleniumId.class, using = "StartTracking")
    private WebElement startTrackingButton;
    
    public TracTracStartTrackingPanel(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void setTrackWind(boolean trackWind) {
        setSelection(trackWindCheckbox, trackWind);
    }

    /**
     * <p>
     * Returns the list of all available trackable races. This list will be empty if no race is available or if no race
     * was specified before.
     * </p>
     * 
     * @return The list of all available trackable races.
     */
    public List<WebElement> getTrackableRaces() {
        WebElement availableRacesTabel = findElementBySeleniumId(this.context, "RacesTable"); //$NON-NLS-1$
        List<WebElement> elements = availableRacesTabel.findElements(By.xpath("./tbody/tr")); //$NON-NLS-1$
        Iterator<WebElement> iterator = elements.iterator();
        while (iterator.hasNext()) {
            WebElement element = iterator.next();
            if (!element.isDisplayed()) {
                iterator.remove();
            }
        }
        return elements;
    }
    
    /**
     * <p>Sets the filter for the trackable races. After the filter is set you can obtain the new resulting list via
     *   {@link #getTrackableRaces}</p>
     * 
     * @param filter
     *   The filter to apply to the trackable races.
     */
    public void setFilterForTrackableRaces(String filter) {
        this.filterTrackableRacesField.clear();
        this.filterTrackableRacesField.sendKeys(filter);
    }
    
    private void setSelection(WebElement checkbox, boolean selected) {
        WebElement input = checkbox.findElement(By.tagName("input"));
        if (input.isSelected() != selected) {
            input.click();
        }
    }

    private WebElement getTrackableRace(String regattaName, String raceName) {
        for (WebElement trackableRace : getTrackableRaces()) {
            List<WebElement> fields = trackableRace.findElements(By.tagName("td"));
            if (regattaName.equals(fields.get(0).getText()) && raceName.equals(fields.get(1).getText())) {
                return trackableRace;
            }
        }
        return null;
    }

    public void startTracking(String regattaName, String raceName) {
        getTrackableRace(regattaName, raceName).click();
        startTrackingButton.click();
        try {
            // there may be an alert that there already is some default regatta; ignore it
            Alert alert = driver.switchTo().alert();
            if (alert != null) {
                alert.accept();
            }
        } catch (NoAlertPresentException e) {
            logger.log(Level.WARNING, "No alert found when starting to track race; probably there is no regatta for its class yet", e);
        }
    }

}

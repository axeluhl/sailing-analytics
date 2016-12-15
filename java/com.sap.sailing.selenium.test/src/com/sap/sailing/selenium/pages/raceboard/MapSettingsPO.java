package com.sap.sailing.selenium.pages.raceboard;

import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;

public class MapSettingsPO extends PageArea {

    @FindBy(how = BySeleniumId.class, using = "raceMapSettingsWindChart")
    private WebElement raceMapSettingsWindChart;

    @FindBy(how = BySeleniumId.class, using = "SaveButton")
    private WebElement saveButton;

    public MapSettingsPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }


    public void setWindChart(boolean b) {
        if (isWindChartSelected() != b) {
            raceMapSettingsWindChart.click();
        }
    }

    public void makeDefault() throws InterruptedException {
        saveButton.click();
        waitForAlertAndDispose(driver);
    }

    void waitForAlertAndDispose(WebDriver driver) throws InterruptedException {
        while (true) {
            try {
                Alert alert = driver.switchTo().alert();
                alert.dismiss();
                break;
            } catch (NoAlertPresentException e) {
                Thread.sleep(1000);
                continue;
            }
        }
    }

    public boolean isWindChartSelected() {
        String checked = raceMapSettingsWindChart.getAttribute("selenium_checkbox");
        return String.valueOf(Boolean.TRUE).equals(checked);
    }
}

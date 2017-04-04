package com.sap.sailing.selenium.pages.adminconsole.wind;

import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;

import com.google.common.base.Function;
import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;

public class WindPanelPO extends PageArea {

    @FindBy(how = BySeleniumId.class, using = "ImportWindFromIgtimi")
    private WebElement importWindFromIgtimi;
    
    @FindBy(how = BySeleniumId.class, using = "IgtimiImportResultReport")
    private WebElement igtimiImportResultReport;
    
    public WindPanelPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void importWindFromIgtimi() throws InterruptedException {
        importWindFromIgtimi.click();
        
        int i = 0;
        while (i < DEFAULT_WAIT_TIMEOUT_SECONDS) {
            i++;
            try {
                Alert alert = driver.switchTo().alert();
                alert.accept();
                break;
            } catch (NoAlertPresentException e) {
                Thread.sleep(1000);
                continue;
            }
        }
        if (i >= DEFAULT_WAIT_TIMEOUT_SECONDS) {
            throw new NoAlertPresentException();
        }
        
        FluentWait<String> wait = createFluentWait("The following number of fixes were imported for the listed races:", /* waiting 10 minutes at most */ 10 * 60, DEFAULT_POLLING_INTERVAL);
        wait.until(new Function<String, Object>() {
            @Override
            public Object apply(String message) {
                try {
                    String statusText = igtimiImportResultReport.getText();
                    return statusText.contains(message);
                } catch (TimeoutException exception) {
                    return Boolean.FALSE;
                }
            }
        });
    }
}

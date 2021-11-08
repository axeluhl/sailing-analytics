package com.sap.sailing.selenium.pages.datamining;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.HostPage;
import com.sap.sailing.selenium.pages.HostPageWithAuthentication;
import com.sap.sailing.selenium.pages.PageObject;

/**
 * {@link PageObject} representing the SAP Sailing datamining page.
 */
public class DataMiningPage extends HostPageWithAuthentication {

    @FindBy(how = BySeleniumId.class, using = "DataMiningPanel")
    private WebElement dataminingPanel;
    
    /**
     * Navigates to the data mining page and provides the corresponding {@link PageObject}.
     * 
     * @param driver
     *            the {@link WebDriver} to use
     * @param root
     *            the context root of the application
     * @return the {@link PageObject} for the data mining page
     */
    public static DataMiningPage goToPage(WebDriver driver, String root) {
        return goToDataMiningUrl(driver, root + "gwt/DataMining.html");
    }

    /**
     * Navigates to the given data mining URL and provides the corresponding {@link PageObject}.
     * 
     * @param driver
     *            the {@link WebDriver} to use
     * @param url
     *            the desired destination URL
     * @return the {@link PageObject} for the data mining page
     */
    public static DataMiningPage goToDataMiningUrl(WebDriver driver, String url) {
        return HostPage.goToUrl(DataMiningPage::new, driver, url);
    }

    private DataMiningPage(WebDriver driver) {
        super(driver);
    }

    public DataMiningPanelPO getDataMiningPanel() {
        return new DataMiningPanelPO(super.driver, dataminingPanel);
    }

}

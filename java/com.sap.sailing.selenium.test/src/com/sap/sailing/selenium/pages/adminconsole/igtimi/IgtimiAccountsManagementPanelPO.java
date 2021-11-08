package com.sap.sailing.selenium.pages.adminconsole.igtimi;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;

/**
 * <p>The page object representing the TracTrac Events tab.</p>
 * 
 * @author
 *   D049941
 */
public class IgtimiAccountsManagementPanelPO extends PageArea {
    
    @FindBy(how = BySeleniumId.class, using = "addIgtimiAccount")
    private WebElement addIgtimiAccount;
    
    /**
     * <p></p>
     * 
     * @param driver
     *   
     * @param element
     *   
     */
    public IgtimiAccountsManagementPanelPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public AddIgtimiAccountDialogPO openAddIgtimiAccountDialog() {
        addIgtimiAccount.click();
        return getPO(AddIgtimiAccountDialogPO::new, "AddIgtimiAccountDialog");
    }
    
    public void addAccount(String email, String password) throws InterruptedException {
        AddIgtimiAccountDialogPO addIgtimiAccountDialog = openAddIgtimiAccountDialog();
        addIgtimiAccountDialog.setEmail(email);
        addIgtimiAccountDialog.setPassword(password);
        addIgtimiAccountDialog.pressOk();
        
        waitForNotificationAndDismiss();
    }
}

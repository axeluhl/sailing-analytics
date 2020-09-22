package com.sap.sailing.selenium.pages.adminconsole.security;

import org.openqa.selenium.By.ByName;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.gwt.CellTablePO;
import com.sap.sailing.selenium.pages.gwt.DataEntryPO;

public abstract class DataEntryWithSecurityActionsPO extends DataEntryPO {

    @FindBy(how = ByName.class, using = "CHANGE_ACL")
    private WebElement aclButton;
    
    public DataEntryWithSecurityActionsPO(CellTablePO<?> table, WebElement element) {
        super(table, element);
    }
    
    protected DataEntryWithSecurityActionsPO() {
        super();
    }

    public AclPopupPO openAclPopup() {
        aclButton.click();
        waitForElement("AclDialog");
        return new AclPopupPO(this.driver, driver.findElement(new BySeleniumId(("AclDialog"))));
    }

}

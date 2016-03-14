package com.sap.sailing.selenium.pages.home.regatta;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.common.LabelTypePO;

public class RegattaListItemPO extends PageArea {
    
    private final RegattaHeaderPO regattaHeader;

    public RegattaListItemPO(WebDriver driver, WebElement element) {
        super(driver, element);
        this.regattaHeader = getChildPO(RegattaHeaderPO::new, "RegattaHeaderPanel");
    }
    
    public RegattaHeaderPO getRegattaHeader() {
        return regattaHeader;
    }
    
    public class RegattaHeaderPO extends PageArea {
        private RegattaHeaderPO(WebDriver driver, WebElement element) {
            super(driver, element);
        }
        
        public String getRegattaName() {
            return findElementBySeleniumId("RegattaNameSpan").getText();
        }
        
        public LabelTypePO getRegattaStateLabel() {
            return getChildPO(LabelTypePO::new, "RegattaStateLabelDiv");
        }
        
    }

}

package com.sap.sailing.selenium.pages.leaderboard;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;

public class CompetitorChartsSettingsDialogPO extends PageArea {
    
    @FindBy(how = BySeleniumId.class, using = "ChartTypeListBox")
    private WebElement chartTypeListBox;
    
    public CompetitorChartsSettingsDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void selectChartType(String type) {
        Select select = new Select(this.chartTypeListBox);
        //select.selectByVisibleText(type);
        select.selectByValue(type);
    }
    
    public List<String> getChartTypes() {
        List<String> types = new ArrayList<>();
        
        Select select = new Select(this.chartTypeListBox);
        for(WebElement option : select.getOptions()) {
            types.add(option.getAttribute("value"));
        }
        
        return types;
    }
}

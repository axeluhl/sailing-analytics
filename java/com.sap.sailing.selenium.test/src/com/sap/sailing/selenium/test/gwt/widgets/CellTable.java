package com.sap.sailing.selenium.test.gwt.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.pages.PageObject;

/**
 * <p></p>
 * 
 * @author
 *   D049941
 */
public class CellTable extends PageObject {
    /**
     * <p></p>
     * 
     * @param driver
     *   
     * @param element
     *   
     */
    public CellTable(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public List<String> getHeaders() {
        List<String> headers = new ArrayList<>();
        
        for(WebElement header : findHeaders()) {
            headers.add(header.getText());
        }
        
        return headers;
    }
    
    public void clickHeader(String name) {
        for(WebElement header : findHeaders()) {
            if(name.equals(header.getText()))
                header.click();
        }
    }
    
    public List<WebElement> getRows() {
        List<WebElement> bodies = this.context.findElements(By.tagName("tbody"));
        
        for(WebElement body : bodies) {
            if(!body.isDisplayed())
                continue;
            
            List<WebElement> rows = body.findElements(By.tagName("tr"));
            
            Iterator<WebElement> iterator = rows.iterator();
            
            while(iterator.hasNext()) {
                WebElement row = iterator.next();
                
                if(isRowForLoadingAnimation(row))
                    iterator.remove();
            }
            
            return rows;
        }
        
        return Collections.emptyList();
    }
    
    @Override
    protected void verify() {
        // TODO: Verify that the context represents a GWT CellTable
        String tagName = ((WebElement) this.context).getTagName();
        
        if(!tagName.equalsIgnoreCase("table"))
            throw new IllegalArgumentException("WebElement does not represent a table");
    }
    
    private List<WebElement> findHeaders() {
        return this.context.findElements(By.tagName("th"));
    }
    
    private boolean isRowForLoadingAnimation(WebElement row) {
        List<WebElement> images = row.findElements(By.xpath(".//td/div/div/div/img"));
        
        if(images.isEmpty() || images.size() > 1)
            return false;
        
        WebElement image = images.get(0);
        System.out.println(image.getCssValue("background-image"));
        
        return true;
    }
}

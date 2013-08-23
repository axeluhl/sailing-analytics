package com.sap.sailing.selenium.pages.gwt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.sap.sailing.selenium.pages.PageArea;

/**
 * <p></p>
 * 
 * @author
 *   D049941
 */
public class CellTable extends PageArea {
    public enum SortingOrder {
        Ascending,
        Descending,
        None;
    }
    
    private static final String ASCENDING_IMAGE = "url(\"data:image/png;base64," +                       //$NON-NLS-1$
            "iVBORw0KGgoAAAANSUhEUgAAAAsAAAAHCAYAAADebrddAAAAiklEQVR42mNgwALyKrumFRf3iDAQAvmVXVVAxf/" +  //$NON-NLS-1$
            "zKjq341WYV95hk1fZ+R+MK8C4HqtCkLW5FZ2PQYpyK6AaKjv/5VV1OmIozq3s3AFR0AXFUNMrO5/lV7WKI6yv6m" +  //$NON-NLS-1$
            "xCksSGDyTU13Mw5JV2qeaWd54FWn0BRAMlLgPZl/NAuBKMz+dWdF0H2hwCAPwcZIjfOFLHAAAAAElFTkSuQmCC\")"; //$NON-NLS-1$
    
    private static final String DESCENDING_IMAGE = "url(\"data:image/png;base64," +                      //$NON-NLS-1$
            "iVBORw0KGgoAAAANSUhEUgAAAAsAAAAHCAYAAADebrddAAAAiklEQVR42mPIrewMya3oup5X2XkeiC/nVXRezgV" +  //$NON-NLS-1$
            "iEDu3vPMskH0BROeVdqkyJNTXcwAlDgDxfwxcAaWrOpsYYCC/qlUcKPgMLlnZBcWd/4E272BAB0DdjkDJf2AFFR" +  //$NON-NLS-1$
            "BTgfTj4uIeEQZsAKigHmE6EJd32DDgA0DF20FOyK/sqmIgBEDWAhVPwyYHAJAqZIiNwsHKAAAAAElFTkSuQmCC\")"; //$NON-NLS-1$
    
    private static final String TABLE_TAG_NAME = "table"; //$NON-NLS-1$
    
    private static final String CELL_TABLE_CSS_CLASS = "GCKY0V4BDK"; //$NON-NLS-1$
    
    private static final String SELECTED_ROW_CSS_CLASS = "GCKY0V4BNJ"; //$NON-NLS-1$
    
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
    
    public void sortByColumn(int column, SortingOrder order) {
        List<WebElement> headers = this.context.findElements(By.xpath(".//thead/tr/th"));
        WebElement header = headers.get(column);
        String role = header.getAttribute("role");
        
        if(role.equals("button")) {
            if(order == SortingOrder.None && getSortingOrder(column) != SortingOrder.None)
                throw new IllegalArgumentException("Can't change sorting order back to NONE");
            
            while(order != getSortingOrder(column)) {
                header.click();
            }
        }
    }
    
    public SortingOrder getSortingOrder(int column) {
        List<WebElement> headers = this.context.findElements(By.xpath(".//thead/tr/th"));
        WebElement header = headers.get(column);
        
        List<WebElement> images = header.findElements(By.xpath(".//div/div/img"));
        
        if(images.size() == 0)
            return SortingOrder.None;
        
        String image = images.get(0).getCssValue("background-image");
        
        if(ASCENDING_IMAGE.equals(image))
            return SortingOrder.Ascending;
        
        if(DESCENDING_IMAGE.equals(image))
            return SortingOrder.Descending;
        
        throw new RuntimeException("Unkown sorting indicator");
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
    
    public void selectRow(WebElement row) {
        Actions actions = new Actions(this.driver);
        
        actions.moveToElement(row.findElement(By.tagName("td")), 1, 1);
        actions.click();
        
        actions.perform();
    }
    
    public void selectRows(List<WebElement> rows) {
        Actions actions = new Actions(this.driver);
        actions.keyDown(Keys.CONTROL);
        
        // First deselect all selected rows
        for(WebElement row : getSelectedRows()) {
            actions.moveToElement(row.findElement(By.tagName("td")), 1, 1);
            actions.click();
        }
        
        // Select all specified rows
        for(WebElement row : rows) {
            actions.moveToElement(row.findElement(By.tagName("td")), 1, 1);
            actions.click();
        }
        
        actions.keyUp(Keys.CONTROL);
        
        actions.perform();
    }
    
    public List<WebElement> getSelectedRows() {
        List<WebElement> rows = getRows();
        Iterator<WebElement> iterator = rows.iterator();
        
        while(iterator.hasNext()) {
            WebElement row = iterator.next();
            String cssClasses = row.getAttribute("class");
            
            if(cssClasses == null || !cssClasses.contains(SELECTED_ROW_CSS_CLASS))
                iterator.remove();
        }
        
        return rows;
    }
    
    @Override
    protected void verify() {
        WebElement table = (WebElement) this.context;
        String tagName = table.getTagName();
        String cssClass = table.getAttribute("class");
        
        if(!TABLE_TAG_NAME.equalsIgnoreCase(tagName) || !CELL_TABLE_CSS_CLASS.equals(cssClass))
            throw new IllegalArgumentException("WebElement does not represent a CellTable");
    }
    
    private List<WebElement> findHeaders() {
        return this.context.findElements(By.xpath(".//thead/tr//*[@__gwt_header]"));
    }
    
    private boolean isRowForLoadingAnimation(WebElement row) {
        List<WebElement> images = row.findElements(By.xpath(".//td/div/div/div/img"));
        
        if(images.isEmpty() || images.size() > 1)
            return false;
                
        return true;
    }
}

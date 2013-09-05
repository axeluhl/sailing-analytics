package com.sap.sailing.selenium.pages.adminconsole.regatta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;

import com.sap.sailing.selenium.pages.PageArea;

import com.sap.sailing.selenium.pages.adminconsole.Actions;

import com.sap.sailing.selenium.pages.gwt.CellTable;
import com.sap.sailing.selenium.pages.gwt.CellTable.SortingOrder;

public class RegattaList extends PageArea {
    public static class RegattaDescriptor {
        private final String name;
        private final String boatClass;
        
        // TODO [D049941]: Mark as a bug and remove this method as soon as possible
        public static RegattaDescriptor fromString(String name) {
            if(name == null || name.isEmpty())
                return null;
            
            int openingBrace = name.lastIndexOf('(');
            int closingBrace = name.lastIndexOf(')');
            
            return new RegattaDescriptor(name.substring(0, openingBrace - 1),
                    name.substring(openingBrace + 1, closingBrace));
        }
        
        public RegattaDescriptor(String name, String boatClass) {
            this.name = name;
            this.boatClass = boatClass;
        }
        
        public String getName() {
            return this.name;
        }
        
        public String getBoatClass() {
            return this.boatClass;
        }
        
        public String toString() {
            return this.name + " (" + this.boatClass + ")";
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(this.name, this.boatClass);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object)
                return true;
            
            if (object == null)
                return false;
            
            if (getClass() != object.getClass())
                return false;
            
            RegattaDescriptor other = (RegattaDescriptor) object;
            
            if (!Objects.equals(this.boatClass, other.boatClass))
                return false;
            
            if (!Objects.equals(this.name, other.name))
                return false;
            
            return true;
        }
    }
    
    @FindBy(how = BySeleniumId.class, using = "FilterRegattasTextField")
    private WebElement filterRegattasTextField;
    
    @FindBy(how = BySeleniumId.class, using = "NoRegattasLabel")
    private WebElement noRegattasLabel;
    
    @FindBy(how = BySeleniumId.class, using = "RegattasTable")
    private WebElement regattasTable;
    
    public RegattaList(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void sortByRegattaName(SortingOrder order) {
        sortRegattaList(0, order);
    }
    
    public void sortByBoatClass(SortingOrder order) {
        sortRegattaList(1, order);
    }
    
    public List<RegattaDescriptor> getRegattas() {
        List<RegattaDescriptor> descriptors = new LinkedList<>();
        CellTable table = new CellTable(this.driver, this.regattasTable);
        
        for(WebElement row : table.getRows()) {
            List<WebElement> columns = row.findElements(By.tagName("td"));
            String name = columns.get(0).getText();
            String boatClass = columns.get(1).getText();
            
            descriptors.add(new RegattaDescriptor(name, boatClass));
        }
        
        return descriptors;
    }
    
    public void selectRegatta(RegattaDescriptor regatta) {
        selectRegattas(Arrays.asList(regatta));
    }
    
    public void selectRegattas(List<RegattaDescriptor> regattas) {
        CellTable table = new CellTable(this.driver, this.regattasTable);
        table.selectRows(findRegattas(regattas));
    }

//    public EditRegattaDialog editRegatta(RegattaDescriptor regatta) {
//        
//    }

    public void removeRegatta(RegattaDescriptor regatta) {
        WebElement row = findRegatta(regatta);
        
        if(row != null) {
            WebElement action = Actions.findRemoveAction(row);
            action.click();
            
            waitForAjaxRequests();
            
            Actions.acceptAlert(this.driver);
        }
    }
    
    private WebElement findRegatta(RegattaDescriptor regatta) {
        CellTable table = new CellTable(this.driver, this.regattasTable);
        
        for(WebElement row : table.getRows()) {
            List<WebElement> columns = row.findElements(By.tagName("td"));
            String name = columns.get(0).getText();
//            String boatClass = columns.get(1).getText();
//            
//            if(regatta.equals(new RegattaDescriptor(name, boatClass)))
//                return row;
            if(regatta.equals(RegattaDescriptor.fromString(name)))
                return row;
        }
        
        return null;
    }
    
    private List<WebElement> findRegattas(List<RegattaDescriptor> regattas) {
        List<WebElement> result = new ArrayList<>();
        
        CellTable table = new CellTable(this.driver, this.regattasTable);
        
        for(WebElement row : table.getRows()) {
            List<WebElement> columns = row.findElements(By.tagName("td"));
            String name = columns.get(0).getText();
//            String boatClass = columns.get(1).getText();
//            
//            if(regattas.contains(new RegattaDescriptor(name, boatClass)))
//                result.add(row);
            if(regattas.contains(RegattaDescriptor.fromString(name)))
                result.add(row);
        }
        
        return result;
    }
    
    private void sortRegattaList(int column, SortingOrder order) {
        CellTable table = new CellTable(this.driver, this.regattasTable);
        table.sortByColumn(column, order);
    }
}

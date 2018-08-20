package com.sap.sailing.selenium.pages.leaderboard;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.pages.common.CSSHelper;
import com.sap.sailing.selenium.pages.gwt.CellTablePO;
import com.sap.sailing.selenium.pages.gwt.DataEntryPO;
import com.sap.sailing.selenium.pages.leaderboard.LeaderboardTablePO.LeaderboardEntry;

public class LeaderboardTablePO extends CellTablePO<LeaderboardEntry> {
    public static class LeaderboardEntry extends DataEntryPO {
        private static final String TOTAL_RANK_COLUMN = "Regatta Rank";
        private static final String COMPETITOR_COLUMN = "Competitor";
        private static final String NAME_COLUMN = "Name";
        
        private static final String TOTAL_POINTS_COLUMN = "Σ";
        
        protected LeaderboardEntry(LeaderboardTablePO table, WebElement element) {
            super(table, element);
        }
        
        @Override
        public String getIdentifier() {
            return getCompetitor();
        }
        
        public Integer getTotalRank() {
            return Integer.valueOf(getColumnContent(TOTAL_RANK_COLUMN));
        }
        
        public String getCompetitor() {
            return getColumnContent(COMPETITOR_COLUMN);
        }
        
        public String getName() {
            return getColumnContent(NAME_COLUMN);
        }
        
        // Overall details
        // ...
        
        // Race details
        // ...
        
        public Integer getPointsForRace(String race) {
            String points = getColumnContent(race);
            
            if(points == null || points.isEmpty()) {
                return null;
            }
            
            try {
                return Integer.valueOf(points);
            } catch(NumberFormatException exception) {
                return Integer.valueOf(-1);
            }
        }
        
        // Leg details
        // ...
        
        public Integer getTotalPoints() {
            return Integer.valueOf(getColumnContent(TOTAL_POINTS_COLUMN));
        }
    }
    
    // Style classes to find columns in different levels (children of expanded columns)
    // We don't use the style classes for the second and third level at the moment, but they may be needed if more
    // functionality is added to the page object soon.
    //private static final String FIRST_LEVEL_COLUMN_STYLE_CLASS = ".GCKY0V4BLK";  //$NON-NLS-1$
    //private static final String SECOND_LEVEL_COLUMN_STYLE_CLASS = ".GCKY0V4BFL"; //$NON-NLS-1$
    //private static final String THIRD_LEVEL_COLUMN_STYLE_CLASS = ".GCKY0V4BHL";  //$NON-NLS-1$
    
    //private static final String RACE_COLUMN_STYLE_CLASS = ".GCKY0V4BML";  //$NON-NLS-1$
    
    private static final String EXPANDABLE_COLUMN_STYLE_CLASS = "openColumn"; //$NON-NLS-1$
    
    private static final String RACE_NAME_STYLE_CLASS = "race-name"; //$NON-NLS-1$
    
    private static final String EXPANDED_IMAGE = "data:image/png;base64," +                              //$NON-NLS-1$
            "iVBORw0KGgoAAAANSUhEUgAAABQAAAATCAYAAACQjC21AAAAj0lEQVR42u2SPQrEIBgFc2JjJQF/Cm0SzxDRE6a" +  //$NON-NLS-1$
            "xEQvf8lksW2QlC9slwlS+mUYnpRT+yfQE7xaUUoJzDsbYJeZ57s5pkC6MMdj3HSklxBiH0CaE0J3P6Du4LAuccy" +  //$NON-NLS-1$
            "il4OqhLTnkngattTiOA6011FqH0Cbn/D1IaK2xriu895fYtq07w1cWQvzE87HvGHwB11fK7fckleMAAAAASUVOR" +  //$NON-NLS-1$
            "K5CYII=";
    
    private static final String COLLAPSED_IMAGE = "data:image/png;base64," +                             //$NON-NLS-1$
            "iVBORw0KGgoAAAANSUhEUgAAABQAAAATCAYAAACQjC21AAABMUlEQVR42tWUzYqDQAzH+6x6U7Dag60f9bPQ6kk" +  //$NON-NLS-1$
            "PraVV1IMP0IOv5cWLeGiWBBzs1rLFXRYa+MMwSX4zk4RZmKYJf6nFZwO32y1omgaSJJFwjXuzgbquw263gyzLSJ" +  //$NON-NLS-1$
            "7nEXQ2UFEUAg2WJAnIsjwfuFqt4Hq9MuDpdKJD3gYahkE3GGrG8zxEUcSAYRgCx3HMv1wuKWcSiA7LsiBNUyiKA" +  //$NON-NLS-1$
            "vI8h/P5DHVdM+DtdqNnow9jsByYM4Yy4GazAd/3oes6GNv9foe+70m4HhvGYg7mTgKxi03TPEBeCWPatn0NHObu" +  //$NON-NLS-1$
            "cDhQrVCu60JVVexGZVmC4zjMHwTB01w+dVlVVSZRFKmzg8VxDIIgPMT879hMAS+XCwMej8ffAXEcbNuG/X5P+j4" +  //$NON-NLS-1$
            "is34bBKzXa9JPsM/4D78ABqXmiURzXL4AAAAASUVORK5CYII=";
    
    // QUESTION: Do we need this predicate in the RACE_COLUMN_XPATH and in the RACE_NAMES_XPATH?
    //private static final String RACE_COLUMN_PREDICATE = CSSHelper.containsCSSClassesPredicate(FIRST_LEVEL_COLUMN_STYLE_CLASS,
    //        RACE_COLUMN_STYLE_CLASS);
    
    private static final String RACE_COLUMN_XPATH = "./thead/tr/th[//*[@__gwt_header] and .//span[" +
            CSSHelper.containsCSSClassPredicate(RACE_NAME_STYLE_CLASS) + " and text() = '%s']]";
    private static final String RACE_NAMES_XPATH = "./thead/tr//*[@__gwt_header]//span[" +
            CSSHelper.containsCSSClassPredicate(RACE_NAME_STYLE_CLASS) + "]";
    
    private static final String EXPANSION_BUTTON_XPATH = "./span/div[" +
            CSSHelper.containsCSSClassPredicate(EXPANDABLE_COLUMN_STYLE_CLASS) + "]/img";
    
    public LeaderboardTablePO(WebDriver driver, WebElement element) {
        super(driver, element);
        
    }
    
    public boolean isColumnExpandable(int column) {
        List<WebElement> headers = this.context.findElements(By.xpath(TABLE_HEADER_XPATH));
        WebElement header = headers.get(column);
        
        return !header.findElements(By.xpath(EXPANSION_BUTTON_XPATH)).isEmpty();
    }
    
    public boolean isColumnExpanded(int column) {
        List<WebElement> headers = this.context.findElements(By.xpath(TABLE_HEADER_XPATH));
        WebElement header = headers.get(column);
        List<WebElement> elements = header.findElements(By.xpath(EXPANSION_BUTTON_XPATH));
        
        if(elements.isEmpty() || COLLAPSED_IMAGE.equals(elements.get(0).getAttribute("src")))
            return false;
        
        return true;
    }
    
    public void expandColumn(int column) {
        List<WebElement> headers = this.context.findElements(By.xpath(TABLE_HEADER_XPATH));
        setExpansionState(headers.get(column), true);
    }
    
    public void collapseColumn(int column) {
        List<WebElement> headers = this.context.findElements(By.xpath(TABLE_HEADER_XPATH));
        setExpansionState(headers.get(column), false);
    }
    
    public List<String> getRaceNames() {
        List<String> raceNames = new ArrayList<>();
        
        List<WebElement> elements = this.context.findElements(By.xpath(RACE_NAMES_XPATH));
        
        for(WebElement element : elements) {
            raceNames.add(element.getText());
        }
        
        return raceNames;
    }
    
    public void expandRace(String name) {
        setExpansionState(getRaceColumn(name), true);
    }
    
    public void collapseRace(String name) {
        setExpansionState(getRaceColumn(name), false);
    }
    
    @Override
    protected LeaderboardEntry createDataEntry(WebElement element) {
        return new LeaderboardEntry(this, element);
    }
    
    private WebElement getRaceColumn(String race) {
        return this.context.findElement(By.xpath(String.format(RACE_COLUMN_XPATH, race)));
    }
    
    private void setExpansionState(WebElement column, boolean expanded) {
        try {
            WebElement button = getExpansionButton(column);
            String image = button.getAttribute("src");
            
            boolean toggleSate = (expanded ? COLLAPSED_IMAGE.equals(image) : EXPANDED_IMAGE.equals(image));
            
            if(toggleSate) {
                button.click();
                
                waitForAjaxRequests();
            }
        } catch(NoSuchElementException excpetion) {
            // The column is not expandable. For a race this happens if its not linked to a tracked race yet.
        }
    }
    
    private WebElement getExpansionButton(WebElement header) {
        return header.findElement(By.xpath(EXPANSION_BUTTON_XPATH));
    }
}

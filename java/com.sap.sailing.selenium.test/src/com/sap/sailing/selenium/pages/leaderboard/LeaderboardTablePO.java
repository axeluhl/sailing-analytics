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
    
    // QUESTION: Do we need this predicate in the RACE_COLUMN_XPATH and in the RACE_NAMES_XPATH?
    //private static final String RACE_COLUMN_PREDICATE = CSSHelper.containsCSSClassesPredicate(FIRST_LEVEL_COLUMN_STYLE_CLASS,
    //        RACE_COLUMN_STYLE_CLASS);
    
    private static final String RACE_COLUMN_XPATH = "./thead/tr/th[//*[@__gwt_header] and .//span[" +
            CSSHelper.containsCSSClassPredicate(RACE_NAME_STYLE_CLASS) + " and text() = '%s']]";
    private static final String RACE_NAMES_XPATH = "./thead/tr//*[@__gwt_header]//span[" +
            CSSHelper.containsCSSClassPredicate(RACE_NAME_STYLE_CLASS) + "]";
    
    private static final String EXPANSION_BUTTON_XPATH = "./span/div[" +
            CSSHelper.containsCSSClassPredicate(EXPANDABLE_COLUMN_STYLE_CLASS) + "]";
    
    private static final String EXPANSION_BUTTON_IMAGE_XPATH = EXPANSION_BUTTON_XPATH + "/img";
    
    public LeaderboardTablePO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public boolean isColumnExpandable(int column) {
        List<WebElement> headers = this.context.findElements(By.xpath(TABLE_HEADER_XPATH));
        WebElement header = headers.get(column);
        return !header.findElements(By.xpath(EXPANSION_BUTTON_IMAGE_XPATH)).isEmpty();
    }
    
    public boolean isColumnExpanded(int column) {
        List<WebElement> headers = this.context.findElements(By.xpath(TABLE_HEADER_XPATH));
        WebElement header = headers.get(column);
        List<WebElement> elements = header.findElements(By.xpath(EXPANSION_BUTTON_XPATH));
        return !elements.isEmpty() && isColumnExpanded(elements.get(0));
    }

    private boolean isColumnExpanded(WebElement expansionButtonDiv) {
        // See ExpandCollapseButtonCell for where this debug attribute is set
        // on the collapse/expand button's enclosing DIV element
        return expansionButtonDiv.getAttribute("isExpanded").equalsIgnoreCase("true");
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
            WebElement expansionDiv = getExpansionButtonDiv(column);
            WebElement button = getExpansionButtonImage(column);
            boolean toggleSate = expanded != isColumnExpanded(expansionDiv);
            if (toggleSate) {
                button.click();
                waitForAjaxRequests();
            }
        } catch (NoSuchElementException excpetion) {
            // The column is not expandable. For a race this happens if its not linked to a tracked race yet.
        }
    }
    
    private WebElement getExpansionButtonImage(WebElement header) {
        return header.findElement(By.xpath(EXPANSION_BUTTON_IMAGE_XPATH));
    }

    private WebElement getExpansionButtonDiv(WebElement header) {
        return header.findElement(By.xpath(EXPANSION_BUTTON_XPATH));
    }
}

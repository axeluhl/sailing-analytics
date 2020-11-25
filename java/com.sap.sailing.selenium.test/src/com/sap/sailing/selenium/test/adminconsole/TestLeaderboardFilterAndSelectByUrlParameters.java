package com.sap.sailing.selenium.test.adminconsole;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.FlexibleLeaderboardCreateDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardConfigurationPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardConfigurationPanelPO.LeaderboardEntryPO;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

/**
 * <p>Tests for creation of leader boards.</p>
 * 
 * @author
 *   D049941
 */
public class TestLeaderboardFilterAndSelectByUrlParameters extends AbstractSeleniumTest {
    
    private static final String PARAM_FILTER = "filter";
    private static final String PARAM_SELECT = "select";
    private static final String PARAM_SELECT_EXACT = "selectExact";
    private static final String PARAM_FILTER_AND_SELECT = "filterAndSelect";
    
    @Override
    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
        createTestLeaderboards();
    }
    
    @Test
    public void testUrlParameterFilter() {        
        List<String> availableLeaderboards = getLeaderboardsOfPageWithUrlParams(PARAM_FILTER, "505");
        assertEquals(4, availableLeaderboards.size()); 
        
        availableLeaderboards = getLeaderboardsOfPageWithUrlParams(PARAM_FILTER, "2017");
        assertEquals(2, availableLeaderboards.size());
        assertEquals("505 World 2017", availableLeaderboards.get(0));
        assertEquals("505 Pre-World 2017", availableLeaderboards.get(1));
        
        availableLeaderboards = getLeaderboardsOfPageWithUrlParams(PARAM_FILTER, "");
        assertEquals(4, availableLeaderboards.size());        
    }
    
    @Test
    public void testUrlParameterSelect() {  
        LeaderboardConfigurationPanelPO leaderboardConfigurationPanelPO = getLeaderboardConfigurationPanelPoOfPageWithUrlParams(PARAM_SELECT, "505");        
        List<String> availableLeaderboards = leaderboardConfigurationPanelPO.getAvailableLeaderboards();      
        assertEquals(4, availableLeaderboards.size()); 
        List<LeaderboardEntryPO> selectedEntries = leaderboardConfigurationPanelPO.getLeaderboardTable().getSelectedEntries();
        assertEquals(4, selectedEntries.size());
        
        leaderboardConfigurationPanelPO = getLeaderboardConfigurationPanelPoOfPageWithUrlParams(PARAM_SELECT, "2017");        
        availableLeaderboards = leaderboardConfigurationPanelPO.getAvailableLeaderboards();      
        assertEquals(4, availableLeaderboards.size()); 
        selectedEntries = leaderboardConfigurationPanelPO.getLeaderboardTable().getSelectedEntries();
        assertEquals(2, selectedEntries.size());
        assertEquals("505 World 2017", selectedEntries.get(0).getName());
        assertEquals("505 Pre-World 2017", selectedEntries.get(1).getName());
        
        leaderboardConfigurationPanelPO = getLeaderboardConfigurationPanelPoOfPageWithUrlParams(PARAM_SELECT, "");        
        availableLeaderboards = leaderboardConfigurationPanelPO.getAvailableLeaderboards();      
        assertEquals(4, availableLeaderboards.size()); 
        selectedEntries = leaderboardConfigurationPanelPO.getLeaderboardTable().getSelectedEntries();
        assertEquals(0, selectedEntries.size());
    }
    
    @Test
    public void testUrlParameterFilterAndSelect() {
        LeaderboardConfigurationPanelPO leaderboardConfigurationPanelPO = getLeaderboardConfigurationPanelPoOfPageWithUrlParams(PARAM_FILTER_AND_SELECT, "505");        
        List<String> availableLeaderboards = leaderboardConfigurationPanelPO.getAvailableLeaderboards();      
        assertEquals(4, availableLeaderboards.size()); 
        List<LeaderboardEntryPO> selectedEntries = leaderboardConfigurationPanelPO.getLeaderboardTable().getSelectedEntries();
        assertEquals(4, selectedEntries.size());
        
        leaderboardConfigurationPanelPO = getLeaderboardConfigurationPanelPoOfPageWithUrlParams(PARAM_FILTER_AND_SELECT, "505 World 2017");        
        availableLeaderboards = leaderboardConfigurationPanelPO.getAvailableLeaderboards();      
        assertEquals(2, availableLeaderboards.size()); 
        selectedEntries = leaderboardConfigurationPanelPO.getLeaderboardTable().getSelectedEntries();
        assertEquals(2, selectedEntries.size());
        assertEquals("505 World 2017", selectedEntries.get(0).getName());
        assertEquals("505 Pre-World 2017", selectedEntries.get(1).getName());      
    }
    
    @Test
    public void testUrlParameterSelectExact() { 
        LeaderboardConfigurationPanelPO leaderboardConfigurationPanelPO = getLeaderboardConfigurationPanelPoOfPageWithUrlParams(PARAM_SELECT_EXACT, "505");        
        List<String> availableLeaderboards = leaderboardConfigurationPanelPO.getAvailableLeaderboards();      
        assertEquals(4, availableLeaderboards.size()); 
        List<LeaderboardEntryPO> selectedEntries = leaderboardConfigurationPanelPO.getLeaderboardTable().getSelectedEntries();
        assertEquals(0, selectedEntries.size());
        
        leaderboardConfigurationPanelPO = getLeaderboardConfigurationPanelPoOfPageWithUrlParams(PARAM_SELECT_EXACT, "505 World 2017");        
        availableLeaderboards = leaderboardConfigurationPanelPO.getAvailableLeaderboards();      
        assertEquals(4, availableLeaderboards.size()); 
        selectedEntries = leaderboardConfigurationPanelPO.getLeaderboardTable().getSelectedEntries();
        assertEquals(1, selectedEntries.size());
        assertEquals("505 World 2017", selectedEntries.get(0).getName());
        
        leaderboardConfigurationPanelPO = getLeaderboardConfigurationPanelPoOfPageWithUrlParams(PARAM_SELECT_EXACT, "");        
        availableLeaderboards = leaderboardConfigurationPanelPO.getAvailableLeaderboards();      
        assertEquals(4, availableLeaderboards.size()); 
        selectedEntries = leaderboardConfigurationPanelPO.getLeaderboardTable().getSelectedEntries();
        assertEquals(0, selectedEntries.size());
    }
    
    @Test
    public void testUrlParameterSelectExactAndSelect() {
        
        LeaderboardConfigurationPanelPO leaderboardConfigurationPanelPO = getLeaderboardConfigurationPanelPoOfPageWithUrlParams(PARAM_SELECT_EXACT, "505 World 2017", PARAM_SELECT, "2017");        
        List<String> availableLeaderboards = leaderboardConfigurationPanelPO.getAvailableLeaderboards();
        assertEquals(4, availableLeaderboards.size());
        List<LeaderboardEntryPO> selectedEntries = leaderboardConfigurationPanelPO.getLeaderboardTable().getSelectedEntries();
        assertEquals(1, selectedEntries.size());
        assertEquals("505 World 2017", selectedEntries.get(0).getName());     
        
        leaderboardConfigurationPanelPO = getLeaderboardConfigurationPanelPoOfPageWithUrlParams(PARAM_SELECT_EXACT, "505", PARAM_SELECT, "2017");        
        availableLeaderboards = leaderboardConfigurationPanelPO.getAvailableLeaderboards();      
        assertEquals(4, availableLeaderboards.size()); 
        selectedEntries = leaderboardConfigurationPanelPO.getLeaderboardTable().getSelectedEntries();
        assertEquals(0, selectedEntries.size());
    }
    
    @Test
    public void testUrlParameterSelectExactAndFilterAndSelect() {
        LeaderboardConfigurationPanelPO leaderboardConfigurationPanelPO = getLeaderboardConfigurationPanelPoOfPageWithUrlParams(PARAM_SELECT_EXACT, "505", PARAM_FILTER_AND_SELECT, "2017");        
        List<String> availableLeaderboards = leaderboardConfigurationPanelPO.getAvailableLeaderboards();      
        assertEquals(2, availableLeaderboards.size()); 
        List<LeaderboardEntryPO> selectedEntries = leaderboardConfigurationPanelPO.getLeaderboardTable().getSelectedEntries();
        assertEquals(0, selectedEntries.size());
        
        leaderboardConfigurationPanelPO = getLeaderboardConfigurationPanelPoOfPageWithUrlParams(PARAM_SELECT_EXACT, "505 World 2017", PARAM_FILTER_AND_SELECT, "2017");        
        availableLeaderboards = leaderboardConfigurationPanelPO.getAvailableLeaderboards();
        assertEquals(2, availableLeaderboards.size());
        selectedEntries = leaderboardConfigurationPanelPO.getLeaderboardTable().getSelectedEntries();
        assertEquals(1, selectedEntries.size());
        assertEquals("505 World 2017", selectedEntries.get(0).getName());     
    }
    
    private void createTestLeaderboards() {
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        LeaderboardConfigurationPanelPO leaderboardConfiguration = adminConsole.goToLeaderboardConfiguration();
        
        createFlexibleLeaderboard(leaderboardConfiguration, "505 World 2017");
        createFlexibleLeaderboard(leaderboardConfiguration, "505 Pre-World 2017");
        createFlexibleLeaderboard(leaderboardConfiguration, "505 World 2015");
        createFlexibleLeaderboard(leaderboardConfiguration, "505 World 2011");
    }
    
    private void createFlexibleLeaderboard(LeaderboardConfigurationPanelPO leaderboardConfiguration, String name) {
        FlexibleLeaderboardCreateDialogPO dialog = leaderboardConfiguration.startCreatingFlexibleLeaderboard();
        dialog.setName(name);
        assertTrue(dialog.isOkButtonEnabled());
        dialog.pressOk();  
    }
    
    private List<String> getLeaderboardsOfPageWithUrlParams(String ...keysValues) {
        LeaderboardConfigurationPanelPO leaderboardConfiguration = getLeaderboardConfigurationPanelPoOfPageWithUrlParams(keysValues);
        return leaderboardConfiguration.getAvailableLeaderboards();
    }
    
    private LeaderboardConfigurationPanelPO getLeaderboardConfigurationPanelPoOfPageWithUrlParams(String ...keysValues) {
        AdminConsolePage adminConsole = AdminConsolePage.goToLeaderboardConfigurationPlace(getWebDriver(), getContextRoot(), keysValues);
        return adminConsole.getLeaderboardConfigurationPanelPO();
    }
}

package com.sap.sailing.gwt.ui.test;


import java.util.Date;

import junit.framework.TestCase;

import org.junit.Test;

import com.sap.sailing.gwt.ui.server.SailingServiceImpl;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;

public class TestColumnSwapping extends TestCase {
	
	private LeaderboardDAO lb = null;
	private SailingServiceImpl sailingService = null;
	
	//Test-Data
	private final String TEST_LEADERBOARD_NAME = "test_board";
	private final String[] races = {"1","2","3"};
	private final boolean[] isMedalRace = {false,false,true};
	
    @Test
    public void testColumnSwapping() {
    	testLeaderBoardDAOMethods();
        testSailingService();
    }
    
    @Test
    public void testSailingService(){
    	sailingService = new SailingServiceImpl();
        assertNotNull("Sailingservice != NULL", sailingService);
        int td[] = {5,8};
        sailingService.createLeaderboard(TEST_LEADERBOARD_NAME, td);
        for (int i = 0 ; i < races.length; i++)
        	sailingService.addColumnToLeaderboard(races[i], TEST_LEADERBOARD_NAME, isMedalRace[i]);
        sailingService.moveLeaderboardColumnDown(TEST_LEADERBOARD_NAME, races[0]);
        sailingService.moveLeaderboardColumnDown(TEST_LEADERBOARD_NAME, races[0]);
        sailingService.moveLeaderboardColumnUp(TEST_LEADERBOARD_NAME, races[2]);
        sailingService.updateIsMedalRace(TEST_LEADERBOARD_NAME, races[0], true);
        sailingService.updateIsMedalRace(TEST_LEADERBOARD_NAME, races[2], false);
        try {
			lb = sailingService.getLeaderboardByName(TEST_LEADERBOARD_NAME, new Date(), null);
		} catch (Exception e) {
			// e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
        assertNotNull("LeaderboardDAO != NULL", lb);
        
        // asserted data
        String[] raceNames = new String[]{"3","2","1"};
        boolean[] medalRace = {true,false,false};
        
        for (int i = 0; i < lb.getRaceList().size(); i++){
    		assertTrue("Race[" + i + "] == " + raceNames[i],lb.getRaceList().get(i).equals(raceNames[i]));
    		assertTrue("Race[" + i + "] is " + (medalRace[i] ? "" : "no ") + "medalrace.",lb.raceIsMedalRace(lb.getRaceList().get(i)) == medalRace[i] );
        }
    }
    
    @Test
    public void testLeaderBoardDAOMethods(){
    	lb = new LeaderboardDAO();
        assertNotNull("Leaderboard != NULL", lb);
        lb.addRace("1", false, false);
    	lb.addRace("2", false, false);
    	lb.addRace("3", true, false);
    	lb.moveRaceDown("1");
    	String[] s = new String[]{"2","1","3"};
    	for (int i = 0; i < lb.getRaceList().size(); i++)
    		assertTrue("Race[" + i + "] = " + s[i],lb.getRaceList().get(i).equals(s[i]));
    }
}

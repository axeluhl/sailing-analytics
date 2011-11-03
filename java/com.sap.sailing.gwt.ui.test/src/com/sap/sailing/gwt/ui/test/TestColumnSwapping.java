package com.sap.sailing.gwt.ui.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.gwt.ui.server.SailingServiceImpl;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;



public class TestColumnSwapping {
	private static final String LEADERBOARDNAME = "test"; 
	private SailingServiceImpl service;
	private LeaderboardDAO leaderboardOriginalDAO;
	private LeaderboardDAO leaderboardDAO;
	private Collection<String> leglist;
	private Date leaderboardCreationDate;
	
	@Before
	public void prepareColumnSwapping(){
		service = new SailingServiceImpl();
		int[] disc = {5,8,9,0,7,5,43};
    	service.createLeaderboard(LEADERBOARDNAME, disc);
    	service.addColumnToLeaderboard("Rank", LEADERBOARDNAME, false);
    	service.addColumnToLeaderboard("Competitor", LEADERBOARDNAME, false);
    	service.addColumnToLeaderboard("Race1", LEADERBOARDNAME, true);
    	service.addColumnToLeaderboard("Race2", LEADERBOARDNAME, true);
    	service.addColumnToLeaderboard("Race3", LEADERBOARDNAME, true);
    	leglist = new ArrayList<String>();
    	leglist.add("Race1");
    	leglist.add("Race2");
    	leglist.add("Race3");
    	leaderboardCreationDate = new Date();
    	try {
    		// get Leaderboard with name and current date
			leaderboardOriginalDAO = new LeaderboardDAO();
			leaderboardOriginalDAO.addRace("Race1", true, false);
			leaderboardOriginalDAO.addRace("Race3", true, false);
			leaderboardOriginalDAO.addRace("Race2", true, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    @Test
    public void testColumnSwapping() {
    	service.moveLeaderboardColumnDown(LEADERBOARDNAME, "Race3");
    	try {
    		leaderboardDAO = service.getLeaderboardByName(LEADERBOARDNAME, leaderboardCreationDate, leglist);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	// check if leaderboardDAO an dleaderboardOriginalDAO same
    	List<String> leaderboardList = leaderboardDAO.getRaceList();;
    	List<String> leaderboardOriginalList = leaderboardOriginalDAO.getRaceList();
    	
    	// ??????? assert races in list
    	assertArrayEquals(leaderboardList.toArray(), leaderboardOriginalList.toArray());
    	
    	System.out.println(leaderboardList.toArray().toString());
    	System.out.println(leaderboardOriginalList.toArray().toString());
    	
    	
    	for (String string : leaderboardOriginalList) {
			assert leaderboardDAO.raceIsMedalRace(string) == leaderboardOriginalDAO.raceIsMedalRace(string);
			// assert leaderboardDAO.raceIsTracked(string) == leaderboardOriginalDAO.raceIsTracked(string);
		}
    }
}

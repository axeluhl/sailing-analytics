package com.sap.sailing.gwt.ui.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import junit.framework.TestCase;

import com.sap.sailing.gwt.ui.server.SailingServiceImpl;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;

public class TestColumnSwapping extends TestCase {

    private LeaderboardDAO lb = null;
    private static final String LEADERBOARDNAME = "test";
    private SailingServiceImpl service;
    private LeaderboardDAO leaderboardOriginalDAO;
    private LeaderboardDAO leaderboardDAO;
    private Collection<String> leglist;
    private Date leaderboardCreationDate;
    private SailingServiceImpl sailingService = null;

    // Test-Data
    private final String TEST_LEADERBOARD_NAME = "test_board";
    private final String[] races = { "1", "2", "3" };
    private final boolean[] isMedalRace = { false, false, true };

    @Before
    public void prepareColumnSwapping() {
        service = new SailingServiceImpl();
        int[] disc = { 5, 8, 9, 0, 7, 5, 43 };
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
        testColumnSwappingFabian();
        testLeaderBoardDAOMethods();
        testSailingService();
    }

    @Test
    public void testSailingService() {
        sailingService = new SailingServiceImpl();
        assertNotNull("Sailingservice != NULL", sailingService);
        int td[] = { 5, 8 };
        sailingService.createLeaderboard(TEST_LEADERBOARD_NAME, td);
        for (int i = 0; i < races.length; i++)
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
        String[] raceNames = new String[] { "3", "2", "1" };
        boolean[] medalRace = { true, false, false };

        for (int i = 0; i < lb.getRaceList().size(); i++) {
            assertTrue("Race[" + i + "] == " + raceNames[i], lb.getRaceList().get(i).equals(raceNames[i]));
            assertTrue("Race[" + i + "] is " + (medalRace[i] ? "" : "no ") + "medalrace.",
                    lb.raceIsMedalRace(lb.getRaceList().get(i)) == medalRace[i]);
        }
    }

    @Test
    public void testLeaderBoardDAOMethods() {
        lb = new LeaderboardDAO();
        assertNotNull("Leaderboard != NULL", lb);
        lb.addRace("1", false, false);
        lb.addRace("2", false, false);
        lb.addRace("3", true, false);
        lb.moveRaceDown("1");
        String[] s = new String[] { "2", "1", "3" };
        for (int i = 0; i < lb.getRaceList().size(); i++)
            assertTrue("Race[" + i + "] = " + s[i], lb.getRaceList().get(i).equals(s[i]));
    }

    @Test
    public void testColumnSwappingFabian() {
        service.moveLeaderboardColumnDown(LEADERBOARDNAME, "Race3");
        try {
            leaderboardDAO = service.getLeaderboardByName(LEADERBOARDNAME, leaderboardCreationDate, leglist);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // check if leaderboardDAO an dleaderboardOriginalDAO same
        List<String> leaderboardList = leaderboardDAO.getRaceList();
        ;
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

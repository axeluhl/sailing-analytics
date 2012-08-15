package com.sap.sailing.gwt.ui.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.gwt.ui.server.SailingServiceImpl;
import com.sap.sailing.gwt.ui.shared.FleetDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;

public class TestColumnSwapping {

    private LeaderboardDTO lb = null;
    private static final String LEADERBOARDNAME = "test";
    private static final String DEFAULT_FLEET_NAME = "Default";
    private static final FleetDTO DEFAULT_FLEET = new FleetDTO(DEFAULT_FLEET_NAME, /* ordering */ 0, /* color */ null);
    private SailingServiceImpl service;
    private LeaderboardDTO leaderboardOriginalDTO;
    private LeaderboardDTO leaderboardDTO;
    private Collection<String> leglist;
    private Date leaderboardCreationDate;
    private SailingServiceImpl sailingService = null;

    // Test-Data
    private final String TEST_LEADERBOARD_NAME = "test_board";
    private final String[] races = { "1", "2", "3" };
    private final boolean[] isMedalRace = { false, false, true };

    @Before
    public void prepareColumnSwapping() {
        service = new SailingServiceImplMock();
        int[] disc = { 5, 8, 9, 0, 7, 5, 43 };
        service.createFlexibleLeaderboard(LEADERBOARDNAME, disc, ScoringSchemeType.LOW_POINT);
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
            leaderboardOriginalDTO = new LeaderboardDTO(null, null);
            leaderboardOriginalDTO.addRace("Race1", DEFAULT_FLEET, true, null, null);
            leaderboardOriginalDTO.addRace("Race3", DEFAULT_FLEET, true, null, null);
            leaderboardOriginalDTO.addRace("Race2", DEFAULT_FLEET, true, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testColumnSwapping() {
        testColumnSwappingFabian();
        testLeaderBoardDTOMethods();
        testSailingService();
    }

    @Test
    public void testSailingService() {
        sailingService = new SailingServiceImplMock();
        assertNotNull("Sailingservice != NULL", sailingService);
        int td[] = { 5, 8 };
        sailingService.createFlexibleLeaderboard(TEST_LEADERBOARD_NAME, td, ScoringSchemeType.LOW_POINT);
        for (int i = 0; i < races.length; i++)
            sailingService.addColumnToLeaderboard(races[i], TEST_LEADERBOARD_NAME, isMedalRace[i]);
        sailingService.moveLeaderboardColumnDown(TEST_LEADERBOARD_NAME, races[0]);
        sailingService.moveLeaderboardColumnDown(TEST_LEADERBOARD_NAME, races[0]);
        sailingService.moveLeaderboardColumnUp(TEST_LEADERBOARD_NAME, races[2]);
        sailingService.updateIsMedalRace(TEST_LEADERBOARD_NAME, races[0], true);
        sailingService.updateIsMedalRace(TEST_LEADERBOARD_NAME, races[2], false);
        try {
            lb = sailingService.getLeaderboardByName(TEST_LEADERBOARD_NAME, new Date(), /* races to load */ null);
        } catch (Exception e) {
            // e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
        assertNotNull("LeaderboardDTO != NULL", lb);

        // asserted data
        String[] raceNames = new String[] { "3", "2", "1" };
        boolean[] medalRace = { true, false, false };

        for (int i = 0; i < lb.getRaceList().size(); i++) {
            assertTrue("Race[" + i + "] == " + raceNames[i], lb.getRaceList().get(i).equals(raceNames[i]));
            assertTrue("Race[" + i + "] is " + (medalRace[i] ? "" : "no ") + "medalrace.",
                    lb.raceIsMedalRace(races[i]) == medalRace[i]);
        }
    }

    @Test
    public void testLeaderBoardDTOMethods() {
        lb = new LeaderboardDTO(null, null);
        assertNotNull("Leaderboard != NULL", lb);
        lb.addRace("1", DEFAULT_FLEET, false, null, null);
        lb.addRace("2", DEFAULT_FLEET, false, null, null);
        lb.addRace("3", DEFAULT_FLEET, true, null, null);
        lb.moveRaceDown("1");
        String[] s = new String[] { "2", "1", "3" };
        for (int i = 0; i < lb.getRaceList().size(); i++)
            assertTrue("Race[" + i + "] = " + s[i], lb.getRaceList().get(i).equals(s[i]));
    }

    @Test
    public void testColumnSwappingFabian() {
        service.moveLeaderboardColumnUp(LEADERBOARDNAME, "Race3");
        try {
            leaderboardDTO = service.getLeaderboardByName(LEADERBOARDNAME, leaderboardCreationDate, leglist);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // check if leaderboardDTO an dleaderboardOriginalDTO same
        List<RaceColumnDTO> leaderboardList = leaderboardDTO.getRaceList();
        List<RaceColumnDTO> leaderboardOriginalList = leaderboardOriginalDTO.getRaceList();

        // ??????? assert races in list
        assertArrayEquals(leaderboardList.toArray(), leaderboardOriginalList.toArray());

        for (RaceColumnDTO raceDTO : leaderboardOriginalList) {
            assert leaderboardDTO.raceIsMedalRace(raceDTO.getRaceColumnName()) == leaderboardOriginalDTO.raceIsMedalRace(raceDTO.getRaceColumnName());
            assert leaderboardDTO.raceIsTracked(raceDTO.getRaceColumnName()) == leaderboardOriginalDTO.raceIsTracked(raceDTO.getRaceColumnName());
        }
    }
}

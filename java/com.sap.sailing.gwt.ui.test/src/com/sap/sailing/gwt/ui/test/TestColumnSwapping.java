package com.sap.sailing.gwt.ui.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.server.SailingServiceImpl;

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
        service.removeLeaderboard(LEADERBOARDNAME);
        service.createFlexibleLeaderboard(LEADERBOARDNAME, null, disc, ScoringSchemeType.LOW_POINT, null);
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
            leaderboardOriginalDTO = new LeaderboardDTO(new Date(), null, null, ScoringSchemeType.LOW_POINT, /* higherScoreIsBetter */ false, new LeaderboardDTO.UUIDGenerator() {
                @Override
                public String generateRandomUUID() {
                    return UUID.randomUUID().toString();
                }
            }, /* hasOverallDetails */ false, new BoatClassDTO(BoatClassMasterdata._49ER.getDisplayName(), BoatClassMasterdata._49ER.getHullLength(), BoatClassMasterdata._49ER.getHullBeam()));
            leaderboardOriginalDTO.addRace("Race1", /* explicitFactor */ null, 2., /* regattaName */ null, /* seriesName */ null, DEFAULT_FLEET, true, null, null, false);
            leaderboardOriginalDTO.addRace("Race3", /* explicitFactor */ null, 2., /* regattaName */ null, /* seriesName */ null, DEFAULT_FLEET, true, null, null, false);
            leaderboardOriginalDTO.addRace("Race2", /* explicitFactor */ null, 2., /* regattaName */ null, /* seriesName */ null, DEFAULT_FLEET, true, null, null, false);
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
        sailingService.removeLeaderboard(TEST_LEADERBOARD_NAME);
        sailingService.createFlexibleLeaderboard(TEST_LEADERBOARD_NAME, null, td,
                ScoringSchemeType.LOW_POINT, null);
        for (int i = 0; i < races.length; i++)
            sailingService.addColumnToLeaderboard(races[i], TEST_LEADERBOARD_NAME, isMedalRace[i]);
        sailingService.moveLeaderboardColumnDown(TEST_LEADERBOARD_NAME, races[0]);
        sailingService.moveLeaderboardColumnDown(TEST_LEADERBOARD_NAME, races[0]);
        sailingService.moveLeaderboardColumnUp(TEST_LEADERBOARD_NAME, races[2]);
        sailingService.updateIsMedalRace(TEST_LEADERBOARD_NAME, races[0], true);
        sailingService.updateIsMedalRace(TEST_LEADERBOARD_NAME, races[2], false);
        try {
            for (int i = 0; i < races.length; i++)
                sailingService.addColumnToLeaderboard(races[i], TEST_LEADERBOARD_NAME, isMedalRace[i]);
            sailingService.moveLeaderboardColumnDown(TEST_LEADERBOARD_NAME, races[0]);
            sailingService.moveLeaderboardColumnDown(TEST_LEADERBOARD_NAME, races[0]);
            sailingService.moveLeaderboardColumnUp(TEST_LEADERBOARD_NAME, races[2]);
            sailingService.updateIsMedalRace(TEST_LEADERBOARD_NAME, races[0], true);
            sailingService.updateIsMedalRace(TEST_LEADERBOARD_NAME, races[2], false);
            lb = sailingService.getLeaderboardByName(TEST_LEADERBOARD_NAME, new Date(), /* races to load */ null, /* addOverallDetails */ true, /* previous leaderboard ID */ null,
                    /* fillTotalPointsUncorrected */ false).getLeaderboardDTO(/* previousVersion */ null);
        } catch (Exception e) {
            // e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
        assertNotNull("LeaderboardDTO != NULL", lb);

        // asserted data
        String[] raceNames = new String[] { "3", "2", "1" };
        boolean[] medalRace = { true, false, false };

        for (int i = 0; i < lb.getRaceList().size(); i++) {
            assertEquals("Race[" + i + "] == " + raceNames[i], raceNames[i], lb.getRaceList().get(i).getName());
            assertTrue("Race[" + i + "] is " + (medalRace[i] ? "" : "no ") + "medalrace.",
                    lb.raceIsMedalRace(races[i]) == medalRace[i]);
        }
    }

    @Test
    public void testLeaderBoardDTOMethods() {
        lb = new LeaderboardDTO(new Date(), null, null, ScoringSchemeType.LOW_POINT, /* higherScoreIsBetter */ false, new LeaderboardDTO.UUIDGenerator() {
            @Override
            public String generateRandomUUID() {
                return UUID.randomUUID().toString();
            }
        }, /* hasOverallDetails */ false, new BoatClassDTO(BoatClassMasterdata._49ER.getDisplayName(), BoatClassMasterdata._49ER.getHullLength(), BoatClassMasterdata._49ER.getHullBeam()));
        assertNotNull("Leaderboard != NULL", lb);
        lb.addRace("1", /* explicitFactor */ null, 1., /* regattaName */ null, /* seriesName */ null, DEFAULT_FLEET, false, null, null, false);
        lb.addRace("2", /* explicitFactor */ null, 1., /* regattaName */ null, /* seriesName */ null, DEFAULT_FLEET, false, null, null, false);
        lb.addRace("3", /* explicitFactor */ null, 1., /* regattaName */ null, /* seriesName */ null, DEFAULT_FLEET, true, null, null, false);
        lb.moveRaceDown("1");
        String[] s = new String[] { "2", "1", "3" };
        for (int i = 0; i < lb.getRaceList().size(); i++) {
            assertEquals("Race[" + i + "] = " + s[i], s[i], lb.getRaceList().get(i).getName());
        }
    }

    @Test
    public void testColumnSwappingFabian() {
        service.moveLeaderboardColumnUp(LEADERBOARDNAME, "Race3");
        try {
            leaderboardDTO = service.getLeaderboardByName(LEADERBOARDNAME, leaderboardCreationDate, leglist, /* addOverallDetails */
                    true, /* previous leaderboard ID */null, /* fillTotalPointsUncorrected */false).getLeaderboardDTO(/* previousVersion */null);
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

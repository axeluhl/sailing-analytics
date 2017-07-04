package com.sap.sailing.gwt.ui.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.dto.IncrementalOrFullLeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.tractracadapter.TracTracConnectionConstants;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.ExpandableSortableColumn;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.TracTracRaceRecordDTO;
import com.sap.sse.common.Util;

public class GwtTestCaseColumnToggling extends GWTTestCase {
    
    //These objects should be created by calling GWT.create(Class c);
    private LeaderboardPanelMock leaderboardPanel;
    private MyTestSailingServiceAsync service;
    private StringMessages stringMessages;
    
    //Test data.
    private final String LEADERBOARD_NAME = "test";
    private static final String DEFAULT_FLEET_NAME = "Default";
    private final String COLUMN1_NAME = "r1";
    private final String EVENT_NAME = "Sailing Team Germany (STG)";
    protected static final boolean tractracTunnel = Boolean.valueOf(System.getProperty("tractrac.tunnel", "false"));
    protected static final String tractracTunnelHost = System.getProperty("tractrac.tunnel.host", "localhost");
    private final String JSON_URL= "http://" + TracTracConnectionConstants.HOST_NAME + "/events/event_20110505_SailingTea/jsonservice.php";
    private final String TRACKED_RACE = "schwerttest";
    private final String COURSE_DESIGN_UPDATE_URI = "http://tracms.traclive.dk/update_course";
    private final String TRACTRAC_USERNAME = "tracTest"; //only used for course update authentification
    private final String TRACTRAC_PASSWORD = "tracTest"; //only used for course update authentification
    
    private LeaderboardDTO leaderboard;
    private TracTracRaceRecordDTO rrDao;
    private ExpandableSortableColumn<LeaderboardRowDTO> rc;
    
    

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        service = GWT.create(MyTestSailingService.class);
        stringMessages = GWT.create(StringMessages.class);
    }

    @Test
    public void testColumnDeleting() {
        delayTestFinish(1000000);
        listRacesInEvent();
    }
    
    private void listRacesInEvent(){
        service.listTracTracRacesInEvent(JSON_URL, /* hidden */ true, new AsyncCallback<Util.Pair<String,List<TracTracRaceRecordDTO>>>() {

            @Override
            public void onFailure(Throwable caught) {
                fail("Failed to list races." + caught.getLocalizedMessage());
            }

            @Override
            public void onSuccess(Util.Pair<String, List<TracTracRaceRecordDTO>> result) {
                System.out.println("Listed races.");
                for (TracTracRaceRecordDTO rr : result.getB()){
                    if (rr.getName().equals(TRACKED_RACE)){
                        rrDao = rr;
                    }
                }
                assertNotNull(rrDao);
                trackRace();
            }
        });
    }
    
    private void trackRace(){
        final Set<TracTracRaceRecordDTO> rrDAOs = new HashSet<TracTracRaceRecordDTO>();
        rrDAOs.add(rrDao);
        service.trackWithTracTrac(/* regattaToAddTo */null, rrDAOs, tractracTunnel ? "tcp://" + tractracTunnelHost + ":"
                + TracTracConnectionConstants.PORT_TUNNEL_LIVE : "tcp://" + TracTracConnectionConstants.HOST_NAME + ":"
                + TracTracConnectionConstants.PORT_LIVE, tractracTunnel ? "tcp://" + tractracTunnelHost + ":"
                + TracTracConnectionConstants.PORT_TUNNEL_STORED : "tcp://" + TracTracConnectionConstants.HOST_NAME
                + ":" + TracTracConnectionConstants.PORT_STORED, 
                COURSE_DESIGN_UPDATE_URI, 
                false, false, /* offsetToStartTimeOfSimulatedRace */ null, /* ignoreTracTracMarkPassings */ false, TRACTRAC_USERNAME, TRACTRAC_PASSWORD, new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
                fail("Failed to track race: " + caught.getLocalizedMessage());
            }

            @Override
            public void onSuccess(Void result) {
                System.out.println("Tracked race.");
                createLeaderboard();
            }
        });
    }
    
    private void createLeaderboard(){
        service.removeLeaderboard(LEADERBOARD_NAME, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                fail("Failed to remove Leaderboard "+LEADERBOARD_NAME+": " + caught.getLocalizedMessage());
                finishTest();
            }

            @Override
            public void onSuccess(Void result) {
                service.createFlexibleLeaderboard(LEADERBOARD_NAME, null, new int[] { 1, 2 }, ScoringSchemeType.LOW_POINT, null,
                        new AsyncCallback<StrippedLeaderboardDTO>() {
                            @Override
                            public void onSuccess(StrippedLeaderboardDTO result) {
                                System.out.println("Created Leaderboard "+result.name);
                                addColumnToLeaderboard();
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                fail("Failed to create Leaderboard." + caught.getLocalizedMessage());
                            }
                        });
            }
        });
    }
    
    private void addColumnToLeaderboard() {
        leaderboardPanel = new LeaderboardPanelMock(service, LEADERBOARD_NAME, null, stringMessages);
        service.addColumnToLeaderboard(COLUMN1_NAME, LEADERBOARD_NAME, false,
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        fail("Failed to add column to leaderboard.");
                    }

                    @Override
                    public void onSuccess(Void result) {
                        System.out.println("Added column to leaderboard.");
                        RaceColumnDTO race = new RaceColumnDTO();
                        race.setName(COLUMN1_NAME);
                        race.setMedalRace(false);
                        leaderboardPanel.addColumn(leaderboardPanel.createRaceColumn(race));
                        linkTrackedRace();
                    }
                });
    }
    
    private void linkTrackedRace() {
        service.connectTrackedRaceToLeaderboardColumn(LEADERBOARD_NAME, COLUMN1_NAME, DEFAULT_FLEET_NAME,
                new RegattaNameAndRaceName(EVENT_NAME, TRACKED_RACE),
                new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
                fail("Failed to link race.");
            }

            @Override
            public void onSuccess(Boolean result) {
                System.out.println("Success of linking race to column: "+result);
                getLeaderboardByNameAndDeleteColumn();
            }
        });
    }
    
    private void getLeaderboardByNameAndDeleteColumn(){
        ArrayList<String> al = new ArrayList<String>();
        al.add(COLUMN1_NAME);
        service.getLeaderboardByName(LEADERBOARD_NAME, new Date(), al, /* addOverallDetails */ false, /* previousLeaderboardId */ null,
                /* fillUncorrectedTotalPoints */ false, new AsyncCallback<IncrementalOrFullLeaderboardDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                fail("Failed to get leaderboard.");
                finishTest();
            }

            @Override
            public void onSuccess(IncrementalOrFullLeaderboardDTO result) {
                System.out.println("Got leaderboard.");
                leaderboard = result.getLeaderboardDTO(/* previousVersion */ null);
                leaderboardPanel.updateLeaderboard(leaderboard);
                for (int i = 0; i < leaderboardPanel.getLeaderboardTable()
                        .getColumnCount(); i++) {
                    Column<LeaderboardRowDTO, ?> c = leaderboardPanel.getLeaderboardTable().getColumn(i);
                    if (c instanceof ExpandableSortableColumn<?>) {
                        @SuppressWarnings("unchecked")
                        ExpandableSortableColumn<LeaderboardRowDTO> myRc = (ExpandableSortableColumn<LeaderboardRowDTO>) c;
                        rc = myRc;
                        rc.setEnableExpansion(true);
                    }
                }
                removeColumnAndAssert();
            }
        });
    }
    
    private void removeColumnAndAssert() {
        service.removeLeaderboardColumn(LEADERBOARD_NAME, COLUMN1_NAME,
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        fail("Failed to remoce column.");
                        finishTest();
                    }

                    @Override
                    public void onSuccess(Void result) {
                        leaderboardPanel.updateLeaderboard(leaderboard);
                        assertNotNull(rc);
                        try {
                            rc.changeExpansionState(/* expand */ !rc.isExpanded());
                            leaderboardPanel.updateLeaderboard(leaderboard);
                        } catch (Exception e) {
                            fail("Toggle column failed." + e.getLocalizedMessage());
                        }
                        finishTest();
                    }
                });
    }
    
    

    @Override
    protected void gwtTearDown() throws Exception {
        // TODO Auto-generated method stub
        super.gwtTearDown();
    }

    @Override
    public String getModuleName() {
        // TODO Auto-generated method stub
        return "com.sap.sailing.gwt.ui.test.TestConsole";
    }

}

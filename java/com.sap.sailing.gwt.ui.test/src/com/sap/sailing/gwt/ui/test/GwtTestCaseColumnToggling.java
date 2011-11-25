package com.sap.sailing.gwt.ui.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.client.ExpandableSortableColumn;
import com.sap.sailing.gwt.ui.client.StringConstants;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDAO;
import com.sap.sailing.gwt.ui.shared.Pair;
import com.sap.sailing.gwt.ui.shared.TracTracRaceRecordDAO;

public class GwtTestCaseColumnToggling extends GWTTestCase {
    
    //These objects should be created by calling GWT.create(Class c);
    private LeaderboardPanelMock leaderboardPanel;
    private TestSailingServiceAsync service;
    private StringConstants sc;
    
    //Test data.
    private final String LEADERBOARD_NAME = "test";
    private final String COLUMN1_NAME = "r1";
    private final String EVENT_NAME = "Sailing Team Germany (STG)";
    private final String JSON_URL= "http://germanmaster.traclive.dk/events/event_20110505_SailingTea/jsonservice.php";
    private final String TRACKED_RACE = "schwerttest";
    
    
    private LeaderboardDAO leaderboard;
    private TracTracRaceRecordDAO rrDao;
    private ExpandableSortableColumn<LeaderboardRowDAO> rc;
    
    

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        service = GWT.create(TestSailingService.class);
        sc = GWT.create(StringConstants.class);
    }

    public void testColumnDeleting() {
        delayTestFinish(1000000);
        
        listRacesInEvent();
        
    }
    
    private void listRacesInEvent(){
        service.listTracTracRacesInEvent(JSON_URL, new AsyncCallback<Pair<String,List<TracTracRaceRecordDAO>>>() {

            @Override
            public void onFailure(Throwable caught) {
                fail("Failed to list races." + caught.getLocalizedMessage());
            }

            @Override
            public void onSuccess(Pair<String, List<TracTracRaceRecordDAO>> result) {
                System.out.println("Listed races.");
                for (TracTracRaceRecordDAO rr : result.getB()){
                    if (rr.name.equals(TRACKED_RACE)){
                        rrDao = rr;
                    }
                }
                //assertNotNull("rrDao != null",rrDao);
                assertNull(rrDao);
                trackRace();
            }
        });
    }
    
    private void trackRace(){
        service.track(rrDao, "", "", false, false, new AsyncCallback<Void>() {

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
        service.createLeaderboard(LEADERBOARD_NAME, new int[] { 1, 2 },
                new AsyncCallback<Void>() {

                    @Override
                    public void onSuccess(Void result) {
                        System.out.println("Created Leaderboard.");
                        addColumnToLeaderboard();
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        fail("Failed to create Leaderboard." + caught.getLocalizedMessage());
                        finishTest();
                    }
                });
    }
    
    private void addColumnToLeaderboard() {
        leaderboardPanel = new LeaderboardPanelMock(service, LEADERBOARD_NAME,
                null, sc);

        service.addColumnToLeaderboard(COLUMN1_NAME, LEADERBOARD_NAME, false,
                new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        fail("Failed to add column to leaderboard.");
                        finishTest();
                    }

                    @Override
                    public void onSuccess(Void result) {
                        System.out.println("Added column to leaderboard.");
                        leaderboardPanel.addColumn(leaderboardPanel.createRaceColumn(COLUMN1_NAME, false, false));
                        linkTrackedRace();
                    }
                });
    }
    
    private void linkTrackedRace(){
        service.connectTrackedRaceToLeaderboardColumn(LEADERBOARD_NAME, COLUMN1_NAME, EVENT_NAME, TRACKED_RACE, new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
                fail("Failed to link race.");
            }

            @Override
            public void onSuccess(Void result) {
                System.out.println("Linked race to column.");
                getLeaderboard();
            }
        });
    }
    
    private void getLeaderboard(){
        ArrayList<String> al = new ArrayList<String>();
        al.add(COLUMN1_NAME);
        service.getLeaderboardByName(LEADERBOARD_NAME, new Date(), al,
                new AsyncCallback<LeaderboardDAO>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        fail("Failed to get leaderboard.");
                        finishTest();
                    }

                    @Override
                    public void onSuccess(LeaderboardDAO result) {
                        System.out.println("Got leaderboard.");
                        
                        leaderboard = result;
                        leaderboardPanel.updateLeaderboard(leaderboard);
                        for (int i = 0; i < leaderboardPanel.getLeaderboardTable()
                                .getColumnCount(); i++) {
                            Column<LeaderboardRowDAO, ?> c = leaderboardPanel.getLeaderboardTable().getColumn(i);
                            if (c instanceof ExpandableSortableColumn<?>) {
                                @SuppressWarnings("unchecked")
                                ExpandableSortableColumn<LeaderboardRowDAO> myRc = (ExpandableSortableColumn<LeaderboardRowDAO>) c;
                                rc = myRc;
                                rc.setEnableLegDrillDown(true);
                            }
                        }
                        removeColumnAndAssert();
                    }
                });
    }
    
    private void removeColumnAndAssert(){
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
                            rc.toggleExpansion();
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

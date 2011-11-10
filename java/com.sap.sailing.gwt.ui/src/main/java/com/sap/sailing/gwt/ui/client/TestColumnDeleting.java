package com.sap.sailing.gwt.ui.client;

import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.client.LeaderboardPanel.RaceColumn;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDAO;
import com.sap.sailing.gwt.ui.shared.Pair;
import com.sap.sailing.gwt.ui.shared.RaceRecordDAO;
import com.sap.sailing.gwt.ui.test.LeaderboardPanelMock;
import com.sap.sailing.gwt.ui.test.TestSailingService;
import com.sap.sailing.gwt.ui.test.TestSailingServiceAsync;

public class TestColumnDeleting extends GWTTestCase {
    private LeaderboardPanelMock leaderboardPanel;
    private TestSailingServiceAsync service;
    private StringConstants sc;
    
    private final String LEADERBOARD_NAME = "test";
    private final String COLUMN1_NAME = "r1";
    private final String EVENT_NAME = "kielerwoche";
    private final String JSON_URL= "http://germanmaster.traclive.dk/events/event_20110609_KielerWoch/jsonservice.php";
    private final String TRACKED_RACE = "49eryellow1";
    
    
    private LeaderboardDAO leaderboard;
    private RaceRecordDAO rrDao;
    private RaceColumn<LeaderboardRowDAO> rc;
    private int indexOfRaceColumn;
    
    

    @Override
    protected void gwtSetUp() throws Exception {
        // TODO Auto-generated method stub
        super.gwtSetUp();
        service = GWT.create(TestSailingService.class);
        sc = GWT.create(StringConstants.class);
    }

    public void testColumnDeleting() {
        delayTestFinish(1000000);
        
        listRacesInEvent();
        
    }
    
    private void linkTrackedRace(){
        service.connectTrackedRaceToLeaderboardColumn(LEADERBOARD_NAME, COLUMN1_NAME, EVENT_NAME, TRACKED_RACE, new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onSuccess(Void result) {
                // TODO Auto-generated method stub
                removeColumnAndAssert();
            }
        });
    }
    
    private void listRacesInEvent(){
        service.listRacesInEvent(JSON_URL, new AsyncCallback<Pair<String,List<RaceRecordDAO>>>() {

            @Override
            public void onFailure(Throwable caught) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onSuccess(Pair<String, List<RaceRecordDAO>> result) {
                for (RaceRecordDAO rr : result.getB()){
                    if (rr.name.toLowerCase().trim().equals(TRACKED_RACE)){
                        rrDao = rr;
                    }
                }
                trackRace();
            }
        });
    }
    
    private void trackRace(){
        service.track(rrDao, "", "", false, false, new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onSuccess(Void result) {
                // TODO Auto-generated method stub
                createLeaderboard();
            }
        });
    }
    
    private void createLeaderboard(){
        service.createLeaderboard(LEADERBOARD_NAME, new int[] { 1, 2 },
                new AsyncCallback<Void>() {

                    @Override
                    public void onSuccess(Void result) {
                        // TODO Auto-generated method stub
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
                        getLeaderboard();
                    }
                });
    }
    
    private void getLeaderboard(){
        service.getLeaderboardByName(LEADERBOARD_NAME, new Date(), null,
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
                        System.out.println("Legcount: " + leaderboard.getLegCount(COLUMN1_NAME));
                        for (int i = 0; i < leaderboardPanel.getLeaderboardTable()
                                .getColumnCount(); i++) {
                            Column<LeaderboardRowDAO, ?> c = leaderboardPanel.getLeaderboardTable().getColumn(i);
                            System.out.println(i + ": " + c);
                            if (c instanceof RaceColumn<?>) {
                                
                                rc = (RaceColumn<LeaderboardRowDAO>) c;
                                rc.setEnableLegDrillDown(true);
                                indexOfRaceColumn = i;
                            }
                        }
                        linkTrackedRace();
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
                        System.out.println("Removed column.");
                        
                        leaderboardPanel.removeColumn(indexOfRaceColumn);
                        assertNotNull(rc);
                        rc.toggleExpansion();
                        finishTest();
                    }
                });
    }

    
    
    @Override
    protected void gwtTearDown() throws Exception {
        // TODO Auto-generated method stub
        //super.gwtTearDown();
        
    }

    @Override
    public String getModuleName() {
        // TODO Auto-generated method stub
        return "com.sap.sailing.gwt.ui.AdminConsole";
    }

}

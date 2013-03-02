package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.adminconsole.AdminConsoleTableResources;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.FleetDTO;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;
import com.sap.sailing.gwt.ui.shared.RaceLogDTO;
import com.sap.sailing.gwt.ui.shared.RaceLogEventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class RegattaOverviewPanel extends FlowPanel {
    private final SailingServiceAsync sailingService;
    @SuppressWarnings("unused")
    private final ErrorReporter errorReporter;
    @SuppressWarnings("unused")
    private final StringMessages stringMessages;

    private final Label label;

    private final VerticalPanel mainPanel;

    public RegattaOverviewPanel(final SailingServiceAsync sailingService, 
            ErrorReporter errorReporter, final StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;

        mainPanel = new VerticalPanel();
        this.add(mainPanel);

        label = new Label("Hallo world");
        mainPanel.add(label);

        loadEventLog();
    }

    private void loadEventLog() {
        sailingService.getLeaderboards(new AsyncCallback<List<StrippedLeaderboardDTO>>() {
            
            @Override
            public void onSuccess(List<StrippedLeaderboardDTO> result) {
                for(StrippedLeaderboardDTO leaderboard : result){
                    /**final AdminConsoleTableResources tableRes = GWT.create(RegattaOverviewTableResources.class);
                    CellTable<StrippedLeaderboardDTO>  regattaTable = new CellTable<StrippedLeaderboardDTO>(10000, tableRes);
                    regattaTable.setWidth("100%");
                    TextColumn<RaceColumnDTO> regattaNameColumn = new TextColumn<RaceColumnDTO>() {
                        @Override
                        public String getValue(RaceColumnDTO race) {
                            return race.name;
                        }
                    };*/
                    
                    for(RaceColumnDTO race :leaderboard.getRaceList()){
                        RaceLogDTO raceLog = null;
                        for(FleetDTO fleet : race.getFleets()){
                            raceLog = race.getRaceLog(fleet);
                        }
                        
                        mainPanel.add(new Label(race.name));
                        for(RaceLogEventDTO raceLogEventDTO : raceLog.raceEvents){
                            mainPanel.add(new Label(raceLogEventDTO.passId+""));
                        }
                    }
                }
            }
            
            @Override
            public void onFailure(Throwable caught) {
                // TODO Auto-generated method stub
                
            }
        });
        /**sailingService.getRaceEventLog(new AsyncCallback<RaceEventLogDTO>() {

            @Override
            public void onSuccess(RaceEventLogDTO result) {
                label.setText(result.raceName);
                int i = 0;
                for(RaceEventDTO raceEvent: result.raceEvents) {
                    mainPanel.add(new Label(i++ + ".) " + raceEvent.eventName));
                }
            }

            @Override
            public void onFailure(Throwable arg0) {
            }
        });*/
    }
}

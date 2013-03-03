package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.FleetDTO;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;
import com.sap.sailing.gwt.ui.shared.RaceInfoDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class RegattaOverviewPanel extends FlowPanel {
    private final SailingServiceAsync sailingService;
    @SuppressWarnings("unused")
    private final ErrorReporter errorReporter;
    @SuppressWarnings("unused")
    private final StringMessages stringMessages;

    private final VerticalPanel mainPanel;
    
    private final String leaderboardName;

    public RegattaOverviewPanel(final SailingServiceAsync sailingService, 
            ErrorReporter errorReporter, final StringMessages stringMessages, String leaderboardName) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;

        mainPanel = new VerticalPanel();
        this.add(mainPanel);
        
        this.leaderboardName = leaderboardName;

        loadEventLog();
    }

    private void loadEventLog() {
        sailingService.getLeaderboard(this.leaderboardName, new AsyncCallback<StrippedLeaderboardDTO>() {

            @Override
            public void onSuccess(StrippedLeaderboardDTO leaderboard) {

                if (leaderboard != null) {
                    final RegattaOverviewTableResources tableRes = GWT.create(RegattaOverviewTableResources.class);
                    CellTable<RaceInfoDTO> regattaTable = new CellTable<RaceInfoDTO>(10, tableRes);
                    regattaTable.setWidth("100%");
                    TextColumn<RaceInfoDTO> raceNameColumn = new TextColumn<RaceInfoDTO>() {
                        @Override
                        public String getValue(RaceInfoDTO raceInfo) {
                            return raceInfo.raceName;
                        }
                    };
                    
                    TextColumn<RaceInfoDTO> raceStartTimeColumn = new TextColumn<RaceInfoDTO>() {
                        @Override
                        public String getValue(RaceInfoDTO raceInfo) {
                            return raceInfo.startTime.toString();
                        }
                    };
                    
                    regattaTable.addColumn(raceNameColumn, "Race Name");
                    regattaTable.addColumn(raceStartTimeColumn, "Starttime");
                    
                 // Create a data provider.
                    ListDataProvider<RaceInfoDTO> dataProvider = new ListDataProvider<RaceInfoDTO>();

                    // Connect the table to the data provider.
                    dataProvider.addDataDisplay(regattaTable);

                    // Add the data to the data provider, which automatically pushes it to the
                    // widget.
                    List<RaceInfoDTO> list = dataProvider.getList();

                    mainPanel.add(regattaTable);
                    for (RaceColumnDTO race : leaderboard.getRaceList()) {
                        RaceInfoDTO raceInfo = null;
                        for (FleetDTO fleet : race.getFleets()) {
                            raceInfo = race.getRaceLog(fleet);
                            list.add(raceInfo);
                        }
                    }

                }
            }
            
            @Override
            public void onFailure(Throwable caught) {
                // TODO Auto-generated method stub
                
            }
        });
    }
}

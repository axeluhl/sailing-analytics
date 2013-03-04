package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TimeListener;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.shared.FleetDTO;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;
import com.sap.sailing.gwt.ui.shared.RaceInfoDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class RegattaOverviewTableComposite extends Composite implements TimeListener {
    
    private final long updateRate = 1000;

    private final CellTable<RaceInfoDTO> regattaOverviewTable;
    private ListDataProvider<RaceInfoDTO> raceListDataProvider;
    private final SimplePanel mainPanel;
    private final VerticalPanel panel;
    private final Timer updateTimer; 
    private final Label timeLabel;
    private final DateTimeFormat timeFormatter = DateTimeFormat.getFormat("HH:mm:ss");

    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    private final String leaderboardName;

    private static RegattaOverviewTableResources tableRes = GWT.create(RegattaOverviewTableResources.class);

    public RegattaOverviewTableComposite(final SailingServiceAsync sailingService, 
            ErrorReporter errorReporter, final StringMessages stringMessages,final String leaderboardName) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.leaderboardName = leaderboardName;
        
        this.updateTimer = new Timer(PlayModes.Live, updateRate);
        this.updateTimer.addTimeListener(this);
        this.updateTimer.play();
        
        
        mainPanel = new SimplePanel();
        panel = new VerticalPanel();
        mainPanel.setWidget(panel);

        raceListDataProvider = new ListDataProvider<RaceInfoDTO>();
        regattaOverviewTable = createRegattaTable();
        
        timeLabel = new Label();
        
        panel.add(timeLabel);
        panel.add(regattaOverviewTable);
        
        initWidget(mainPanel);
    }

    
    @Override
    public void timeChanged(Date time) {
        timeLabel.setText(timeFormatter.format(time));
        loadAndUpdateEventLog();

    }
    
    private void loadAndUpdateEventLog() {
        sailingService.getLeaderboard(this.leaderboardName, new AsyncCallback<StrippedLeaderboardDTO>() {

            @Override
            public void onSuccess(StrippedLeaderboardDTO leaderboard) {
                raceListDataProvider.getList().clear();
                if (leaderboard != null) {
                    for (RaceColumnDTO race : leaderboard.getRaceList()) {
                        for (FleetDTO fleet : race.getFleets()) {
                            raceListDataProvider.getList().add(race.getRaceLog(fleet));
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

    private CellTable<RaceInfoDTO> createRegattaTable() {
        CellTable<RaceInfoDTO> table = new CellTable<RaceInfoDTO>(/* pageSize */10000, tableRes);
        raceListDataProvider.addDataDisplay(table);
        table.setWidth("100%");
        
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
        
        table.addColumn(raceNameColumn, "Race Name");
        table.addColumn(raceStartTimeColumn, "Starttime");
        
        return table;
    }
}

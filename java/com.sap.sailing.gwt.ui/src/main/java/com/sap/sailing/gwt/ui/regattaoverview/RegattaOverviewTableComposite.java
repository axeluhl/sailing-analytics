package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.Date;

import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
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
import com.sap.sailing.gwt.ui.client.Timer.PlayStates;
import com.sap.sailing.gwt.ui.shared.FleetDTO;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;
import com.sap.sailing.gwt.ui.shared.RaceInfoDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class RegattaOverviewTableComposite extends Composite {

    private final long serverUpdateRate = 5000;
    private final long uiUpdateRate = 1000;

    private final CellTable<RaceInfoDTO> regattaOverviewTable;
    private ListDataProvider<RaceInfoDTO> raceListDataProvider;
    private final SimplePanel mainPanel;
    private final VerticalPanel panel;
    private final Timer serverUpdateTimer;
    private final Timer uiUpdateTimer;
    private final Label timeLabel;
    private final Button startStopUpdatingButton;
    private final DateTimeFormat timeFormatter = DateTimeFormat.getFormat("HH:mm:ss");

    private final SailingServiceAsync sailingService;
    private final StringMessages stringMessages;
    private final String leaderboardName;

    private static RegattaOverviewResources resources = GWT.create(RegattaOverviewResources.class);

    private static RegattaOverviewTableResources tableRes = GWT.create(RegattaOverviewTableResources.class);

    public RegattaOverviewTableComposite(final SailingServiceAsync sailingService, ErrorReporter errorReporter,
            final StringMessages stringMessages, final String leaderboardName) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.leaderboardName = leaderboardName;

        this.serverUpdateTimer = new Timer(PlayModes.Live, serverUpdateRate);
        this.serverUpdateTimer.addTimeListener(new TimeListener() {

            @Override
            public void timeChanged(Date date) {
                RegattaOverviewTableComposite.this.onUpdateServer(date);
            }
        });
        this.serverUpdateTimer.play();

        this.uiUpdateTimer = new Timer(PlayModes.Live, uiUpdateRate);
        this.uiUpdateTimer.addTimeListener(new TimeListener() {

            @Override
            public void timeChanged(Date date) {
                RegattaOverviewTableComposite.this.onUpdateUI(date);
            }
        });
        this.uiUpdateTimer.play();

        mainPanel = new SimplePanel();
        panel = new VerticalPanel();
        mainPanel.setWidget(panel);

        raceListDataProvider = new ListDataProvider<RaceInfoDTO>();
        regattaOverviewTable = createRegattaTable();

        timeLabel = new Label();
        
        Button refreshNowButton = new Button("Refresh now");
        refreshNowButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                loadAndUpdateEventLog();
            }
            
        });
        
        this.startStopUpdatingButton = new Button("Stop updating");
        this.startStopUpdatingButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (serverUpdateTimer.getPlayState().equals(PlayStates.Playing)) {
                    serverUpdateTimer.pause();
                    startStopUpdatingButton.setText("Start updating");
                } else if (serverUpdateTimer.getPlayState().equals(PlayStates.Paused)) {
                    serverUpdateTimer.play();
                    startStopUpdatingButton.setText("Stop updating");
                }
            }
            
        });
        
        Grid gridTimeRefreshStop = new Grid(1, 4);
        gridTimeRefreshStop.setWidget(0, 0, new Label("Current time: "));
        gridTimeRefreshStop.setWidget(0, 1, timeLabel);
        gridTimeRefreshStop.setWidget(0, 2, refreshNowButton);
        gridTimeRefreshStop.setWidget(0, 3, startStopUpdatingButton);

        panel.add(gridTimeRefreshStop);
        panel.add(regattaOverviewTable);

        initWidget(mainPanel);
        
        loadAndUpdateEventLog();
        onUpdateUI(new Date());
    }

    public void onUpdateServer(Date time) {
        loadAndUpdateEventLog();
    }

    public void onUpdateUI(Date time) {
        timeLabel.setText(timeFormatter.format(time));
    }

    private void loadAndUpdateEventLog() {
        sailingService.getLeaderboard(this.leaderboardName, new AsyncCallback<StrippedLeaderboardDTO>() {

            @Override
            public void onSuccess(StrippedLeaderboardDTO leaderboard) {
                raceListDataProvider.getList().clear();
                if (leaderboard != null) {
                    for (RaceColumnDTO race : leaderboard.getRaceList()) {
                        for (FleetDTO fleet : race.getFleets()) {
                            raceListDataProvider.getList().add(race.getRaceInfo(fleet));
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
        
        TextColumn<RaceInfoDTO> fleetNameColumn = new TextColumn<RaceInfoDTO>() {
            @Override
            public String getValue(RaceInfoDTO raceInfo) {
                return raceInfo.fleet;
            }
        };

        TextColumn<RaceInfoDTO> raceNameColumn = new TextColumn<RaceInfoDTO>() {
            @Override
            public String getValue(RaceInfoDTO raceInfo) {
                return raceInfo.raceName;
            }
        };

        TextColumn<RaceInfoDTO> raceStartTimeColumn = new TextColumn<RaceInfoDTO>() {
            @Override
            public String getValue(RaceInfoDTO raceInfo) {
                String result = stringMessages.noStarttimeAnnouncedYet();
                if (raceInfo.startTime != null) {
                    result = timeFormatter.format(raceInfo.startTime);
                }
                return result;
            }
        };
        
        TextColumn<RaceInfoDTO> raceStatusColumn = new TextColumn<RaceInfoDTO>() {
            @Override
            public String getValue(RaceInfoDTO raceInfo) {
                String result = "";
                if(raceInfo.lastStatus!=null)
                    result =  raceInfo.lastStatus.toString();
                return result;
            }
        };

        Column<RaceInfoDTO, ImageResource> lastFlagColumn = new Column<RaceInfoDTO, ImageResource>(new ImageResourceCell()) {
            @Override
            public ImageResource getValue(RaceInfoDTO raceInfo) {
                return resources.flagAP();
            }
        };
        
        Column<RaceInfoDTO, ImageResource> lastFlagDirectionColumn = new Column<RaceInfoDTO, ImageResource>(new ImageResourceCell()) {
            @Override
            public ImageResource getValue(RaceInfoDTO object) {
                return resources.arrowUp();
            }
        };

        table.addColumn(fleetNameColumn, stringMessages.fleet());
        table.addColumn(raceNameColumn, stringMessages.race());
        table.addColumn(raceStartTimeColumn, stringMessages.startTime());
        table.addColumn(raceStatusColumn, stringMessages.status());
        table.addColumn(lastFlagColumn, stringMessages.lastFlag());
        table.addColumn(lastFlagDirectionColumn, "");

        return table;
    }
}

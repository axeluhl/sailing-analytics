package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.MarkedAsyncCallback;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TimeListener;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.client.Timer.PlayStates;
import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;

public class RegattaOverviewTableComposite extends Composite {

    private final long serverUpdateRate = 10000;
    private final long uiUpdateRate = 1000;

    private final SelectionModel<RegattaOverviewEntryDTO> raceSelectionModel;
    private final CellTable<RegattaOverviewEntryDTO> regattaOverviewTable;
    private ListDataProvider<RegattaOverviewEntryDTO> regattaOverviewDataProvider;
    private final SimplePanel mainPanel;
    private final VerticalPanel panel;
    private final Timer serverUpdateTimer;
    private final Timer uiUpdateTimer;
    private final Label timeLabel;
    private final Button startStopUpdatingButton;
    private final DateTimeFormat timeFormatter = DateTimeFormat.getFormat("HH:mm:ss");

    private final SailingServiceAsync sailingService;
    private final StringMessages stringMessages;
    private final String eventIdAsString;
    private final RegattaOverviewRaceSelectionProvider raceSelectionProvider;

    private static RegattaOverviewResources resources = GWT.create(RegattaOverviewResources.class);

    private static RegattaOverviewTableResources tableRes = GWT.create(RegattaOverviewTableResources.class);

    public RegattaOverviewTableComposite(final SailingServiceAsync sailingService, ErrorReporter errorReporter,
            final StringMessages stringMessages, final String eventIdAsString, final RegattaOverviewRaceSelectionProvider raceSelectionProvider) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.eventIdAsString = eventIdAsString;
        this.raceSelectionProvider = raceSelectionProvider;

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

        regattaOverviewDataProvider = new ListDataProvider<RegattaOverviewEntryDTO>();
        regattaOverviewTable = createRegattaTable();
        
        timeLabel = new Label();
        
        Button refreshNowButton = new Button(stringMessages.refreshNow());
        refreshNowButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                loadAndUpdateEventLog();
            }
            
        });
        
        this.startStopUpdatingButton = new Button(stringMessages.stopUpdating());
        this.startStopUpdatingButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (serverUpdateTimer.getPlayState().equals(PlayStates.Playing)) {
                    serverUpdateTimer.pause();
                    startStopUpdatingButton.setText(stringMessages.startUpdating());
                } else if (serverUpdateTimer.getPlayState().equals(PlayStates.Paused)) {
                    serverUpdateTimer.play();
                    startStopUpdatingButton.setText(stringMessages.stopUpdating());
                }
            }
            
        });
        
        Grid gridTimeRefreshStop = new Grid(1, 4);
        gridTimeRefreshStop.setWidget(0, 0, new Label(stringMessages.currentTime()));
        gridTimeRefreshStop.setWidget(0, 1, timeLabel);
        gridTimeRefreshStop.setWidget(0, 2, refreshNowButton);
        gridTimeRefreshStop.setWidget(0, 3, startStopUpdatingButton);

        raceSelectionModel = new SingleSelectionModel<RegattaOverviewEntryDTO>();
        raceSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                List<RegattaOverviewEntryDTO> selectedRaces = getSelectedRaces();
                List<RaceIdentifier> selectedRaceIdentifiers = new ArrayList<RaceIdentifier>();
                for (RegattaOverviewEntryDTO selectedRace : selectedRaces) {
                    selectedRaceIdentifiers.add(selectedRace.raceInfo.raceIdentifier);
                }
                RegattaOverviewTableComposite.this.raceSelectionProvider.setSelection(selectedRaces);
            }
            
        });
        
        regattaOverviewTable.setSelectionModel(raceSelectionModel);
        
        panel.add(gridTimeRefreshStop);
        panel.add(regattaOverviewTable);

        initWidget(mainPanel);
        
        loadAndUpdateEventLog();
        onUpdateUI(new Date());
    }

    protected List<RegattaOverviewEntryDTO> getSelectedRaces() {
        List<RegattaOverviewEntryDTO> result = new ArrayList<RegattaOverviewEntryDTO>();
        if (regattaOverviewDataProvider != null) {
            for (RegattaOverviewEntryDTO race : regattaOverviewDataProvider.getList()) {
                if (raceSelectionModel.isSelected(race)) {
                    result.add(race);
                }
            }
        }
        return result;
    }

    public void onUpdateServer(Date time) {
        loadAndUpdateEventLog();
    }

    public void onUpdateUI(Date time) {
        timeLabel.setText(timeFormatter.format(time));
    }

    private void loadAndUpdateEventLog() {
        sailingService.getRegattaOverviewEntriesForEvent(eventIdAsString, new MarkedAsyncCallback<List<RegattaOverviewEntryDTO>>() {

            @Override
            protected void handleFailure(Throwable cause) {
                
            }

            @Override
            protected void handleSuccess(List<RegattaOverviewEntryDTO> result) {
                regattaOverviewDataProvider.getList().clear();
                regattaOverviewDataProvider.getList().addAll(result);
            }
            
        });
    }

    private CellTable<RegattaOverviewEntryDTO> createRegattaTable() {
        CellTable<RegattaOverviewEntryDTO> table = new CellTable<RegattaOverviewEntryDTO>(/* pageSize */10000, tableRes);
        regattaOverviewDataProvider.addDataDisplay(table);
        table.setWidth("100%");
        
        TextColumn<RegattaOverviewEntryDTO> courseAreaColumn = new TextColumn<RegattaOverviewEntryDTO>() {
            @Override
            public String getValue(RegattaOverviewEntryDTO entryDTO) {
                return entryDTO.courseAreaName;
            }
        };
        
        TextColumn<RegattaOverviewEntryDTO> regattaNameColumn = new TextColumn<RegattaOverviewEntryDTO>() {
            @Override
            public String getValue(RegattaOverviewEntryDTO entryDTO) {
                return entryDTO.regattaName;
            }
        };
        
        TextColumn<RegattaOverviewEntryDTO> fleetNameColumn = new TextColumn<RegattaOverviewEntryDTO>() {
            @Override
            public String getValue(RegattaOverviewEntryDTO entryDTO) {
                return entryDTO.raceInfo.fleet;
            }
        };

        TextColumn<RegattaOverviewEntryDTO> raceNameColumn = new TextColumn<RegattaOverviewEntryDTO>() {
            @Override
            public String getValue(RegattaOverviewEntryDTO entryDTO) {
                return entryDTO.raceInfo.raceName;
            }
        };

        TextColumn<RegattaOverviewEntryDTO> raceStartTimeColumn = new TextColumn<RegattaOverviewEntryDTO>() {
            @Override
            public String getValue(RegattaOverviewEntryDTO entryDTO) {
                String result = stringMessages.noStarttimeAnnouncedYet();
                if (entryDTO.raceInfo.startTime != null) {
                    result = timeFormatter.format(entryDTO.raceInfo.startTime);
                }
                return result;
            }
        };
        
        TextColumn<RegattaOverviewEntryDTO> raceStatusColumn = new TextColumn<RegattaOverviewEntryDTO>() {
            @Override
            public String getValue(RegattaOverviewEntryDTO entryDTO) {
                String result = "";
                if(entryDTO.raceInfo.lastStatus != null)
                    result = entryDTO.raceInfo.lastStatus.toString();
                return result;
            }
        };

        Column<RegattaOverviewEntryDTO, ImageResource> lastFlagColumn = new Column<RegattaOverviewEntryDTO, ImageResource>(new ImageResourceCell()) {
            @Override
            public ImageResource getValue(RegattaOverviewEntryDTO entryDTO) {
                return resources.flagAP();
            }
        };
        
        Column<RegattaOverviewEntryDTO, ImageResource> lastFlagDirectionColumn = new Column<RegattaOverviewEntryDTO, ImageResource>(new ImageResourceCell()) {
            @Override
            public ImageResource getValue(RegattaOverviewEntryDTO entryDTO) {
                return resources.arrowUp();
            }
        };

        table.addColumn(courseAreaColumn, stringMessages.courseArea());
        table.addColumn(regattaNameColumn, stringMessages.boatClass()); // For sailors the boat class also contains additional infos such as woman/man, e.g. Laser Radial Woman or Laser Radial Men
        table.addColumn(fleetNameColumn, stringMessages.fleet());
        table.addColumn(raceNameColumn, stringMessages.race());
        table.addColumn(raceStartTimeColumn, stringMessages.startTime());
        table.addColumn(raceStatusColumn, stringMessages.status());
        table.addColumn(lastFlagColumn, stringMessages.lastFlag());
        table.addColumn(lastFlagDirectionColumn, "");

        return table;
    }
    
    public List<RegattaOverviewEntryDTO> getAllRaces() {
        return regattaOverviewDataProvider.getList();
    }
}

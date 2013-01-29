package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.ColumnFormatter;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionModel;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.MarkedAsyncCallback;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.TracTracConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.TracTracRaceRecordDTO;

/**
 * Allows the user to start and stop tracking of events, regattas and races using the TracTrac connector. In particular,
 * previously configured connections can be retrieved from a drop-down list which then pre-populates all connection
 * parameters. The user can also choose to enter connection information manually. Using a "hierarchical" entry system
 * comparable to that of, e.g., the Eclipse CVS connection setup wizard, components entered will be used to
 * automatically assemble the full URL which can still be overwritten manually. There is a propagation order across the
 * fields. Hostname propagates to JSON URL, Live URI and Stored URI. Port Live Data propagates to Port Stored Data,
 * incremented by one. The ports propagate to Live URI and Stored URI, respectively. The event name propagates to the
 * JSON URL.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
// TODO: Do not inherit from FormPanel since the provided functionality is never used!
public class TracTracEventManagementPanel extends AbstractEventManagementPanel {
    private final ErrorReporter errorReporter;
    
    private final Map<String, TracTracConfigurationDTO> previousConfigurations;

    private final List<TracTracRaceRecordDTO> availableTracTracRaces;
    
    private final ListDataProvider<TracTracRaceRecordDTO> raceList;
    
    private ListBox connectionsHistoryListBox;
    
    private TextBox eventNameTextBox;
    private TextBox hostnameTextBox;

    private IntegerBox storedDataPortIntegerBox;
    private IntegerBox liveDataPortIntegerBox;
    
    private TextBox storedURITextBox;
    private TextBox liveURITextBox;
    private TextBox jsonURLTextBox;

    private TextBox racesFilterTextBox;
    private CellTable<TracTracRaceRecordDTO> racesTable;


    public TracTracEventManagementPanel(final SailingServiceAsync sailingService, ErrorReporter errorReporter,
            RegattaRefresher regattaRefresher, StringMessages stringMessages) {
        super(sailingService, regattaRefresher, errorReporter, new RaceSelectionModel(), stringMessages);
        this.errorReporter = errorReporter;
        this.previousConfigurations = new HashMap<String, TracTracConfigurationDTO>();
        this.availableTracTracRaces = new ArrayList<TracTracRaceRecordDTO>();
        this.raceList = new ListDataProvider<TracTracRaceRecordDTO>();
        this.setWidget(createContent());
    }
    
    protected Widget createContent() {
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setWidth("100%");

        CaptionPanel connectionsPanel = createConnectionsPanel();
        //connectionsPanel.setWidth("100%");

        mainPanel.add(connectionsPanel);

        HorizontalPanel racesPanel = createRacesPanel();
        racesPanel.setWidth("100%");

        mainPanel.add(racesPanel);
        
        fillConfigurations();
        synchURIs();
        
        return mainPanel;
    }
    
    protected CaptionPanel createConnectionsPanel() {
        CaptionPanel connectionsPanel = new CaptionPanel(this.stringMessages.connections());
        connectionsPanel.ensureDebugId("ConnectionsSection");
        connectionsPanel.setStyleName("bold");

        FlexTable layoutTable = new FlexTable();
        layoutTable.setWidth("100%");

        ColumnFormatter columnFormatter = layoutTable.getColumnFormatter();
        FlexCellFormatter cellFormatter = layoutTable.getFlexCellFormatter();

        columnFormatter.setWidth(0, "130px");
        //columnFormatter.setWidth(1, "90%");

        // History of connections
        Label connectionsHistoryLabel = new Label(this.stringMessages.historyOfConnections() + ":");

        this.connectionsHistoryListBox = new ListBox();
        this.connectionsHistoryListBox.ensureDebugId("ConnectionHistory");
        this.connectionsHistoryListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                updatePanelFromSelectedStoredConfiguration();
            }
        });
        this.connectionsHistoryListBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // TODO: This leads to a strange behavior (values are updated by opening the drop down) but at the
                //       moment there is no other way to "reset" the values to the selected value, if the selection does
                //       not change since there is no close event or something similar.
                updatePanelFromSelectedStoredConfiguration();
            }
        });

        layoutTable.setWidget(0, 0, connectionsHistoryLabel);
        layoutTable.setWidget(0, 1, this.connectionsHistoryListBox);

        // Definition of new connection
        Label defineNewConnectionLabel = new Label(this.stringMessages.defineNewConnection());
        Element defineNewConnectionElement = defineNewConnectionLabel.getElement();
        Style defineNewConnectionStyle = defineNewConnectionElement.getStyle();
        defineNewConnectionStyle.setPaddingTop(30, Unit.PX);
        defineNewConnectionStyle.setPaddingBottom(10, Unit.PX);
        
        layoutTable.setWidget(1, 0, defineNewConnectionLabel);
        cellFormatter.setColSpan(1, 0, 2);

        // Host name
        Label hostnameLabel = new Label(this.stringMessages.hostname() + ":");

        this.hostnameTextBox = new TextBox();
        this.hostnameTextBox.ensureDebugId("HostName");
        this.hostnameTextBox.setText("germanmaster.traclive.dk");
        this.hostnameTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                synchURIs();
            }
        });

        layoutTable.setWidget(2, 0, hostnameLabel);
        layoutTable.setWidget(2, 1, this.hostnameTextBox);

        // Regatta name
        Label eventNameLabel = new Label(this.stringMessages.eventName() + ":");

        this.eventNameTextBox = new TextBox();
        this.eventNameTextBox.ensureDebugId("RegattaName");
        this.eventNameTextBox.setText("event_2011...");
        this.eventNameTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                synchURIs();
            }
        });

        layoutTable.setWidget(3, 0, eventNameLabel);
        layoutTable.setWidget(3, 1, this.eventNameTextBox);

        // Ports (Live and Stored)
        Label portsLabel = new Label(this.stringMessages.ports() + ":");

        layoutTable.setWidget(4, 0, portsLabel);

        Label liveDataPortLabel = new Label(this.stringMessages.liveData());
        Element liveDataPortElement = liveDataPortLabel.getElement();
        Style liveDataPortStyle = liveDataPortElement.getStyle();
        liveDataPortStyle.setProperty("paddingLeft", "30px");

        this.liveDataPortIntegerBox = new IntegerBox();
        this.liveDataPortIntegerBox.ensureDebugId("LiveDataPort");
        this.liveDataPortIntegerBox.setText(Integer.toString(4400));
        this.liveDataPortIntegerBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                updateStoredDataPort();
                synchURIs();
            }
        });

        layoutTable.setWidget(5, 0, liveDataPortLabel);
        layoutTable.setWidget(5, 1, this.liveDataPortIntegerBox);

        Label storedDataPortLabel = new Label(this.stringMessages.storedData());
        Element storedDataPortElement = storedDataPortLabel.getElement();
        Style storedDataPortStyle = storedDataPortElement.getStyle();
        storedDataPortStyle.setProperty("paddingLeft", "30px");

        this.storedDataPortIntegerBox = new IntegerBox();
        this.storedDataPortIntegerBox.ensureDebugId("StoredDataPort");
        this.storedDataPortIntegerBox.setText(Integer.toString(4401));
        this.storedDataPortIntegerBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                synchURIs();
            }
        });

        layoutTable.setWidget(6, 0, storedDataPortLabel);
        layoutTable.setWidget(6, 1, this.storedDataPortIntegerBox);

        // URIs (Live and Stored)
        Label urisLabel = new Label(this.stringMessages.uris() + ":");
        urisLabel.setTitle(this.stringMessages.leaveEmptyForDefault());

        layoutTable.setWidget(7, 0, urisLabel);

        Label liveURILabel = new Label(this.stringMessages.liveUri());
        liveURILabel.setTitle(this.stringMessages.leaveEmptyForDefault());
        Element liveURIElement = liveURILabel.getElement();
        Style liveURIStyle = liveURIElement.getStyle();
        liveURIStyle.setProperty("paddingLeft", "30px");

        this.liveURITextBox = new TextBox();
        this.liveURITextBox.ensureDebugId("LiveURI");
        this.liveURITextBox.setVisibleLength(40);
        this.liveURITextBox.setTitle(this.stringMessages.leaveEmptyForDefault());

        layoutTable.setWidget(8, 0, liveURILabel);
        layoutTable.setWidget(8, 1, this.liveURITextBox);

        Label storedURILabel = new Label(this.stringMessages.storedUri());
        storedURILabel.setTitle(this.stringMessages.leaveEmptyForDefault());
        Element storedURIElement = storedURILabel.getElement();
        Style storedURIStyle = storedURIElement.getStyle();
        storedURIStyle.setProperty("paddingLeft", "30px");

        this.storedURITextBox = new TextBox();
        this.storedURITextBox.ensureDebugId("StoredURI");
        this.storedURITextBox.setVisibleLength(40);
        this.storedURITextBox.setTitle(this.stringMessages.leaveEmptyForDefault());

        layoutTable.setWidget(9, 0, storedURILabel);
        layoutTable.setWidget(9, 1, this.storedURITextBox);

        // JSON URL
        Label jsonURLLabel = new Label(this.stringMessages.jsonUrl() + ":");

        this.jsonURLTextBox = new TextBox();
        this.jsonURLTextBox.ensureDebugId("JSONURL");
        this.jsonURLTextBox.setVisibleLength(100);

        layoutTable.setWidget(10, 0, jsonURLLabel);
        layoutTable.setWidget(10, 1, this.jsonURLTextBox);

        // List Races
        Button listRacesButton = new Button(this.stringMessages.listRaces());
        listRacesButton.ensureDebugId("ListRaces");
        listRacesButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fillRaces(sailingService);
            }
        });

        layoutTable.setWidget(11, 1, listRacesButton);

        connectionsPanel.setContentWidget(layoutTable);

        return connectionsPanel;
    }

    protected HorizontalPanel createRacesPanel() {
        HorizontalPanel racesPanel = new HorizontalPanel();
        
        CaptionPanel trackableRacesPanel = createTrackableRacesPanel();
        //trackableRacesPanel.setWidth("100%");
        
        racesPanel.add(trackableRacesPanel);
        racesPanel.setCellWidth(trackableRacesPanel, "50%");
        
        CaptionPanel trackedRacesPanel = createTrackedRacesPanel();
        //trackedRacesPanel.setWidth("100%");
        
        racesPanel.add(trackedRacesPanel);
        racesPanel.setCellWidth(trackedRacesPanel, "50%");
        
        return racesPanel;
    }
    
    protected CaptionPanel createTrackableRacesPanel() {
        CaptionPanel trackableRacesPanel = new CaptionPanel(this.stringMessages.trackableRaces());
        trackableRacesPanel.ensureDebugId("TrackableRacesSection");
        trackableRacesPanel.setStyleName("bold");
        
        FlexTable layoutTable = new FlexTable();
        layoutTable.setWidth("100%");

        ColumnFormatter columnFormatter = layoutTable.getColumnFormatter();
        FlexCellFormatter cellFormatter = layoutTable.getFlexCellFormatter();

        columnFormatter.setWidth(0, "130px");
        //columnFormatter.setWidth(1, "80%");

        // Regatta
        Label regattaForTrackingLabel = new Label("Regatta used for the tracked race:");
        regattaForTrackingLabel.setWordWrap(false);
        
        layoutTable.setWidget(0, 0, regattaForTrackingLabel);
        layoutTable.setWidget(0, 1, getAvailableRegattasListBox());

        // Track settings (wind)
        Label trackSettingsLabel = new Label(this.stringMessages.trackSettings() + ":");

        final CheckBox trackWindCheckBox = new CheckBox(this.stringMessages.trackWind());
        trackWindCheckBox.ensureDebugId("TrackWind");
        trackWindCheckBox.setWordWrap(false);
        trackWindCheckBox.setValue(Boolean.TRUE);

        final CheckBox correctWindCheckBox = new CheckBox(this.stringMessages.declinationCheckbox());
        correctWindCheckBox.ensureDebugId("CorrectWind");
        correctWindCheckBox.setWordWrap(false);
        correctWindCheckBox.setValue(Boolean.TRUE);

        final CheckBox simulateWithStartTimeNowCheckBox = new CheckBox(this.stringMessages.simulateWithStartTimeNow());
        simulateWithStartTimeNowCheckBox.ensureDebugId("SimulateWithStartTimeNow");
        simulateWithStartTimeNowCheckBox.setWordWrap(false);
        simulateWithStartTimeNowCheckBox.setValue(Boolean.FALSE);
        
        layoutTable.setWidget(1, 0, trackSettingsLabel);
        layoutTable.setWidget(1, 1, trackWindCheckBox);
        layoutTable.setWidget(2, 1, correctWindCheckBox);
        layoutTable.setWidget(3, 1, simulateWithStartTimeNowCheckBox);
        
        // Filter
        Label racesFilterLabel = new Label(this.stringMessages.filterRacesByName() + ":");

        this.racesFilterTextBox = new TextBox();
        this.racesFilterTextBox.ensureDebugId("FilterRaces");
        this.racesFilterTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                fillRaceListFromAvailableRacesApplyingFilter(TracTracEventManagementPanel.this.racesFilterTextBox.getText());
            }
        });

        layoutTable.setWidget(4, 0, racesFilterLabel);
        layoutTable.setWidget(4, 1, this.racesFilterTextBox);

        // Races
        TextColumn<TracTracRaceRecordDTO> regattaNameColumn = new TextColumn<TracTracRaceRecordDTO>() {
            @Override
            public String getValue(TracTracRaceRecordDTO object) {
                return object.regattaName;
            }
        };
        regattaNameColumn.setSortable(false);
        TextColumn<TracTracRaceRecordDTO> boatClassColumn = new TextColumn<TracTracRaceRecordDTO>() {
            @Override
            public String getValue(TracTracRaceRecordDTO object) {
                return getBoatClassNamesAsString(object);
            }
        };
        boatClassColumn.setSortable(true);
        TextColumn<TracTracRaceRecordDTO> raceNameColumn = new TextColumn<TracTracRaceRecordDTO>() {
            @Override
            public String getValue(TracTracRaceRecordDTO object) {
                return object.name;
            }
        };
        raceNameColumn.setSortable(true);
        TextColumn<TracTracRaceRecordDTO> raceStartTrackingColumn = new TextColumn<TracTracRaceRecordDTO>() {
            @Override
            public String getValue(TracTracRaceRecordDTO object) {
                return object.trackingStartTime == null ? "" : dateFormatter.render(object.trackingStartTime) + " "
                        + timeFormatter.render(object.trackingStartTime);
            }
        };
        raceStartTrackingColumn.setSortable(true);
        
        AdminConsoleTableResources tableResources = GWT.create(AdminConsoleTableResources.class);
        this.racesTable = new CellTable<TracTracRaceRecordDTO>(10000, tableResources);
        this.racesTable.ensureDebugId("RacesTable");
        this.racesTable.addColumn(regattaNameColumn, this.stringMessages.event());
        this.racesTable.addColumn(raceNameColumn, this.stringMessages.race());
        this.racesTable.addColumn(boatClassColumn, this.stringMessages.boatClass());
        this.racesTable.addColumn(raceStartTrackingColumn, this.stringMessages.startTime());
        this.racesTable.addColumnSortHandler(getRaceTableColumnSortHandler(this.raceList.getList(), raceNameColumn,
                boatClassColumn, raceStartTrackingColumn));
        this.racesTable.setSelectionModel(new MultiSelectionModel<TracTracRaceRecordDTO>());
        this.racesTable.setWidth("100%");

        this.raceList.addDataDisplay(this.racesTable);

        layoutTable.setWidget(5, 0, this.racesTable);
        cellFormatter.setColSpan(5, 0, 2);
        
        Button startTrackingButton = new Button(this.stringMessages.startTracking());
        startTrackingButton.ensureDebugId("StartTracking");
        startTrackingButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                trackSelectedRaces(trackWindCheckBox.getValue(), correctWindCheckBox.getValue(),
                        simulateWithStartTimeNowCheckBox.getValue());
            }
        });

        layoutTable.setWidget(6, 1, startTrackingButton);

        trackableRacesPanel.setContentWidget(layoutTable);

        return trackableRacesPanel;
    }
    
    protected CaptionPanel createTrackedRacesPanel() {
        CaptionPanel trackedRacesPanel = new CaptionPanel(this.stringMessages.trackedRaces());
        trackedRacesPanel.ensureDebugId("TrackedRacesSection");
        trackedRacesPanel.setStyleName("bold");
        
        trackedRacesPanel.setContentWidget(this.trackedRacesListComposite);

        return trackedRacesPanel;
    }
    
    protected void reportError(String message) {
        this.errorReporter.reportError(message);
    }
    
    private ListHandler<TracTracRaceRecordDTO> getRaceTableColumnSortHandler(List<TracTracRaceRecordDTO> raceRecords,
            Column<TracTracRaceRecordDTO, ?> nameColumn, Column<TracTracRaceRecordDTO, ?> boatClassColumn,
            Column<TracTracRaceRecordDTO, ?> trackingStartColumn) {
        ListHandler<TracTracRaceRecordDTO> result = new ListHandler<TracTracRaceRecordDTO>(raceRecords);
        result.setComparator(nameColumn, new Comparator<TracTracRaceRecordDTO>() {
            @Override
            public int compare(TracTracRaceRecordDTO o1, TracTracRaceRecordDTO o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        result.setComparator(boatClassColumn, new Comparator<TracTracRaceRecordDTO>() {
            @Override
            public int compare(TracTracRaceRecordDTO o1, TracTracRaceRecordDTO o2) {
                return getBoatClassNamesAsString(o1).compareTo(getBoatClassNamesAsString(o2));
            }
        });
        result.setComparator(trackingStartColumn, new Comparator<TracTracRaceRecordDTO>() {
            @Override
            public int compare(TracTracRaceRecordDTO o1, TracTracRaceRecordDTO o2) {
                return o1.trackingStartTime == null ? -1 : o2.trackingStartTime == null ? 1 : o1.trackingStartTime
                        .compareTo(o2.trackingStartTime);
            }
        });
        return result;
    }
    
    private String getBoatClassNamesAsString(TracTracRaceRecordDTO object) {
        StringBuilder boatClassNames = new StringBuilder();
        
        for (String boatClassName : object.boatClassNames) {
            boatClassNames.append(boatClassName);
            boatClassNames.append(", ");
        }
        
        return boatClassNames.substring(0, boatClassNames.length() - 2);
    }

    private void synchURIs() {
        String hostName = this.hostnameTextBox.getValue();
        String regattaName = this.eventNameTextBox.getValue();
        Integer liveDataPort = this.liveDataPortIntegerBox.getValue();
        Integer storedDataPort = this.storedDataPortIntegerBox.getValue();
        
        this.liveURITextBox.setValue("tcp://" + hostName + (liveDataPort != null ? ":" + liveDataPort : ""));
        this.storedURITextBox.setValue("tcp://" + hostName + (storedDataPort != null ? ":" + storedDataPort : ""));
        this.jsonURLTextBox.setValue("http://" + hostName + "/events/" + regattaName + "/jsonservice.php");
    }

    private void updateStoredDataPort() {
        Integer liveDataPort = this.liveDataPortIntegerBox.getValue();

        if(liveDataPort != null)
            this.storedDataPortIntegerBox.setValue(Integer.valueOf(liveDataPort.intValue() + 1));
    }
    
    private void fillConfigurations() {
        this.sailingService.getPreviousTracTracConfigurations(new MarkedAsyncCallback<List<TracTracConfigurationDTO>>() {
            @Override
            public void handleFailure(Throwable caught) {
                reportError("Remote Procedure Call getPreviousConfigurations() - Failure: " + caught.getMessage());
            }

            @Override
            public void handleSuccess(List<TracTracConfigurationDTO> result) {
                TracTracEventManagementPanel.this.previousConfigurations.clear();
                TracTracEventManagementPanel.this.connectionsHistoryListBox.clear();
                
                for (TracTracConfigurationDTO config : result) {
                    TracTracEventManagementPanel.this.previousConfigurations.put(config.name, config);
                    TracTracEventManagementPanel.this.connectionsHistoryListBox.addItem(config.name);
                }
                
                
                if (!result.isEmpty()) {
                    updatePanelFromSelectedStoredConfiguration();
                }
            }
        });
    }

    private void fillRaces(final SailingServiceAsync sailingService) {
        final String jsonURL = this.jsonURLTextBox.getValue();
        final String liveDataURI = this.liveURITextBox.getValue();
        final String storedDataURI = this.storedURITextBox.getValue();

        sailingService.listTracTracRacesInEvent(jsonURL, new MarkedAsyncCallback<Pair<String, List<TracTracRaceRecordDTO>>>() {
            @Override
            public void handleFailure(Throwable caught) {
                reportError("Error trying to list races: " + caught.getMessage());
            }

            @Override
            public void handleSuccess(final Pair<String, List<TracTracRaceRecordDTO>> result) {
                TracTracEventManagementPanel.this.availableTracTracRaces.clear();
                
                final String eventName = result.getA();
                final List<TracTracRaceRecordDTO> eventRaces = result.getB();
                
                if (eventRaces != null) {
                    TracTracEventManagementPanel.this.availableTracTracRaces.addAll(eventRaces);
                }
                
                List<TracTracRaceRecordDTO> races = TracTracEventManagementPanel.this.raceList.getList();
                
                races.clear();
                races.addAll(TracTracEventManagementPanel.this.availableTracTracRaces);
                
                TracTracEventManagementPanel.this.racesFilterTextBox.setText("");
                TracTracEventManagementPanel.this.racesTable.setPageSize(races.size());
                
                // store a successful configuration in the database for later retrieval
                sailingService.storeTracTracConfiguration(eventName, jsonURL, liveDataURI, storedDataURI,
                        new MarkedAsyncCallback<Void>() {
                            @Override
                            public void handleFailure(Throwable caught) {
                                reportError("Exception trying to store configuration in DB: "  + caught.getMessage());
                            }

                            @Override
                            public void handleSuccess(Void voidResult) {
                                // refresh list of previous configurations
                                TracTracConfigurationDTO config = new TracTracConfigurationDTO(eventName, jsonURL,
                                        liveDataURI, storedDataURI);
                                
                                if (TracTracEventManagementPanel.this.previousConfigurations.put(config.name, config) == null) {
                                    TracTracEventManagementPanel.this.connectionsHistoryListBox.addItem(config.name);
                                }
                            }
                        });
            }
        });
    }

    private boolean checkBoatClassMatch(TracTracRaceRecordDTO tracTracRecord, RegattaDTO selectedRegatta) {
        Iterable<String> boatClassNames = tracTracRecord.boatClassNames;
        if (boatClassNames != null && Util.size(boatClassNames) > 0) {
            String tracTracBoatClass = boatClassNames.iterator().next();
            if (selectedRegatta == null) {
                // in case no regatta has been selected we check if there would be a matching regatta
                for (RegattaDTO regatta : getAvailableRegattas()) {
                    if (tracTracBoatClass.equalsIgnoreCase(regatta.boatClass.name)) {
                        return false;
                    }
                }
            } else {
                if (!tracTracBoatClass.equalsIgnoreCase(selectedRegatta.boatClass.name)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private void trackSelectedRaces(boolean trackWind, boolean correctWind, final boolean simulateWithStartTimeNow) {
        String liveURI = this.liveURITextBox.getValue();
        String storedURI = this.storedURITextBox.getValue();
        RegattaDTO selectedRegatta = getSelectedRegatta();
        RegattaIdentifier regattaIdentifier = null;
        
        if (selectedRegatta != null) {
            regattaIdentifier = new RegattaName(selectedRegatta.name);
        }
        
        // Check if the assigned regatta makes sense
        List<TracTracRaceRecordDTO> allRaces = this.raceList.getList();
        List<TracTracRaceRecordDTO> racesWithNotMatchingBoatClasses = new ArrayList<TracTracRaceRecordDTO>();
        SelectionModel<? super TracTracRaceRecordDTO> selectionModel = this.racesTable.getSelectionModel();
        
        for (TracTracRaceRecordDTO race : allRaces) {
            if (selectionModel.isSelected(race)) {
                if(!checkBoatClassMatch(race, selectedRegatta))
                    racesWithNotMatchingBoatClasses.add(race);
            }
        }

        if(racesWithNotMatchingBoatClasses.size() > 0) {
            StringBuilder builder = new StringBuilder(100 + racesWithNotMatchingBoatClasses.size() * 30);
            
            builder.append("WARNING\n");
            
            if(selectedRegatta != null) {
                builder.append(this.stringMessages.boatClassDoesNotMatchSelectedRegatta(selectedRegatta.boatClass.name,
                        selectedRegatta.name));
            } else {
                builder.append(this.stringMessages.regattaExistForSelectedBoatClass());
            }
            
            builder.append("\n\n");
            builder.append(this.stringMessages.races());
            builder.append("\n");
            
            for(TracTracRaceRecordDTO record: racesWithNotMatchingBoatClasses) {
                builder.append(record.name);
                builder.append("\n");
            }
            
            if(!Window.confirm(builder.toString())) {
                return;
            }
        }
        
        for (final TracTracRaceRecordDTO race : this.raceList.getList()) {
            if (selectionModel.isSelected(race)) {
                this.sailingService.trackWithTracTrac(regattaIdentifier, race, liveURI, storedURI, trackWind, 
                        correctWind, simulateWithStartTimeNow, new MarkedAsyncCallback<Void>() {
                    @Override
                    public void handleFailure(Throwable caught) {
                        reportError("Error trying to register race " + race.name + " for tracking: "
                                + caught.getMessage() + ". Check live/stored URI syntax.");
                    }

                    @Override
                    public void handleSuccess(Void result) {
                        TracTracEventManagementPanel.this.regattaRefresher.fillRegattas();
                    }
                });
            }
        }
    }

    private void updatePanelFromSelectedStoredConfiguration() {
        int index = this.connectionsHistoryListBox.getSelectedIndex();

        if (index == -1)
            return;
        
        String configurationKey = this.connectionsHistoryListBox.getItemText(index);
        TracTracConfigurationDTO config = this.previousConfigurations.get(configurationKey);

        this.hostnameTextBox.setValue("");
        this.eventNameTextBox.setValue("");
        this.liveDataPortIntegerBox.setText("");
        this.storedDataPortIntegerBox.setText("");
        this.jsonURLTextBox.setValue(config.jsonURL);
        this.liveURITextBox.setValue(config.liveDataURI);
        this.storedURITextBox.setValue(config.storedDataURI);
    }

    private void fillRaceListFromAvailableRacesApplyingFilter(String text) {
        List<String> wordsToFilter = Arrays.asList(text.split(" "));
        List<TracTracRaceRecordDTO> races = this.raceList.getList();
        
        races.clear();

        for (TracTracRaceRecordDTO race : this.availableTracTracRaces) {
            if (textContainsStringsToCheck(wordsToFilter, race.regattaName, race.name)) {
                races.add(race);
            }
        }

        // now sort again according to selected criterion
        ColumnSortEvent.fire(this.racesTable, this.racesTable.getColumnSortList());
    }
}

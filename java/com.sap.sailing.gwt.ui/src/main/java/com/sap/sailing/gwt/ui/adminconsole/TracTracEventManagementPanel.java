package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.Handler;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
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
public class TracTracEventManagementPanel extends AbstractEventManagementPanel {
    private final ErrorReporter errorReporter;
    private final IntegerBox storedPortIntegerbox;
    private final TextBox jsonURLBox;
    private final TextBox liveURIBox;
    private final TextBox storedURIBox;
    private final IntegerBox livePortIntegerbox;
    private final TextBox hostnameTextbox;
    private final TextBox regattaNameTextbox;
    private final TextBox filterEventsTextbox;
    private final ListDataProvider<TracTracRaceRecordDTO> raceList;
    private final CellTable<TracTracRaceRecordDTO> raceTable;
    private final Map<String, TracTracConfigurationDTO> previousConfigurations;
    private final ListBox previousConfigurationsComboBox;
    private final Grid grid;
    private final List<TracTracRaceRecordDTO> availableTracTracRaces;
    
    public TracTracEventManagementPanel(final SailingServiceAsync sailingService, ErrorReporter errorReporter,
            RegattaRefresher regattaRefresher, StringMessages stringConstants) {
        super(sailingService, regattaRefresher, errorReporter, new RaceSelectionModel(), stringConstants);
        this.errorReporter = errorReporter;
        availableTracTracRaces = new ArrayList<TracTracRaceRecordDTO>();

        VerticalPanel mainPanel = new VerticalPanel();
        this.setWidget(mainPanel);
        mainPanel.setWidth("100%");
        
        CaptionPanel captionPanelConnections = new CaptionPanel(stringConstants.connections());
        mainPanel.add(captionPanelConnections);

        VerticalPanel verticalPanel = new VerticalPanel();
        
        captionPanelConnections.setContentWidget(verticalPanel);
        captionPanelConnections.setStyleName("bold");
        
        verticalPanel.setWidth("100%");
        
        grid = new Grid(9, 2);
        verticalPanel.add(grid);
        verticalPanel.setCellWidth(grid, "100%");
        
        Label lblPredefined = new Label(stringConstants.historyOfConnections());
        grid.setWidget(0, 0, lblPredefined);
        
        previousConfigurations = new HashMap<String, TracTracConfigurationDTO>();
        previousConfigurationsComboBox = new ListBox();
        grid.setWidget(0, 1, previousConfigurationsComboBox);
        previousConfigurationsComboBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                updatePanelFromSelectedStoredConfiguration();
            }
        });
        previousConfigurationsComboBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updatePanelFromSelectedStoredConfiguration();
            }
        });
        fillConfigurations();
        
        Button btnListRaces = new Button(stringConstants.listRaces());
        grid.setWidget(8, 1, btnListRaces);
        btnListRaces.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fillRaces(sailingService);
            }
        });
        
        Label lblTrackNewEvent = new Label(stringConstants.defineNewConnection());
        grid.setWidget(2, 0, lblTrackNewEvent);
        
        Label lblHostname = new Label(stringConstants.hostname() + ":");
        grid.setWidget(3, 0, lblHostname);
        
        hostnameTextbox = new TextBox();
        hostnameTextbox.setText("germanmaster.traclive.dk");
        hostnameTextbox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                updateLiveURI();
                updateStoredURI();
                updateJsonUrl();
            }
        });
        grid.setWidget(3, 1, hostnameTextbox);
        
        Label lblEventName = new Label(stringConstants.regattaName() + ":");
        grid.setWidget(4, 0, lblEventName);
        
        regattaNameTextbox = new TextBox();
        regattaNameTextbox.setText("event_2011...");
        regattaNameTextbox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                updateJsonUrl();
            }
        });
        grid.setWidget(4, 1, regattaNameTextbox);
        
        Label lblLivePort = new Label(stringConstants.ports() + ":");
        grid.setWidget(5, 0, lblLivePort);
        
        HorizontalPanel horizontalPanel_1 = new HorizontalPanel();
        horizontalPanel_1.setSpacing(5);
        grid.setWidget(5, 1, horizontalPanel_1);

        Label lblLiveDataPort = new Label(stringConstants.liveData() + ":");
        horizontalPanel_1.add(lblLiveDataPort);
        horizontalPanel_1.setCellVerticalAlignment(lblLiveDataPort, HasVerticalAlignment.ALIGN_MIDDLE);

        livePortIntegerbox = new IntegerBox();
        livePortIntegerbox.setText(Integer.toString(4400));
        livePortIntegerbox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                updatePortStoredData();
                updateStoredURI();
                updateLiveURI();
                updateJsonUrl();
            }
        });
        horizontalPanel_1.add(livePortIntegerbox);
        
        Label lblStoredPort = new Label(stringConstants.storedData() + ":");
        horizontalPanel_1.add(lblStoredPort);
        horizontalPanel_1.setCellVerticalAlignment(lblStoredPort, HasVerticalAlignment.ALIGN_MIDDLE);
        
        storedPortIntegerbox = new IntegerBox();
        storedPortIntegerbox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                updateStoredURI();
            }
        });
        horizontalPanel_1.add(storedPortIntegerbox);
        
        Label lblJsonUrl = new Label(stringConstants.jsonUrl() + ":");
        grid.setWidget(7, 0, lblJsonUrl);
        
        jsonURLBox = new TextBox();
        grid.setWidget(7, 1, jsonURLBox);
        jsonURLBox.setVisibleLength(100);
        
        Label lblUri = new Label(stringConstants.uris() + ":");
        lblUri.setTitle(stringConstants.leaveEmptyForDefault());
        grid.setWidget(6, 0, lblUri);
        
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.setSpacing(5);
        grid.setWidget(6, 1, horizontalPanel);

        Label lblLiveUri = new Label(stringConstants.liveUri() + ":");
        lblLiveUri.setTitle(stringConstants.leaveEmptyForDefault());
        horizontalPanel.add(lblLiveUri);
        horizontalPanel.setCellVerticalAlignment(lblLiveUri, HasVerticalAlignment.ALIGN_MIDDLE);

        liveURIBox = new TextBox();
        liveURIBox.setVisibleLength(40);
        liveURIBox.setTitle(stringConstants.leaveEmptyForDefault());
        horizontalPanel.add(liveURIBox);
        
        Label lblStoredUri = new Label(stringConstants.storedUri() + ":");
        lblStoredUri.setTitle(stringConstants.leaveEmptyForDefault());
        horizontalPanel.add(lblStoredUri);
        horizontalPanel.setCellVerticalAlignment(lblStoredUri, HasVerticalAlignment.ALIGN_MIDDLE);
        
        storedURIBox = new TextBox();
        storedURIBox.setVisibleLength(40);
        storedURIBox.setTitle(stringConstants.leaveEmptyForDefault());
        horizontalPanel.add(storedURIBox);
        
        TextColumn<TracTracRaceRecordDTO> regattaNameColumn = new TextColumn<TracTracRaceRecordDTO>() {
            @Override
            public String getValue(TracTracRaceRecordDTO object) {
                return object.regattaName;
            }
        };
        TextColumn<TracTracRaceRecordDTO> raceNameColumn = new TextColumn<TracTracRaceRecordDTO>() {
            @Override
            public String getValue(TracTracRaceRecordDTO object) {
                return object.name;
            }
        };
        TextColumn<TracTracRaceRecordDTO> raceStartTrackingColumn = new TextColumn<TracTracRaceRecordDTO>() {
            @Override
            public String getValue(TracTracRaceRecordDTO object) {
                return object.trackingStartTime==null?"":dateFormatter.render(object.trackingStartTime) + " " + timeFormatter.render(object.trackingStartTime);
            }
        };

        
        HorizontalPanel racesSplitPanel = new HorizontalPanel();
        mainPanel.add(racesSplitPanel);
        
        CaptionPanel racesCaptionPanel = new CaptionPanel(stringConstants.trackableRaces());
        racesSplitPanel.add(racesCaptionPanel);
        racesCaptionPanel.setWidth("50%");

        CaptionPanel trackedRacesCaptionPanel = new CaptionPanel(stringConstants.trackedRaces());
        racesSplitPanel.add(trackedRacesCaptionPanel);
        trackedRacesCaptionPanel.setWidth("50%");

        VerticalPanel racesPanel = new VerticalPanel();
        racesCaptionPanel.setContentWidget(racesPanel);
        racesCaptionPanel.setStyleName("bold");

        VerticalPanel trackedRacesPanel = new VerticalPanel();
        trackedRacesPanel.setWidth("100%");
        trackedRacesCaptionPanel.setContentWidget(trackedRacesPanel);
        trackedRacesCaptionPanel.setStyleName("bold");

        // text box for filtering the cell table
        HorizontalPanel filterPanel = new HorizontalPanel();
        filterPanel.setSpacing(5);
        racesPanel.add(filterPanel);
        
        Label lblFilterEvents = new Label(stringConstants.filterRacesByName()+ ":");
        filterPanel.add(lblFilterEvents);
        filterPanel.setCellVerticalAlignment(lblFilterEvents, HasVerticalAlignment.ALIGN_MIDDLE);
        
        filterEventsTextbox = new TextBox();
        filterEventsTextbox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                fillRaceListFromAvailableRacesApplyingFilter(TracTracEventManagementPanel.this.filterEventsTextbox.getText());
            }
        });
        
        filterPanel.add(filterEventsTextbox);
        
        HorizontalPanel racesHorizontalPanel = new HorizontalPanel();
        racesPanel.add(racesHorizontalPanel);

        VerticalPanel trackPanel = new VerticalPanel();
        trackPanel.setStyleName("paddedPanel");
        
        raceNameColumn.setSortable(true);
        raceStartTrackingColumn.setSortable(true);
        
        AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
        raceTable = new CellTable<TracTracRaceRecordDTO>(/* pageSize */ 200, tableRes);
        raceTable.addColumn(regattaNameColumn, stringConstants.event());
        raceTable.addColumn(raceNameColumn, stringConstants.race());
        raceTable.addColumn(raceStartTrackingColumn, stringConstants.startTime());
        raceTable.setWidth("300px");
        raceTable.setSelectionModel(new MultiSelectionModel<TracTracRaceRecordDTO>() {});

        racesHorizontalPanel.add(raceTable);
        racesHorizontalPanel.add(trackPanel);

        raceList = new ListDataProvider<TracTracRaceRecordDTO>();
        raceList.addDataDisplay(raceTable);
        Handler columnSortHandler = getRaceTableColumnSortHandler(raceList.getList(), raceNameColumn, raceStartTrackingColumn);
        raceTable.addColumnSortHandler(columnSortHandler);

        Label lblTrackSettings = new Label(stringConstants.trackNewEvent());
        trackPanel.add(lblTrackSettings);
        
        final CheckBox trackWindCheckbox = new CheckBox(stringConstants.trackWind());
        trackWindCheckbox.setWordWrap(false);
        trackWindCheckbox.setValue(true);
        trackPanel.add(trackWindCheckbox);

        final CheckBox declinationCheckbox = new CheckBox(stringConstants.declinationCheckbox());
        declinationCheckbox.setWordWrap(false);
        declinationCheckbox.setValue(true);
        trackPanel.add(declinationCheckbox);
        
        trackedRacesPanel.add(trackedRacesListComposite);

        HorizontalPanel racesButtonPanel = new HorizontalPanel();
        racesPanel.add(racesButtonPanel);

        Button btnTrack = new Button(stringConstants.startTracking());
        racesButtonPanel.add(btnTrack);
        racesButtonPanel.setSpacing(10);
        btnTrack.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                trackSelectedRaces(trackWindCheckbox.getValue(), declinationCheckbox.getValue());
            }
        });

        updatePortStoredData();
        updateJsonUrl();
    }

    private ListHandler<TracTracRaceRecordDTO> getRaceTableColumnSortHandler(List<TracTracRaceRecordDTO> raceRecords,
            Column<TracTracRaceRecordDTO, ?> nameColumn, Column<TracTracRaceRecordDTO, ?> trackingStartColumn) {
        ListHandler<TracTracRaceRecordDTO> result = new ListHandler<TracTracRaceRecordDTO>(raceRecords);
        result.setComparator(nameColumn, new Comparator<TracTracRaceRecordDTO>() {
            @Override
            public int compare(TracTracRaceRecordDTO o1, TracTracRaceRecordDTO o2) {
                return o1.name.compareTo(o2.name);
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

    private void updatePortStoredData() {
        storedPortIntegerbox.setValue(livePortIntegerbox.getValue() + 1);
    }

    private void updateLiveURI() {
        liveURIBox.setValue("tcp://" + hostnameTextbox.getValue() + ":" + livePortIntegerbox.getValue());
    }

    private void updateStoredURI() {
        storedURIBox.setValue("tcp://" + hostnameTextbox.getValue() + ":" + storedPortIntegerbox.getValue());
    }

    private void updateJsonUrl() {
        jsonURLBox.setValue("http://" + hostnameTextbox.getValue() + "/events/" + regattaNameTextbox.getValue()
                + "/jsonservice.php");
    }

    private void fillConfigurations() {
        sailingService.getPreviousTracTracConfigurations(new AsyncCallback<List<TracTracConfigurationDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Remote Procedure Call getPreviousConfigurations() - Failure: "
                        + caught.getMessage());
            }

            @Override
            public void onSuccess(List<TracTracConfigurationDTO> result) {
                while (previousConfigurationsComboBox.getItemCount() > 0) {
                    previousConfigurationsComboBox.removeItem(0);
                }
                for (TracTracConfigurationDTO ttConfig : result) {
                    previousConfigurations.put(ttConfig.name, ttConfig);
                    previousConfigurationsComboBox.addItem(ttConfig.name);
                }
                if (!result.isEmpty()) {
                    updatePanelFromSelectedStoredConfiguration();
                }
            }
        });
    }

    private void fillRaces(final SailingServiceAsync sailingService) {
        final String jsonURL = jsonURLBox.getValue();
        final String liveDataURI = liveURIBox.getValue();
        final String storedDataURI = storedURIBox.getValue();
        sailingService.listTracTracRacesInEvent(jsonURL, new AsyncCallback<Pair<String, List<TracTracRaceRecordDTO>>>() {
            @Override
            public void onFailure(Throwable caught) {
                TracTracEventManagementPanel.this.errorReporter.reportError("Error trying to list races: "
                        + caught.getMessage());
            }

            @Override
            public void onSuccess(final Pair<String, List<TracTracRaceRecordDTO>> result) {
                availableTracTracRaces.clear();
                if (result.getB() != null) {
                    availableTracTracRaces.addAll(result.getB());
                }
                raceList.getList().clear();
                raceList.getList().addAll(availableTracTracRaces);
                filterEventsTextbox.setText(null);
                // store a successful configuration in the database for later retrieval
                sailingService.storeTracTracConfiguration(result.getA(), jsonURL, liveDataURI, storedDataURI,
                        new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Exception trying to store configuration in DB: "
                                        + caught.getMessage());
                            }

                            @Override
                            public void onSuccess(Void voidResult) {
                                // refresh list of previous configurations
                                TracTracConfigurationDTO ttConfig = new TracTracConfigurationDTO(result.getA(),
                                        jsonURL, liveDataURI, storedDataURI);
                                if (previousConfigurations.put(ttConfig.name, ttConfig) == null) {
                                    previousConfigurationsComboBox.addItem(ttConfig.name);
                                }
                            }
                        });

            }
        });
    }

    private void trackSelectedRaces(boolean trackWind, boolean correctWindByDeclination) {
        String liveURI = liveURIBox.getValue();
        String storedURI = storedURIBox.getValue();
        RegattaDTO selectedRegatta = trackedRacesListComposite.getSelectedRegatta();
        RegattaIdentifier regattaIdentifier = null;
        if(selectedRegatta != null) {
            regattaIdentifier = new RegattaName(selectedRegatta.name);
        }
        for (final TracTracRaceRecordDTO rr : raceList.getList()) {
            if (raceTable.getSelectionModel().isSelected(rr)) {
                sailingService.trackWithTracTrac(regattaIdentifier, rr, liveURI, storedURI, trackWind, 
                        correctWindByDeclination, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error trying to register race " + rr.name + " for tracking: "
                                + caught.getMessage()+". Check live/stored URI syntax.");
                    }

                    @Override
                    public void onSuccess(Void result) {
                        regattaRefresher.fillRegattas();
                    }
                });
            }
        }
    }

    private void updatePanelFromSelectedStoredConfiguration() {
        TracTracConfigurationDTO ttConfig = previousConfigurations.get(previousConfigurationsComboBox
                .getItemText(previousConfigurationsComboBox.getSelectedIndex()));
        if (ttConfig != null) {
            hostnameTextbox.setValue("");
            regattaNameTextbox.setValue("");
            livePortIntegerbox.setText("");
            storedPortIntegerbox.setText("");
            jsonURLBox.setValue(ttConfig.jsonURL);
            liveURIBox.setValue(ttConfig.liveDataURI);
            storedURIBox.setValue(ttConfig.storedDataURI);
        }
    }

    @Override
    public void fillRegattas(List<RegattaDTO> result) {
        trackedRacesListComposite.fillRegattas(result);
    }
    
    private void fillRaceListFromAvailableRacesApplyingFilter(String text) {
        List<String> wordsToFilter = Arrays.asList(text.split(" "));
        raceList.getList().clear();
        if (text != null && !text.isEmpty()) {
            for (TracTracRaceRecordDTO triple : availableTracTracRaces) {
                boolean failed = textContainingStringsToCheck(wordsToFilter, triple.regattaName, triple.name);
                if (!failed) {
                    raceList.getList().add(triple);
                }
            }
        } else {
            raceList.getList().addAll(availableTracTracRaces);
        }
        // now sort again according to selected criterion
        ColumnSortEvent.fire(raceTable, raceTable.getColumnSortList());
    }

}

package com.sap.sailing.gwt.ui.client;

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
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.Handler;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FormPanel;
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
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RaceRecordDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;
import com.sap.sailing.gwt.ui.shared.TracTracConfigurationDAO;

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
public class EventManagementPanel extends FormPanel {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final IntegerBox storedPortIntegerbox;
    private final TextBox jsonURLBox;
    private final TextBox liveURIBox;
    private final TextBox storedURIBox;
    private final IntegerBox livePortIntegerbox;
    private final TextBox hostnameTextbox;
    private final TextBox eventNameTextbox;
    private final ListDataProvider<RaceRecordDAO> raceList;
    private final CellTable<RaceRecordDAO> raceTable;
    private final Map<String, TracTracConfigurationDAO> previousConfigurations;
    private final ListBox previousConfigurationsComboBox;
    private final Grid grid;

    public EventManagementPanel(final SailingServiceAsync sailingService, ErrorReporter errorReporter) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        StringConstants stringConstants = GWT.create(StringConstants.class);
        VerticalPanel verticalPanel = new VerticalPanel();
        this.setWidget(verticalPanel);
        verticalPanel.setSize("100%", "100%");
        
        grid = new Grid(9, 2);
        verticalPanel.add(grid);
        verticalPanel.setCellWidth(grid, "100%");
        
        Label lblPredefined = new Label(stringConstants.trackedBefore());
        grid.setWidget(0, 0, lblPredefined);
        
        previousConfigurations = new HashMap<String, TracTracConfigurationDAO>();
        previousConfigurationsComboBox = new ListBox();
        grid.setWidget(1, 0, previousConfigurationsComboBox);
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
        grid.setWidget(1, 1, btnListRaces);
        btnListRaces.setWidth("100%");
        btnListRaces.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fillRaces(sailingService);
            }
        });
        
        Label lblTrackNewEvent = new Label("Track New Event");
        grid.setWidget(2, 0, lblTrackNewEvent);
        
        Grid grid_1 = new Grid(5, 3);
        grid.setWidget(3, 0, grid_1);
        
        Label lblHostname = new Label("Hostname");
        grid_1.setWidget(0, 1, lblHostname);
        
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
        grid_1.setWidget(0, 2, hostnameTextbox);
        
        Label lblEventName = new Label("Event name");
        grid_1.setWidget(1, 1, lblEventName);
        
        eventNameTextbox = new TextBox();
        eventNameTextbox.setText("event_2011...");
        eventNameTextbox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                updateJsonUrl();
            }
        });
        grid_1.setWidget(1, 2, eventNameTextbox);
        
        Label lblLivePort = new Label("Port Live Data");
        grid_1.setWidget(2, 1, lblLivePort);
        
        HorizontalPanel horizontalPanel_1 = new HorizontalPanel();
        grid_1.setWidget(2, 2, horizontalPanel_1);
        
        livePortIntegerbox = new IntegerBox();
        livePortIntegerbox.setText("1520");
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
        
        Label lblStoredPort = new Label("Port Stored Data");
        horizontalPanel_1.add(lblStoredPort);
        
        storedPortIntegerbox = new IntegerBox();
        storedPortIntegerbox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                updateStoredURI();
            }
        });
        horizontalPanel_1.add(storedPortIntegerbox);
        
        Label lblJsonUrl = new Label("JSON URL");
        grid_1.setWidget(3, 1, lblJsonUrl);
        
        jsonURLBox = new TextBox();
        grid_1.setWidget(3, 2, jsonURLBox);
        jsonURLBox.setVisibleLength(80);
        
        Label lblLiveUri = new Label("Live URI");
        grid_1.setWidget(4, 1, lblLiveUri);
        
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        grid_1.setWidget(4, 2, horizontalPanel);
        
        liveURIBox = new TextBox();
        liveURIBox.setVisibleLength(30);
        horizontalPanel.add(liveURIBox);
        
        Label lblStoredUri = new Label("Stored URI");
        horizontalPanel.add(lblStoredUri);
        horizontalPanel.setCellVerticalAlignment(lblStoredUri, HasVerticalAlignment.ALIGN_MIDDLE);
        
        storedURIBox = new TextBox();
        storedURIBox.setVisibleLength(30);
        horizontalPanel.add(storedURIBox);
        
        Label lblTrackableRaces = new Label("Trackable Races");
        grid.setWidget(5, 0, lblTrackableRaces);
        TextColumn<RaceRecordDAO> raceNameColumn = new TextColumn<RaceRecordDAO>() {
            @Override
            public String getValue(RaceRecordDAO object) {
                return object.name;
            }
        };
        TextColumn<RaceRecordDAO> raceStartTrackingColumn = new TextColumn<RaceRecordDAO>() {
            @Override
            public String getValue(RaceRecordDAO object) {
                return object.trackingStartTime.toString();
            }
        };
        raceNameColumn.setSortable(true);
        raceStartTrackingColumn.setSortable(true);
        raceTable = new CellTable<RaceRecordDAO>(/* pageSize */ 100);
        raceTable.addColumn(raceNameColumn, "Name");
        raceTable.addColumn(raceStartTrackingColumn, "Tracking Started");
        grid.setWidget(6, 0, raceTable);
        grid.getCellFormatter().setHeight(6, 0, "100%");
        raceTable.setWidth("100%");
        raceTable.setSelectionModel(new MultiSelectionModel<RaceRecordDAO>() {});
        raceList = new ListDataProvider<RaceRecordDAO>();
        raceList.addDataDisplay(raceTable);
        Handler columnSortHandler = getRaceTableColumnSortHandler(raceList.getList(), raceNameColumn, raceStartTrackingColumn);
        raceTable.addColumnSortHandler(columnSortHandler);
        
        VerticalPanel trackPanel = new VerticalPanel();
        grid.setWidget(6, 1, trackPanel);
        final CheckBox trackWindCheckbox = new CheckBox("Track Wind");
        trackWindCheckbox.setValue(true);
        trackPanel.add(trackWindCheckbox);
        final CheckBox declinationCheckbox = new CheckBox("Correct Wind Bearing by Declination");
        declinationCheckbox.setValue(true);
        trackPanel.add(declinationCheckbox);
        
        Button btnTrack = new Button("Track");
        trackPanel.add(btnTrack);
        btnTrack.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                trackSelectedRaces(trackWindCheckbox.getValue(), declinationCheckbox.getValue());
            }
        });
        grid.getCellFormatter().setVerticalAlignment(6, 1, HasVerticalAlignment.ALIGN_TOP);
        grid.getCellFormatter().setVerticalAlignment(6, 0, HasVerticalAlignment.ALIGN_TOP);

        Label lblEventsConnectedTo = new Label("Events Currently Tracked");
        grid.setWidget(7, 0, lblEventsConnectedTo);

        grid.getCellFormatter().setVerticalAlignment(8, 1, HasVerticalAlignment.ALIGN_TOP);
        grid.getCellFormatter().setVerticalAlignment(8, 0, HasVerticalAlignment.ALIGN_TOP);
        
        fillEvents();
        updatePortStoredData();
        updateLiveURI();
        updateStoredURI();
        updateJsonUrl();
    }

    private void stopTrackingEvent(final EventDAO event) {
        sailingService.stopTrackingEvent(event.name, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Exception trying to stop tracking event " + event.name + ": "
                        + caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                fillEvents();
            }
        });
    }

    private void stopTrackingRace(final EventDAO event, final RaceDAO race) {
        sailingService.stopTrackingRace(event.name, race.name, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Exception trying to stop tracking race " + race.name + "in event "+event.name+": "
                        + caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                fillEvents();
            }
        });
    }

    private ListHandler<RaceRecordDAO> getRaceTableColumnSortHandler(List<RaceRecordDAO> raceRecords,
            Column<RaceRecordDAO, ?> nameColumn, Column<RaceRecordDAO, ?> trackingStartColumn) {
        ListHandler<RaceRecordDAO> result = new ListHandler<RaceRecordDAO>(raceRecords);
        result.setComparator(nameColumn, new Comparator<RaceRecordDAO>() {
            @Override
            public int compare(RaceRecordDAO o1, RaceRecordDAO o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        result.setComparator(trackingStartColumn, new Comparator<RaceRecordDAO>() {
            @Override
            public int compare(RaceRecordDAO o1, RaceRecordDAO o2) {
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
        jsonURLBox.setValue("http://" + hostnameTextbox.getValue() + "/events/" + eventNameTextbox.getValue()
                + "/jsonservice.php");
    }

    private void fillConfigurations() {
        sailingService.getPreviousConfigurations(new AsyncCallback<List<TracTracConfigurationDAO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Remote Procedure Call getPreviousConfigurations() - Failure: "
                        + caught.getMessage());
            }

            @Override
            public void onSuccess(List<TracTracConfigurationDAO> result) {
                while (previousConfigurationsComboBox.getItemCount() > 0) {
                    previousConfigurationsComboBox.removeItem(0);
                }
                for (TracTracConfigurationDAO ttConfig : result) {
                    previousConfigurations.put(ttConfig.name, ttConfig);
                    previousConfigurationsComboBox.addItem(ttConfig.name);
                }
                if (!result.isEmpty()) {
                    updatePanelFromSelectedStoredConfiguration();
                }
            }
        });
    }

    private void fillEvents() {
        sailingService.listEvents(new AsyncCallback<List<EventDAO>>() {
            @Override
            public void onSuccess(List<EventDAO> result) {
                grid.setWidget(8, 0, null);
                VerticalPanel buttonPanel = new VerticalPanel();
                Button btnRefresh = new Button("Refresh");
                btnRefresh.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        fillEvents();
                    }
                });
                buttonPanel.add(btnRefresh);
                grid.setWidget(8, 1, buttonPanel);
                if (!result.isEmpty()) {
                    final ListDataProvider<EventDAO> eventsList = new ListDataProvider<EventDAO>(result);
                    final TrackedEventsTreeModel trackedEventsModel = new TrackedEventsTreeModel(eventsList);
                    // When the following line is uncommented, the race table contents don't show anymore...???!!!
                    CellTree eventsCellTree = new CellTree(trackedEventsModel, /* root */null);
                    grid.setWidget(8, 0, eventsCellTree);

                    Button btnRemove = new Button("Remove");
                    btnRemove.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent click) {
                            for (EventDAO event : eventsList.getList()) {
                                if (trackedEventsModel.getSelectionModel().isSelected(event)) {
                                    stopTrackingEvent(event);
                                } else {
                                    // scan event's races:
                                    for (RegattaDAO regatta : event.regattas) {
                                        for (RaceDAO race : regatta.races) {
                                            if (trackedEventsModel.getSelectionModel().isSelected(regatta) ||
                                                    trackedEventsModel.getSelectionModel().isSelected(race)) {
                                                stopTrackingRace(event, race);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    });
                    btnRemove.setWidth("100%");
                    buttonPanel.add(btnRemove);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Remote Procedure Call listEvents() - Failure");
            }
        });
    }

    private void fillRaces(final SailingServiceAsync sailingService) {
        final String jsonURL = jsonURLBox.getValue();
        final String liveDataURI = liveURIBox.getValue();
        final String storedDataURI = storedURIBox.getValue();
        sailingService.listRacesInEvent(jsonURL, new AsyncCallback<List<RaceRecordDAO>>() {
            @Override
            public void onFailure(Throwable caught) {
                EventManagementPanel.this.errorReporter.reportError("Error trying to list races: "
                        + caught.getMessage());
            }

            @Override
            public void onSuccess(final List<RaceRecordDAO> result) {
                raceList.getList().clear();
                raceList.getList().addAll(result);
                if (!result.isEmpty()) {
                    // store a successful configuration in the database for later retrieval
                    sailingService.storeTracTracConfiguration(result.iterator().next().eventName, jsonURL, liveDataURI,
                            storedDataURI, new AsyncCallback<Void>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    errorReporter.reportError("Exception trying to store configuration in DB: "
                                            + caught.getMessage());
                                }

                                @Override
                                public void onSuccess(Void voidResult) {
                                    // refresh list of previous configurations
                                    TracTracConfigurationDAO ttConfig = new TracTracConfigurationDAO(result.iterator().next().eventName,
                                            jsonURL, liveDataURI, storedDataURI);
                                    if (previousConfigurations.put(ttConfig.name, ttConfig) == null) {
                                        previousConfigurationsComboBox.addItem(ttConfig.name);
                                    }
                                }
                            });
                }
            }
        });
    }

    private void trackSelectedRaces(boolean trackWind, boolean correctWindByDeclination) {
        String liveURI = liveURIBox.getValue();
        String storedURI = storedURIBox.getValue();
        for (final RaceRecordDAO rr : raceList.getList()) {
            if (raceTable.getSelectionModel().isSelected(rr)) {
                sailingService.track(rr, liveURI, storedURI, trackWind, correctWindByDeclination, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error trying to register race " + rr.name + " for tracking: "
                                + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(Void result) {
                        fillEvents();
                    }
                });
            }
        }
    }

    private void updatePanelFromSelectedStoredConfiguration() {
        TracTracConfigurationDAO ttConfig = previousConfigurations.get(previousConfigurationsComboBox
                .getItemText(previousConfigurationsComboBox.getSelectedIndex()));
        if (ttConfig != null) {
            hostnameTextbox.setValue("");
            eventNameTextbox.setValue("");
            livePortIntegerbox.setText("");
            storedPortIntegerbox.setText("");
            jsonURLBox.setValue(ttConfig.jsonURL);
            liveURIBox.setValue(ttConfig.liveDataURI);
            storedURIBox.setValue(ttConfig.storedDataURI);
        }
    }

}

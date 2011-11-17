package com.sap.sailing.gwt.ui.client;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
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
import com.sap.sailing.gwt.ui.shared.RegattaDAO;
import com.sap.sailing.gwt.ui.shared.SwissTimingConfigurationDAO;
import com.sap.sailing.gwt.ui.shared.SwissTimingRaceRecordDAO;
import com.sap.sailing.gwt.ui.shared.Triple;

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
public class SwissTimingEventManagementPanel extends FormPanel implements EventDisplayer {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final IntegerBox portIntegerbox;
    private final TextBox hostnameTextbox;
    private final ListDataProvider<SwissTimingRaceRecordDAO> raceList;
    private final CellTable<SwissTimingRaceRecordDAO> raceTable;
    private final Map<String, SwissTimingConfigurationDAO> previousConfigurations;
    private final ListBox previousConfigurationsComboBox;
    private final Grid grid;
    private final RaceTreeView trackedRacesTreeView;
    private final EventRefresher eventRefresher;

    public SwissTimingEventManagementPanel(final SailingServiceAsync sailingService, ErrorReporter errorReporter,
            EventRefresher eventRefresher, StringConstants stringConstants) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.eventRefresher = eventRefresher;
        VerticalPanel verticalPanel = new VerticalPanel();
        this.setWidget(verticalPanel);
        verticalPanel.setSize("100%", "100%");
        
        grid = new Grid(9, 2);
        verticalPanel.add(grid);
        verticalPanel.setCellWidth(grid, "100%");
        
        Label lblPredefined = new Label(stringConstants.trackedBefore());
        grid.setWidget(0, 0, lblPredefined);
        
        previousConfigurations = new HashMap<String, SwissTimingConfigurationDAO>();
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
        
        Label lblTrackNewEvent = new Label(stringConstants.trackNewEvent());
        grid.setWidget(2, 0, lblTrackNewEvent);
        
        Grid grid_1 = new Grid(5, 3);
        grid.setWidget(3, 0, grid_1);
        
        Label lblHostname = new Label(stringConstants.hostname());
        grid_1.setWidget(0, 1, lblHostname);
        
        hostnameTextbox = new TextBox();
        hostnameTextbox.setText("gps.sportresult.com");
        grid_1.setWidget(0, 2, hostnameTextbox);
        
        HorizontalPanel horizontalPanel_1 = new HorizontalPanel();
        grid_1.setWidget(2, 2, horizontalPanel_1);
        
        Label lblPort = new Label(stringConstants.port());
        horizontalPanel_1.add(lblPort);
        
        portIntegerbox = new IntegerBox();
        horizontalPanel_1.add(portIntegerbox);
        
        Label lblTrackableRaces = new Label(stringConstants.trackableRaces());
        grid.setWidget(5, 0, lblTrackableRaces);
        TextColumn<SwissTimingRaceRecordDAO> raceNameColumn = new TextColumn<SwissTimingRaceRecordDAO>() {
            @Override
            public String getValue(SwissTimingRaceRecordDAO object) {
                return object.ID;
            }
        };
        TextColumn<SwissTimingRaceRecordDAO> raceStartTrackingColumn = new TextColumn<SwissTimingRaceRecordDAO>() {
            @Override
            public String getValue(SwissTimingRaceRecordDAO object) {
                return object.raceStartTime.toString();
            }
        };
        raceNameColumn.setSortable(true);
        raceStartTrackingColumn.setSortable(true);
        raceTable = new CellTable<SwissTimingRaceRecordDAO>(/* pageSize */ 100);
        raceTable.addColumn(raceNameColumn, stringConstants.name());
        raceTable.addColumn(raceStartTrackingColumn, stringConstants.raceStartTimeColumn());
        grid.setWidget(6, 0, raceTable);
        grid.getCellFormatter().setHeight(6, 0, "100%");
        raceTable.setWidth("100%");
        raceTable.setSelectionModel(new MultiSelectionModel<SwissTimingRaceRecordDAO>() {});
        raceList = new ListDataProvider<SwissTimingRaceRecordDAO>();
        raceList.addDataDisplay(raceTable);
        Handler columnSortHandler = getRaceTableColumnSortHandler(raceList.getList(), raceNameColumn, raceStartTrackingColumn);
        raceTable.addColumnSortHandler(columnSortHandler);
        
        VerticalPanel trackPanel = new VerticalPanel();
        grid.setWidget(6, 1, trackPanel);
        final CheckBox trackWindCheckbox = new CheckBox(stringConstants.trackWind());
        trackWindCheckbox.setValue(true);
        trackPanel.add(trackWindCheckbox);
        final CheckBox declinationCheckbox = new CheckBox(stringConstants.declinationCheckbox());
        declinationCheckbox.setValue(true);
        trackPanel.add(declinationCheckbox);
        
        Button btnTrack = new Button(stringConstants.btnTrack());
        trackPanel.add(btnTrack);
        btnTrack.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                trackSelectedRaces(trackWindCheckbox.getValue(), declinationCheckbox.getValue());
            }
        });
        grid.getCellFormatter().setVerticalAlignment(6, 1, HasVerticalAlignment.ALIGN_TOP);
        grid.getCellFormatter().setVerticalAlignment(6, 0, HasVerticalAlignment.ALIGN_TOP);

        Label lblEventsConnectedTo = new Label(stringConstants.eventsConnectedTo());
        grid.setWidget(7, 0, lblEventsConnectedTo);
        trackedRacesTreeView = new RaceTreeView(stringConstants, /* multiselection */ true);
        grid.setWidget(8, 0, trackedRacesTreeView);
        VerticalPanel buttonPanel = new VerticalPanel();
        Button btnRefresh = new Button(stringConstants.refresh());
        btnRefresh.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                SwissTimingEventManagementPanel.this.eventRefresher.fillEvents();
            }
        });
        buttonPanel.add(btnRefresh);
        Button btnRemove = new Button(stringConstants.remove());
        btnRemove.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent click) {
                for (Triple<EventDAO, RegattaDAO, RaceDAO> selection : trackedRacesTreeView.getSelectedEventAndRace()) {
                    if (selection.getC().currentlyTracked) {
                        stopTrackingRace(selection.getA(), selection.getC());
                    }
                }
            }
        });
        btnRemove.setWidth("100%");
        buttonPanel.add(btnRemove);
        grid.setWidget(8, 1, buttonPanel);
        
        grid.getCellFormatter().setVerticalAlignment(8, 1, HasVerticalAlignment.ALIGN_TOP);
        grid.getCellFormatter().setVerticalAlignment(8, 0, HasVerticalAlignment.ALIGN_TOP);
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
                eventRefresher.fillEvents();
            }
        });
    }

    private ListHandler<SwissTimingRaceRecordDAO> getRaceTableColumnSortHandler(List<SwissTimingRaceRecordDAO> raceRecords,
            Column<SwissTimingRaceRecordDAO, ?> nameColumn, Column<SwissTimingRaceRecordDAO, ?> trackingStartColumn) {
        ListHandler<SwissTimingRaceRecordDAO> result = new ListHandler<SwissTimingRaceRecordDAO>(raceRecords);
        result.setComparator(nameColumn, new Comparator<SwissTimingRaceRecordDAO>() {
            @Override
            public int compare(SwissTimingRaceRecordDAO o1, SwissTimingRaceRecordDAO o2) {
                return o1.ID.compareTo(o2.ID);
            }
        });
        result.setComparator(trackingStartColumn, new Comparator<SwissTimingRaceRecordDAO>() {
            @Override
            public int compare(SwissTimingRaceRecordDAO o1, SwissTimingRaceRecordDAO o2) {
                return o1.raceStartTime == null ? -1 : o2.raceStartTime == null ? 1 : o1.raceStartTime
                        .compareTo(o2.raceStartTime);
            }
        });
        return result;
    }

    private void fillConfigurations() {
        sailingService.getPreviousSwissTimingConfigurations(new AsyncCallback<List<SwissTimingConfigurationDAO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Remote Procedure Call getPreviousConfigurations() - Failure: "
                        + caught.getMessage());
            }

            @Override
            public void onSuccess(List<SwissTimingConfigurationDAO> result) {
                while (previousConfigurationsComboBox.getItemCount() > 0) {
                    previousConfigurationsComboBox.removeItem(0);
                }
                for (SwissTimingConfigurationDAO stConfig : result) {
                    previousConfigurations.put(stConfig.name, stConfig);
                    previousConfigurationsComboBox.addItem(stConfig.name);
                }
                if (!result.isEmpty()) {
                    updatePanelFromSelectedStoredConfiguration();
                }
            }
        });
    }

    private void fillRaces(final SailingServiceAsync sailingService) {
        final String hostname = hostnameTextbox.getValue();
        final int port = portIntegerbox.getValue();
        sailingService.listSwissTimingRaces(hostname, port, /* TODO canSendRequests */ false,
                new AsyncCallback<List<SwissTimingRaceRecordDAO>>() {
            @Override
            public void onFailure(Throwable caught) {
                SwissTimingEventManagementPanel.this.errorReporter.reportError("Error trying to list races: "
                        + caught.getMessage());
            }

            @Override
            public void onSuccess(final List<SwissTimingRaceRecordDAO> result) {
                raceList.getList().clear();
                if (result != null) {
                    raceList.getList().addAll(result);
                }
                // store a successful configuration in the database for later retrieval
                final String configName = hostname+":"+port;
                sailingService.storeSwissTimingConfiguration(configName, hostname, port,
                        new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Exception trying to store configuration in DB: "
                                        + caught.getMessage());
                            }

                            @Override
                            public void onSuccess(Void voidResult) {
                                // refresh list of previous configurations
                                SwissTimingConfigurationDAO stConfig = new SwissTimingConfigurationDAO(configName,
                                        hostname, port);
                                if (previousConfigurations.put(stConfig.name, stConfig) == null) {
                                    previousConfigurationsComboBox.addItem(stConfig.name);
                                }
                            }
                        });
            }
        });
    }

    private void trackSelectedRaces(boolean trackWind, boolean correctWindByDeclination) {
        String hostname = hostnameTextbox.getValue();
        int port = portIntegerbox.getValue();
        for (final SwissTimingRaceRecordDAO rr : raceList.getList()) {
            if (raceTable.getSelectionModel().isSelected(rr)) {
                sailingService.trackWithSwissTiming(rr, hostname, port, /* TODO canSendRequests */false, trackWind,
                        correctWindByDeclination, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error trying to register race " + rr.ID + " for tracking: "
                                + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(Void result) {
                        eventRefresher.fillEvents();
                    }
                });
            }
        }
    }

    private void updatePanelFromSelectedStoredConfiguration() {
        if (previousConfigurationsComboBox.getSelectedIndex() >= 0) {
            SwissTimingConfigurationDAO stConfig = previousConfigurations.get(previousConfigurationsComboBox
                    .getItemText(previousConfigurationsComboBox.getSelectedIndex()));
            if (stConfig != null) {
                hostnameTextbox.setValue(stConfig.hostname);
                portIntegerbox.setValue(stConfig.port);
            }
        }
    }

    @Override
    public void fillEvents(List<EventDAO> result) {
        trackedRacesTreeView.fillEvents(result);
    }

}

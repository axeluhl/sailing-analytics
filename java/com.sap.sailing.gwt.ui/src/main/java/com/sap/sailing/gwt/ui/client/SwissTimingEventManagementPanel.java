package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
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
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.text.client.DateTimeFormatRenderer;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.Handler;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
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
import com.sap.sailing.gwt.ui.shared.SwissTimingConfigurationDAO;
import com.sap.sailing.gwt.ui.shared.SwissTimingRaceRecordDAO;

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
    private final TextBox filterEventsTextbox;
    private final ListDataProvider<SwissTimingRaceRecordDAO> raceList;
    private final CellTable<SwissTimingRaceRecordDAO> raceTable;
    private final Map<String, SwissTimingConfigurationDAO> previousConfigurations;
    private final ListBox previousConfigurationsComboBox;
    private final TrackedEventsComposite trackedEventsComposite;
    private final EventRefresher eventRefresher;
    private final CheckBox canSendRequestsCheckbox;
    private DateTimeFormatRenderer dateFormatter = new DateTimeFormatRenderer(DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT));
    private DateTimeFormatRenderer timeFormatter = new DateTimeFormatRenderer(DateTimeFormat.getFormat(PredefinedFormat.TIME_LONG));
    private final List<SwissTimingRaceRecordDAO> availableSwissTimingRaces = new ArrayList<SwissTimingRaceRecordDAO>();

    public SwissTimingEventManagementPanel(final SailingServiceAsync sailingService, ErrorReporter errorReporter,
            EventRefresher eventRefresher, StringConstants stringConstants) {
        
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.eventRefresher = eventRefresher;

        VerticalPanel mainPanel = new VerticalPanel();
        this.setWidget(mainPanel);
        mainPanel.setWidth("100%");
        
        CaptionPanel captionPanelConnections = new CaptionPanel("Connections");
        mainPanel.add(captionPanelConnections);

        VerticalPanel verticalPanel = new VerticalPanel();
        
        captionPanelConnections.setContentWidget(verticalPanel);
        captionPanelConnections.setStyleName("bold");
        Grid connectionsGrid = new Grid(7, 2);
        verticalPanel.add(connectionsGrid);
        verticalPanel.setCellWidth(connectionsGrid, "100%");
        
//        Label lblPredefined = new Label(stringConstants.trackedBefore() + ":");
        Label lblPredefined = new Label("History of connections:");
        connectionsGrid.setWidget(0, 0, lblPredefined);
        
        previousConfigurations = new HashMap<String, SwissTimingConfigurationDAO>();
        previousConfigurationsComboBox = new ListBox();
        connectionsGrid.setWidget(0, 1, previousConfigurationsComboBox);
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

//        Label lblTrackNewEvent = new Label(stringConstants.trackNewEvent());
        Label lblTrackNewEvent = new Label("Define a new connection");
        connectionsGrid.setWidget(2, 0, lblTrackNewEvent);
        
        Label lblHostname = new Label(stringConstants.hostname() + ":");
        connectionsGrid.setWidget(3, 0, lblHostname);
        
        hostnameTextbox = new TextBox();
        hostnameTextbox.setText("");
        connectionsGrid.setWidget(3, 1, hostnameTextbox);
        
        Label lblPort = new Label(stringConstants.port() + ":");
        connectionsGrid.setWidget(4, 0, lblPort);
        
        portIntegerbox = new IntegerBox();
        connectionsGrid.setWidget(4, 1, portIntegerbox);

        Label lblCanSendRequests = new Label("Can send requests:");
        connectionsGrid.setWidget(5, 0, lblCanSendRequests);

        canSendRequestsCheckbox = new CheckBox();
        canSendRequestsCheckbox.setValue(false);
        connectionsGrid.setWidget(5, 1, canSendRequestsCheckbox);

        
//        Button btnListRaces = new Button(stringConstants.listRaces());
        Button btnListRaces = new Button("Connect and read races");
        connectionsGrid.setWidget(6, 1, btnListRaces);
        btnListRaces.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fillRaces(sailingService);
            }
        });


        TextColumn<SwissTimingRaceRecordDAO> raceNameColumn = new TextColumn<SwissTimingRaceRecordDAO>() {
            @Override
            public String getValue(SwissTimingRaceRecordDAO object) {
                return object.ID;
            }
        };
        
        TextColumn<SwissTimingRaceRecordDAO> raceStartTrackingColumn = new TextColumn<SwissTimingRaceRecordDAO>() {
            @Override
            public String getValue(SwissTimingRaceRecordDAO object) {
                return object.raceStartTime==null?"":dateFormatter.render(object.raceStartTime) + " " + timeFormatter.render(object.raceStartTime);
            }
        };


        HorizontalPanel racesSplitPanel = new HorizontalPanel();
        mainPanel.add(racesSplitPanel);
        
        CaptionPanel racesCaptionPanel = new CaptionPanel(stringConstants.trackableRaces());
        racesSplitPanel.add(racesCaptionPanel);
        racesCaptionPanel.setWidth("50%");

        CaptionPanel trackedRacesCaptionPanel = new CaptionPanel("Tracked Races");
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
        
        Label lblFilterEvents = new Label("Filter races by name:");
        filterPanel.add(lblFilterEvents);
        filterPanel.setCellVerticalAlignment(lblFilterEvents, HasVerticalAlignment.ALIGN_MIDDLE);
        
        filterEventsTextbox = new TextBox();
        filterEventsTextbox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                String text = filterEventsTextbox.getText();

                raceList.getList().clear();
                
                if(text == null || text.isEmpty()) {
                    raceList.getList().addAll(availableSwissTimingRaces);
                } else {
                    String textAsUppercase = text.toUpperCase();
                    for(SwissTimingRaceRecordDAO dao: availableSwissTimingRaces) {
                        if(dao.ID != null) {
                            if(dao.ID.toUpperCase().contains(textAsUppercase))
                                raceList.getList().add(dao);
                        }
                    }
                }
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
        raceTable = new CellTable<SwissTimingRaceRecordDAO>(/* pageSize */ 200, tableRes);
        raceTable.addColumn(raceNameColumn, stringConstants.name());
        raceTable.addColumn(raceStartTrackingColumn, stringConstants.raceStartTimeColumn());
        raceTable.setWidth("300px");
        raceTable.setSelectionModel(new MultiSelectionModel<SwissTimingRaceRecordDAO>() {});

        racesHorizontalPanel.add(raceTable);
        racesHorizontalPanel.add(trackPanel);

        raceList = new ListDataProvider<SwissTimingRaceRecordDAO>();
        raceList.addDataDisplay(raceTable);
        Handler columnSortHandler = getRaceTableColumnSortHandler(raceList.getList(), raceNameColumn, raceStartTrackingColumn);
        raceTable.addColumnSortHandler(columnSortHandler);

        Label lblTrackSettings = new Label("Track settings");
        trackPanel.add(lblTrackSettings);
        
        final CheckBox trackWindCheckbox = new CheckBox(stringConstants.trackWind());
        trackWindCheckbox.setWordWrap(false);
        trackWindCheckbox.setValue(true);
        trackPanel.add(trackWindCheckbox);

        final CheckBox declinationCheckbox = new CheckBox(stringConstants.declinationCheckbox());
        declinationCheckbox.setWordWrap(false);
        declinationCheckbox.setValue(true);
        trackPanel.add(declinationCheckbox);
        
        trackedEventsComposite = new TrackedEventsComposite(sailingService, errorReporter, eventRefresher,
                    stringConstants, /* multiselection */ true);
        trackedRacesPanel.add(trackedEventsComposite);

        HorizontalPanel racesButtonPanel = new HorizontalPanel();
        racesPanel.add(racesButtonPanel);

        Button btnTrack = new Button("Start tracking");
        
//        Button btnTrack = new Button(stringConstants.btnTrack());
        racesButtonPanel.add(btnTrack);
        racesButtonPanel.setSpacing(10);
        btnTrack.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                trackSelectedRaces(trackWindCheckbox.getValue(), declinationCheckbox.getValue());
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
        final boolean canSendRequests = canSendRequestsCheckbox.getValue();
        sailingService.listSwissTimingRaces(hostname, port, canSendRequests,
                new AsyncCallback<List<SwissTimingRaceRecordDAO>>() {
            @Override
            public void onFailure(Throwable caught) {
                SwissTimingEventManagementPanel.this.errorReporter.reportError("Error trying to list races: "
                        + caught.getMessage());
            }

            @Override
            public void onSuccess(final List<SwissTimingRaceRecordDAO> result) {
                availableSwissTimingRaces.clear();
                if (result != null)
                    availableSwissTimingRaces.addAll(result);

                raceList.getList().clear();
                raceList.getList().addAll(availableSwissTimingRaces);

                filterEventsTextbox.setText(null);

                // store a successful configuration in the database for later retrieval
                final String configName = hostname+":"+port;
                sailingService.storeSwissTimingConfiguration(configName, hostname, port, canSendRequests,
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
                                        hostname, port, canSendRequests);
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
                canSendRequestsCheckbox.setValue(stConfig.canSendRequests);
            }
        }
    }

    @Override
    public void fillEvents(List<EventDAO> result) {
        trackedEventsComposite.fillEvents(result);
    }

}

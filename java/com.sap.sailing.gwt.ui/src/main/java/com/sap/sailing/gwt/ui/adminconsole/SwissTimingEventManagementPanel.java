package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.Column;
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
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingEventRecordDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingRaceRecordDTO;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.FlushableCellTable;
import com.sap.sse.gwt.client.celltable.SelectionCheckboxColumn;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;

/**
 * Allows the user to start and stop tracking of races using the SwissTiming connector. In particular,
 * previously configured connections can be retrieved from a drop-down list which then pre-populates all connection
 * parameters. The user can also choose to enter connection information manually.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class SwissTimingEventManagementPanel extends AbstractEventManagementPanel {
    private static final AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
    
    private final LabeledAbstractFilterablePanel<SwissTimingRaceRecordDTO> filterablePanelEvents;
    private final ListDataProvider<SwissTimingRaceRecordDTO> raceList;
    private final FlushableCellTable<SwissTimingRaceRecordDTO> raceTable;
    private final Map<String, SwissTimingConfigurationDTO> previousConfigurations;
    private final ListBox previousConfigurationsComboBox;
    private final TextBox eventIdBox;
    private final TextBox jsonUrlBox;
    private final TextBox updateURLBox;
    private final TextBox updateUsernameBox;
    private final PasswordTextBox updatePasswordBox;
    private final TextBox hostnameTextbox;
    private final IntegerBox portIntegerbox;
    private final List<SwissTimingRaceRecordDTO> availableSwissTimingRaces = new ArrayList<SwissTimingRaceRecordDTO>();
    private final String manage2sailBaseAPIUrl = "http://manage2sail.com/api/public/links/event/";
    private final String manage2sailAPIaccessToken = "?accesstoken=bDAv8CwsTM94ujZ";
    private final String manage2sailUrlAppendix = "&mediaType=json&includeRaces=true";
    private final String eventIdPattern = "[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}";

    public SwissTimingEventManagementPanel(final SailingServiceAsync sailingService,
            ErrorReporter errorReporter,
            RegattaRefresher regattaRefresher, StringMessages stringConstants) {
        super(sailingService, regattaRefresher, errorReporter, true, stringConstants);
        this.errorReporter = errorReporter;
        VerticalPanel mainPanel = new VerticalPanel();
        this.setWidget(mainPanel);
        mainPanel.setWidth("100%");
        CaptionPanel captionPanelConnections = new CaptionPanel(stringConstants.connections());
        mainPanel.add(captionPanelConnections);
        VerticalPanel verticalPanel = new VerticalPanel();
        captionPanelConnections.setContentWidget(verticalPanel);
        captionPanelConnections.setStyleName("bold");
        Grid connectionsGrid = new Grid(9, 2);
        verticalPanel.add(connectionsGrid);
        previousConfigurations = new HashMap<String, SwissTimingConfigurationDTO>();
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
        connectionsGrid.setWidget(0, 0, new Label(stringMessages.swissTimingEvents() + ":"));
        connectionsGrid.setWidget(0, 1, previousConfigurationsComboBox);
        
        eventIdBox = new TextBox();
        eventIdBox.getElement().getStyle().setWidth(30, Unit.EM);
        eventIdBox.setTitle(stringMessages.manage2SailEventIdBoxTooltip());
        connectionsGrid.setWidget(1, 0, new Label(stringMessages.manage2SailEventIdBox() + ":"));
        connectionsGrid.setWidget(1, 1, eventIdBox);
        eventIdBox.addChangeHandler(event -> {
            if (eventIdBox.getValue() != "") {
                updateUrlFromEventId(eventIdBox.getValue());
            }
        });

        jsonUrlBox = new TextBox();
        jsonUrlBox.getElement().getStyle().setWidth(50, Unit.EM);
        connectionsGrid.setWidget(2, 0, new Label(stringMessages.manage2SailEventURLBox() + ":"));
        connectionsGrid.setWidget(2, 1, jsonUrlBox);
        jsonUrlBox.addChangeHandler(event -> {
            if (jsonUrlBox.getValue() != "") {
                updateEventIdFromUrl(jsonUrlBox.getValue());
            }
        });

        hostnameTextbox = new TextBox();
        portIntegerbox = new IntegerBox();

        connectionsGrid.setWidget(3, 0,  new Label(stringConstants.hostname() + ":"));
        connectionsGrid.setWidget(3, 1, hostnameTextbox);
        
        connectionsGrid.setWidget(4, 0, new Label(stringMessages.manage2SailPort() + ":"));
        connectionsGrid.setWidget(4, 1, portIntegerbox);

        updateURLBox = new TextBox();
        updateURLBox.getElement().getStyle().setWidth(50, Unit.EM);
        connectionsGrid.setWidget(5, 0, new Label(stringMessages.swissTimingUpdateURL() + ":"));
        connectionsGrid.setWidget(5, 1, updateURLBox);
        
        updateUsernameBox = new TextBox();
        updateUsernameBox.getElement().getStyle().setWidth(50, Unit.EM);
        connectionsGrid.setWidget(6, 0, new Label(stringMessages.swissTimingUpdateUsername() + ":"));
        connectionsGrid.setWidget(6, 1, updateUsernameBox);

        updatePasswordBox = new PasswordTextBox();
        updatePasswordBox.getElement().getStyle().setWidth(50, Unit.EM);
        connectionsGrid.setWidget(7, 0, new Label(stringMessages.swissTimingUpdatePassword() + ":"));
        connectionsGrid.setWidget(7, 1, updatePasswordBox);

        Button btnListRaces = new Button(stringConstants.listRaces());
        connectionsGrid.setWidget(8, 1, btnListRaces);
        btnListRaces.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fillRaces(sailingService);
            }
        });
        
        HorizontalPanel racesSplitPanel = new HorizontalPanel();
        mainPanel.add(racesSplitPanel);
        
        CaptionPanel trackableRacesCaptionPanel = new CaptionPanel(stringConstants.trackableRaces());
        racesSplitPanel.add(trackableRacesCaptionPanel);
        trackableRacesCaptionPanel.setWidth("50%");

        CaptionPanel trackedRacesCaptionPanel = new CaptionPanel(stringConstants.trackedRaces());
        racesSplitPanel.add(trackedRacesCaptionPanel);
        trackedRacesCaptionPanel.setWidth("50%");

        VerticalPanel trackableRacesPanel = new VerticalPanel();
        trackableRacesCaptionPanel.setContentWidget(trackableRacesPanel);
        trackableRacesCaptionPanel.setStyleName("bold");

        VerticalPanel trackedRacesPanel = new VerticalPanel();
        trackedRacesPanel.setWidth("100%");
        trackedRacesCaptionPanel.setContentWidget(trackedRacesPanel);
        trackedRacesCaptionPanel.setStyleName("bold");

        // Regatta selection
        HorizontalPanel regattaPanel = new HorizontalPanel();
        regattaPanel.setSpacing(5);
        Label regattaForTrackingLabel = new Label(stringMessages.regattaUsedForTheTrackedRace());
        regattaForTrackingLabel.setWordWrap(false);
        regattaPanel.add(regattaForTrackingLabel);
        regattaPanel.add(getAvailableRegattasListBox());
        trackableRacesPanel.add(regattaPanel);

        Label lblTrackSettings = new Label(stringConstants.trackSettings());
        trackableRacesPanel.add(lblTrackSettings);

        final CheckBox trackWindCheckbox = new CheckBox(stringConstants.trackWind());
        trackWindCheckbox.setWordWrap(false);
        trackWindCheckbox.setValue(true);
        trackableRacesPanel.add(trackWindCheckbox);

        final CheckBox declinationCheckbox = new CheckBox(stringConstants.declinationCheckbox());
        declinationCheckbox.setWordWrap(false);
        declinationCheckbox.setValue(true);
        trackableRacesPanel.add(declinationCheckbox);
        
        final CheckBox simulateWithStartTimeNow = new CheckBox(stringMessages.simulateAsLiveRace());
        simulateWithStartTimeNow.setWordWrap(false);
        simulateWithStartTimeNow.setValue(false);
        trackableRacesPanel.add(simulateWithStartTimeNow);
        
        final CheckBox useInternalMarkPassingAlgorithmCheckbox = new CheckBox(stringMessages.useInternalAlgorithm());
        useInternalMarkPassingAlgorithmCheckbox.setWordWrap(false);
        useInternalMarkPassingAlgorithmCheckbox.setValue(Boolean.FALSE);
        trackableRacesPanel.add(useInternalMarkPassingAlgorithmCheckbox);
        
        // text box for filtering the cell table
        HorizontalPanel filterPanel = new HorizontalPanel();
        filterPanel.setSpacing(5);
        trackableRacesPanel.add(filterPanel);

        Label lblFilterEvents = new Label(stringConstants.filterRaces() + ":");
        filterPanel.add(lblFilterEvents);
        filterPanel.setCellVerticalAlignment(lblFilterEvents, HasVerticalAlignment.ALIGN_MIDDLE);
        
        raceTable = new FlushableCellTable<SwissTimingRaceRecordDTO>(/* pageSize */10000, tableRes);
        raceTable.setWidth("300px");
        raceList = new ListDataProvider<SwissTimingRaceRecordDTO>();
        filterablePanelEvents = new LabeledAbstractFilterablePanel<SwissTimingRaceRecordDTO>(lblFilterEvents,
                availableSwissTimingRaces, raceTable, raceList) {
            @Override
            public Iterable<String> getSearchableStrings(SwissTimingRaceRecordDTO t) {
                List<String> strings = new ArrayList<>();
                strings.add(t.regattaName);
                strings.add(t.seriesName);
                strings.add(t.getName());
                strings.add(t.raceStatus);
                strings.add(t.boatClass);
                strings.add(t.gender);
                if (t.raceStartTime != null) {
                    strings.add(dateFormatter.render(t.raceStartTime));
                }
                return strings;
            }
        };
        
        final EntityIdentityComparator<SwissTimingRaceRecordDTO> entityIdentityComparator = new EntityIdentityComparator<SwissTimingRaceRecordDTO>() {
            @Override
            public boolean representSameEntity(SwissTimingRaceRecordDTO dto1, SwissTimingRaceRecordDTO dto2) {
                return dto1.raceId.equals(dto2.raceId);
            }
            @Override
            public int hashCode(SwissTimingRaceRecordDTO t) {
                return t.raceId.hashCode();
            }
        };

        TextColumn<SwissTimingRaceRecordDTO> raceNameColumn = new TextColumn<SwissTimingRaceRecordDTO>() {
            @Override
            public String getValue(SwissTimingRaceRecordDTO object) {
                return object.getName();
            }
        };

        TextColumn<SwissTimingRaceRecordDTO> regattaNameColumn = new TextColumn<SwissTimingRaceRecordDTO>() {
            @Override
            public String getValue(SwissTimingRaceRecordDTO object) {
                return object.regattaName;
            }
        };

        SelectionCheckboxColumn<SwissTimingRaceRecordDTO> selectionColumn = new SelectionCheckboxColumn<SwissTimingRaceRecordDTO>(
                tableRes.cellTableStyle().cellTableCheckboxSelected(),
                tableRes.cellTableStyle().cellTableCheckboxDeselected(),
                tableRes.cellTableStyle().cellTableCheckboxColumnCell(), entityIdentityComparator, raceList, raceTable);

        TextColumn<SwissTimingRaceRecordDTO> seriesNameColumn = new TextColumn<SwissTimingRaceRecordDTO>() {
            @Override
            public String getValue(SwissTimingRaceRecordDTO object) {
                return object.seriesName;
            }
        };

        TextColumn<SwissTimingRaceRecordDTO> raceIdColumn = new TextColumn<SwissTimingRaceRecordDTO>() {
            @Override
            public String getValue(SwissTimingRaceRecordDTO object) {
                return object.raceId;
            }
        };
        
        TextColumn<SwissTimingRaceRecordDTO> boatClassColumn = new TextColumn<SwissTimingRaceRecordDTO>() {
            @Override
            public String getValue(SwissTimingRaceRecordDTO object) {
                return object.boatClass != null ? object.boatClass : "";
            }
        };

        TextColumn<SwissTimingRaceRecordDTO> genderColumn = new TextColumn<SwissTimingRaceRecordDTO>() {
            @Override
            public String getValue(SwissTimingRaceRecordDTO object) {
                return object.gender != null ? object.gender : "";
            }
        };

        TextColumn<SwissTimingRaceRecordDTO> raceStatusColumn = new TextColumn<SwissTimingRaceRecordDTO>() {
            @Override
            public String getValue(SwissTimingRaceRecordDTO object) {
                return object.raceStatus != null ? object.raceStatus : "";
            }
        };

        TextColumn<SwissTimingRaceRecordDTO> raceStartTimeColumn = new TextColumn<SwissTimingRaceRecordDTO>() {
            @Override
            public String getValue(SwissTimingRaceRecordDTO object) {
                return object.raceStartTime==null?"":dateFormatter.render(object.raceStartTime) + " " + timeFormatter.render(object.raceStartTime);
            }
        };

        raceNameColumn.setSortable(true);
        raceStartTimeColumn.setSortable(true);
        boatClassColumn.setSortable(true);
        raceIdColumn.setSortable(true);
        genderColumn.setSortable(true);
        raceStatusColumn.setSortable(true);
        regattaNameColumn.setSortable(true);
        seriesNameColumn.setSortable(true);
        

        raceTable.addColumn(selectionColumn, selectionColumn.getHeader());
        raceTable.addColumn(regattaNameColumn, stringConstants.regatta());
        raceTable.addColumn(seriesNameColumn, stringConstants.series());
        raceTable.addColumn(raceNameColumn, stringConstants.name());
        //raceTable.addColumn(raceIdColumn, stringConstants.id());
        raceTable.addColumn(raceStatusColumn, stringConstants.status());
        raceTable.addColumn(boatClassColumn, stringConstants.boatClass());
        raceTable.addColumn(genderColumn, stringConstants.gender());
        raceTable.addColumn(raceStartTimeColumn, stringConstants.startTime());

        raceTable.setSelectionModel(selectionColumn.getSelectionModel(), selectionColumn.getSelectionManager());
        
        trackableRacesPanel.add(raceTable);
        raceList.addDataDisplay(raceTable);
        Handler columnSortHandler = getRaceTableColumnSortHandler(raceList.getList(), regattaNameColumn, seriesNameColumn,
        		raceNameColumn, raceStartTimeColumn, raceIdColumn, boatClassColumn, genderColumn, raceStatusColumn);
        raceTable.addColumnSortHandler(columnSortHandler);
        
        trackedRacesPanel.add(trackedRacesListComposite);
        filterPanel.add(filterablePanelEvents);
        HorizontalPanel racesButtonPanel = new HorizontalPanel();
        trackableRacesPanel.add(racesButtonPanel);

        Button btnTrack = new Button(stringConstants.startTracking());
        racesButtonPanel.add(btnTrack);
        racesButtonPanel.setSpacing(10);
        btnTrack.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                trackSelectedRaces(trackWindCheckbox.getValue(), declinationCheckbox.getValue(),
                        useInternalMarkPassingAlgorithmCheckbox.getValue());
            }
        });
    }

    /**
     * This function tries to infer a valid JsonUrl for any input given that matches the pattern of an event Id from
     * M2S. If there is an event id detected the Json Url gets updated and the event Id textbox is filled with the
     * detected event Id. The ID pattern is defined in {@link eventIdPattern}.
     */
    private void updateUrlFromEventId(String eventIdTextbox) {
        if (eventIdTextbox.matches(".*" + eventIdPattern + ".*")) {
            final String inferredEventId = eventIdTextbox.replaceFirst(".*(" + eventIdPattern + ").*", "$1");
            jsonUrlBox.setValue(
                    manage2sailBaseAPIUrl + inferredEventId + manage2sailAPIaccessToken + manage2sailUrlAppendix);
            eventIdBox.setValue(inferredEventId);
        }
    }

    /**
     * Similar to {@link #updateUrlFromEventId} this function tries to extract a M2S event Id by looking at the given
     * url in the Json Url Textbox. The value of {@link eventIdBox} is then set to the event ID inferred from the Json Url.
     */
    private void updateEventIdFromUrl(String jsonUrlTextBox) {
        if (jsonUrlTextBox.matches("http://manage2sail.com/.*" + eventIdPattern + ".*")) {
            final String inferredEventId = jsonUrlTextBox.replaceFirst(".*(" + eventIdPattern + ").*", "$1");
            eventIdBox.setValue(inferredEventId);
        }
    }

    private ListHandler<SwissTimingRaceRecordDTO> getRaceTableColumnSortHandler(List<SwissTimingRaceRecordDTO> raceRecords,
    		Column<SwissTimingRaceRecordDTO, ?> regattaNameColumn, Column<SwissTimingRaceRecordDTO, ?> seriesNameColumn, 
    		Column<SwissTimingRaceRecordDTO, ?> nameColumn, Column<SwissTimingRaceRecordDTO, ?> trackingStartColumn,
            Column<SwissTimingRaceRecordDTO, ?> raceIdColumn, Column<SwissTimingRaceRecordDTO, ?> boatClassColumn,
            Column<SwissTimingRaceRecordDTO, ?> genderColumn, Column<SwissTimingRaceRecordDTO, ?> statusColumn) {
           
        ListHandler<SwissTimingRaceRecordDTO> result = new ListHandler<SwissTimingRaceRecordDTO>(raceRecords);
        result.setComparator(regattaNameColumn, new Comparator<SwissTimingRaceRecordDTO>() {
            @Override
            public int compare(SwissTimingRaceRecordDTO o1, SwissTimingRaceRecordDTO o2) {
                return new NaturalComparator().compare(o1.regattaName,  o2.regattaName);
            }
        });
        result.setComparator(seriesNameColumn, new Comparator<SwissTimingRaceRecordDTO>() {
            @Override
            public int compare(SwissTimingRaceRecordDTO o1, SwissTimingRaceRecordDTO o2) {
                return new NaturalComparator().compare(o1.seriesName,  o2.seriesName);
            }
        });
        result.setComparator(nameColumn, new Comparator<SwissTimingRaceRecordDTO>() {
            @Override
            public int compare(SwissTimingRaceRecordDTO o1, SwissTimingRaceRecordDTO o2) {
                return new NaturalComparator().compare(o1.getName(),  o2.getName());
            }
        });
        result.setComparator(trackingStartColumn, new Comparator<SwissTimingRaceRecordDTO>() {
            @Override
            public int compare(SwissTimingRaceRecordDTO o1, SwissTimingRaceRecordDTO o2) {
                return o1.raceStartTime == null ? -1 : o2.raceStartTime == null ? 1 : o1.raceStartTime
                        .compareTo(o2.raceStartTime);
            }
        });
        result.setComparator(raceIdColumn, new Comparator<SwissTimingRaceRecordDTO>()  {
            @Override
            public int compare(SwissTimingRaceRecordDTO o1, SwissTimingRaceRecordDTO o2) {
                return o1.raceId == null ? -1 : o2.raceId == null ? 1 : 
                    new NaturalComparator().compare(o1.raceId, o2.raceId);
            }
        });
        result.setComparator(boatClassColumn, new Comparator<SwissTimingRaceRecordDTO>() {
            @Override
            public int compare(SwissTimingRaceRecordDTO o1, SwissTimingRaceRecordDTO o2) {
                return o1.boatClass == null ? -1 : o2.boatClass == null ? 1 : new NaturalComparator(false).compare(o1.boatClass, o2.boatClass);
            }
        });
        result.setComparator(genderColumn, new Comparator<SwissTimingRaceRecordDTO>() {
            @Override
            public int compare(SwissTimingRaceRecordDTO o1, SwissTimingRaceRecordDTO o2) {
                return o1.gender == null ? -1 : o2.gender == null ? 1 : new NaturalComparator(false).compare(o1.gender, o2.gender);
            }
        });
        result.setComparator(statusColumn, new Comparator<SwissTimingRaceRecordDTO>() {
            @Override
            public int compare(SwissTimingRaceRecordDTO o1, SwissTimingRaceRecordDTO o2) {
                return new NaturalComparator().compare(o1.raceStatus,  o2.raceStatus);
            }
        });
        return result;
    }

    private void fillConfigurations() {
        sailingService.getPreviousSwissTimingConfigurations(new AsyncCallback<List<SwissTimingConfigurationDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Remote Procedure Call getPreviousConfigurations() - Failure: "
                        + caught.getMessage());
            }

            @Override
            public void onSuccess(List<SwissTimingConfigurationDTO> result) {
                while (previousConfigurationsComboBox.getItemCount() > 0) {
                    previousConfigurationsComboBox.removeItem(0);
                }
                Collections.sort(result, (c1, c2) -> c1.getName().compareTo(c2.getName()));
                for (SwissTimingConfigurationDTO stConfig : result) {
                    previousConfigurations.put(stConfig.getName(), stConfig);
                    previousConfigurationsComboBox.addItem(stConfig.getName());
                }
                if (!result.isEmpty()) {
                    updatePanelFromSelectedStoredConfiguration();
                }
            }
        });
    }

    private void fillRaces(final SailingServiceAsync sailingService) {
        final String jsonUrl = jsonUrlBox.getValue();
        final String updateURL = updateURLBox.getValue();
        final String updateUsername = updateUsernameBox.getValue();
        final String updatePassword = updatePasswordBox.getValue();
        sailingService.getRacesOfSwissTimingEvent(jsonUrl, new AsyncCallback<SwissTimingEventRecordDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                SwissTimingEventManagementPanel.this.errorReporter.reportError("Error trying to list races: "
                        + caught.getMessage());
            }

            @Override
            public void onSuccess(final SwissTimingEventRecordDTO result) {
                availableSwissTimingRaces.clear();
                if (result != null) {
                    availableSwissTimingRaces.addAll(result.races);
                }
                raceList.getList().clear();
                raceList.getList().addAll(availableSwissTimingRaces);
                filterablePanelEvents.getTextBox().setText(null);
                filterablePanelEvents.updateAll(result.races);
                // store a successful configuration in the database for later retrieval
                final String hostname = result.trackingDataHost;
                final Integer port = result.trackingDataPort;
                final String configName = result.eventName;
                sailingService.storeSwissTimingConfiguration(configName, jsonUrl, hostname, port, updateURL, updateUsername, updatePassword,
                        new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Exception trying to store configuration in DB: "
                                        + caught.getMessage());
                            }

                            @Override
                            public void onSuccess(Void voidResult) {
                                // refresh list of previous configurations
                                SwissTimingConfigurationDTO stConfig = new SwissTimingConfigurationDTO(configName, jsonUrl,
                                        hostname, port, updateURL, updateUsername, updatePassword);
                                if (previousConfigurations.put(stConfig.getName(), stConfig) == null) {
                                    previousConfigurationsComboBox.addItem(stConfig.getName());
                                }
                                for (int i=0; i<previousConfigurationsComboBox.getItemCount(); i++) {
                                    if (previousConfigurationsComboBox.getValue(i).equals(stConfig.getName())) {
                                        previousConfigurationsComboBox.setSelectedIndex(i);
                                        break;
                                    }
                                }
                                updatePanelFromSelectedStoredConfiguration();
                            }
                        });
            }
        });
    }

    private void trackSelectedRaces(boolean trackWind, boolean correctWindByDeclination, boolean useInternalMarkPassingAlgorithm) {
        final String hostname = hostnameTextbox.getValue();
        final Integer port = portIntegerbox.getValue();
        final String updateURL = updateURLBox.getValue();
        final String updateUsername = updateUsernameBox.getValue();
        final String updatePassword = updatePasswordBox.getValue();
        final List<SwissTimingRaceRecordDTO> selectedRaces = new ArrayList<SwissTimingRaceRecordDTO>();
        for (final SwissTimingRaceRecordDTO race : this.raceList.getList()) {
            if (raceTable.getSelectionModel().isSelected(race)) {
                selectedRaces.add(race);
            }
        }
        RegattaDTO selectedRegatta = getSelectedRegatta();
        RegattaIdentifier regattaIdentifier = null;
        if (selectedRegatta != null) {
            regattaIdentifier = new RegattaName(selectedRegatta.getName());
        }
        
        // Check if the assigned regatta makes sense
        if (checkBoatClassOK(selectedRegatta, selectedRaces)) {
            sailingService.trackWithSwissTiming(
                /* regattaToAddTo */ regattaIdentifier,
                selectedRaces, hostname, port, trackWind, correctWindByDeclination,
                useInternalMarkPassingAlgorithm, updateURL, updateUsername, updatePassword, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error trying to register races " + selectedRaces + " for tracking: "
                                + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(Void result) {
                        regattaRefresher.fillRegattas();
                    }
                });
        }
    }

    private void updatePanelFromSelectedStoredConfiguration() {
        if (previousConfigurationsComboBox.getSelectedIndex() >= 0) {
            SwissTimingConfigurationDTO stConfig = previousConfigurations.get(previousConfigurationsComboBox
                    .getItemText(previousConfigurationsComboBox.getSelectedIndex()));
            if (stConfig != null) {
                hostnameTextbox.setValue(stConfig.getHostname());
                portIntegerbox.setValue(stConfig.getPort());
                jsonUrlBox.setValue(stConfig.getJsonURL());
                updateURLBox.setValue(stConfig.getUpdateURL());
                updateUsernameBox.setValue(stConfig.getUpdateUsername());
                updatePasswordBox.setValue(stConfig.getUpdatePassword());
            }
        }
    }

}

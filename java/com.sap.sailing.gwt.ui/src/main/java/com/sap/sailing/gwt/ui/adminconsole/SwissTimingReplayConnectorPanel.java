package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.CellTable;
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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingArchiveConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingReplayRaceDTO;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.BaseCelltable;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;

/**
 * Allows the user to start and stop tracking of events, regattas and races using the SwissTiming connector.
 * Inspired by TracTracEventManagementPanel  
 * 
 * @author Jens Rommel (D047974)
 * 
 */
public class SwissTimingReplayConnectorPanel extends AbstractEventManagementPanel {
    private final ErrorReporter errorReporter;
    private final LabeledAbstractFilterablePanel<SwissTimingReplayRaceDTO> filterablePanelEvents;
    private final ListDataProvider<SwissTimingReplayRaceDTO> raceList;
    private final CellTable<SwissTimingReplayRaceDTO> raceTable;
    private final Map<String, SwissTimingArchiveConfigurationDTO> previousConfigurations;
    private final ListBox previousConfigurationsComboBox;
    private final TextBox jsonUrlBox;
    private final Grid grid;
    private final List<SwissTimingReplayRaceDTO> availableSwissTimingRaces;

    public SwissTimingReplayConnectorPanel(final SailingServiceAsync sailingService,
            ErrorReporter errorReporter,
            RegattaRefresher regattaRefresher, StringMessages stringMessages) {
        super(sailingService, regattaRefresher, errorReporter, true, stringMessages);
        this.errorReporter = errorReporter;
        availableSwissTimingRaces = new ArrayList<SwissTimingReplayRaceDTO>();
        previousConfigurationsComboBox = new ListBox();
        previousConfigurationsComboBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                updateJsonUrlFromSelectedPreviousConfiguration();
            }
        });
        previousConfigurations = new HashMap<String, SwissTimingArchiveConfigurationDTO>();
        getConnectionHistory();

        jsonUrlBox = new TextBox();
        jsonUrlBox.getElement().getStyle().setWidth(50, Unit.EM);

        VerticalPanel mainPanel = new VerticalPanel();
        this.setWidget(mainPanel);
        mainPanel.setWidth("100%");
        
        CaptionPanel captionPanelConnections = new CaptionPanel(stringMessages.connections());
        mainPanel.add(captionPanelConnections);

        VerticalPanel verticalPanel = new VerticalPanel();
        
        captionPanelConnections.setContentWidget(verticalPanel);
        captionPanelConnections.setStyleName("bold");
        
        grid = new Grid(3, 2);
        verticalPanel.add(grid);

        grid.setWidget(0, 0, new Label(stringMessages.swissTimingEvents() + ":"));
        grid.setWidget(0, 1, previousConfigurationsComboBox);
        grid.setWidget(1, 0, new Label(stringMessages.jsonUrl() + ":"));
        grid.setWidget(1, 1, jsonUrlBox);

        Button btnListRaces = new Button(stringMessages.listRaces());
        grid.setWidget(2, 1, btnListRaces);
        btnListRaces.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fillRaces(sailingService);
            }
        });
        
        TextColumn<SwissTimingReplayRaceDTO> regattaNameColumn = new TextColumn<SwissTimingReplayRaceDTO>() {
            @Override
            public String getValue(SwissTimingReplayRaceDTO object) {
                return object.rsc;
            }
        };
        TextColumn<SwissTimingReplayRaceDTO> raceNameColumn = new TextColumn<SwissTimingReplayRaceDTO>() {
            @Override
            public String getValue(SwissTimingReplayRaceDTO object) {
                return object.getName();
            }
        };
        TextColumn<SwissTimingReplayRaceDTO> raceStartTrackingColumn = new TextColumn<SwissTimingReplayRaceDTO>() {
            @Override
            public String getValue(SwissTimingReplayRaceDTO object) {
                return object.startTime == null ? "" : dateFormatter.render(object.startTime) + " " + timeFormatter.render(object.startTime);
            }
        };
        TextColumn<SwissTimingReplayRaceDTO> boatClassNamesColumn = new TextColumn<SwissTimingReplayRaceDTO>() {
            @Override
            public String getValue(SwissTimingReplayRaceDTO object) {
                return object.boat_class;
            }

        };
        
        HorizontalPanel racesSplitPanel = new HorizontalPanel();
        mainPanel.add(racesSplitPanel);
        
        CaptionPanel racesCaptionPanel = new CaptionPanel(stringMessages.trackableRaces());
        racesSplitPanel.add(racesCaptionPanel);
        racesCaptionPanel.setWidth("50%");

        CaptionPanel trackedRacesCaptionPanel = new CaptionPanel(stringMessages.trackedRaces());
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
        // the regatta selection for a tracked race
        HorizontalPanel regattaPanel = new HorizontalPanel();
        racesPanel.add(regattaPanel);
        Label lblRegattas = new Label(stringMessages.regattaUsedForTheTrackedRace());
        lblRegattas.setWordWrap(false);
        regattaPanel.setCellVerticalAlignment(lblRegattas, HasVerticalAlignment.ALIGN_MIDDLE);
        regattaPanel.setSpacing(5);
        regattaPanel.add(lblRegattas);
        regattaPanel.add(getAvailableRegattasListBox());
        regattaPanel.setCellVerticalAlignment(getAvailableRegattasListBox(), HasVerticalAlignment.ALIGN_MIDDLE);
        
        HorizontalPanel filterPanel = new HorizontalPanel();
        filterPanel.setSpacing(5);
        racesPanel.add(filterPanel);
        
        Label lblFilterEvents = new Label(stringMessages.filterRaces()+ ":");
        filterPanel.add(lblFilterEvents);
        filterPanel.setCellVerticalAlignment(lblFilterEvents, HasVerticalAlignment.ALIGN_MIDDLE);
        

        HorizontalPanel racesHorizontalPanel = new HorizontalPanel();
        racesPanel.add(racesHorizontalPanel);
        VerticalPanel trackPanel = new VerticalPanel();
        trackPanel.setStyleName("paddedPanel");
        raceNameColumn.setSortable(true);
        raceStartTrackingColumn.setSortable(true);
        boatClassNamesColumn.setSortable(true);
        AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
        raceTable = new BaseCelltable<SwissTimingReplayRaceDTO>(/* pageSize */10000, tableRes);
        raceTable.addColumn(raceNameColumn, stringMessages.race());
        raceTable.addColumn(regattaNameColumn, "RSC");
        raceTable.addColumn(boatClassNamesColumn, stringMessages.boatClass());
        raceTable.addColumn(raceStartTrackingColumn, stringMessages.startTime());
        raceTable.setWidth("300px");
        raceList = new ListDataProvider<SwissTimingReplayRaceDTO>();
        filterablePanelEvents = new LabeledAbstractFilterablePanel<SwissTimingReplayRaceDTO>(lblFilterEvents, availableSwissTimingRaces, raceList) {
            @Override
            public List<String> getSearchableStrings(SwissTimingReplayRaceDTO t) {
                List<String> strings = new ArrayList<String>();
                strings.addAll(Arrays.asList(t.boat_class, t.flight_number, t.getName(), t.rsc));
                return strings;
            }

            @Override
            public AbstractCellTable<SwissTimingReplayRaceDTO> getCellTable() {
                return raceTable;
            }
        };
        raceTable.setSelectionModel(new RefreshableMultiSelectionModel<SwissTimingReplayRaceDTO>(
                new EntityIdentityComparator<SwissTimingReplayRaceDTO>() {
                    @Override
                    public boolean representSameEntity(SwissTimingReplayRaceDTO dto1, SwissTimingReplayRaceDTO dto2) {
                        return dto1.race_id.equals(dto2.race_id);
                    }
                    @Override
                    public int hashCode(SwissTimingReplayRaceDTO t) {
                        return t.race_id.hashCode();
                    }
                }, filterablePanelEvents.getAllListDataProvider()) {
        });

        racesHorizontalPanel.add(raceTable);
        racesHorizontalPanel.add(trackPanel);
        raceList.addDataDisplay(raceTable);
        Handler columnSortHandler = getRaceTableColumnSortHandler(raceList.getList(), raceNameColumn, boatClassNamesColumn, raceStartTrackingColumn);
        raceTable.addColumnSortHandler(columnSortHandler);
        filterPanel.add(filterablePanelEvents);
        
        Label lblTrackSettings = new Label(stringMessages.trackNewEvent());
        trackPanel.add(lblTrackSettings);
        
        final CheckBox trackWindCheckbox = new CheckBox(stringMessages.trackWind());
        trackWindCheckbox.setWordWrap(false);
        trackWindCheckbox.setValue(true);
        trackPanel.add(trackWindCheckbox);

        final CheckBox declinationCheckbox = new CheckBox(stringMessages.declinationCheckbox());
        declinationCheckbox.setWordWrap(false);
        declinationCheckbox.setValue(true);
        trackPanel.add(declinationCheckbox);
        
        final CheckBox useInternalMarkPassingAlgorithmCheckbox = new CheckBox(stringMessages.useInternalAlgorithm());
        useInternalMarkPassingAlgorithmCheckbox.setWordWrap(false);
        useInternalMarkPassingAlgorithmCheckbox.setValue(Boolean.FALSE);
        trackPanel.add(useInternalMarkPassingAlgorithmCheckbox);

        trackedRacesPanel.add(trackedRacesListComposite);

        HorizontalPanel racesButtonPanel = new HorizontalPanel();
        racesPanel.add(racesButtonPanel);

        Button btnTrack = new Button(stringMessages.startTracking());
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

    private String getBoatClassNamesAsString(SwissTimingReplayRaceDTO object) {
        return object.boat_class;
    }

    private ListHandler<SwissTimingReplayRaceDTO> getRaceTableColumnSortHandler(List<SwissTimingReplayRaceDTO> raceRecords,
            Column<SwissTimingReplayRaceDTO, ?> nameColumn, Column<SwissTimingReplayRaceDTO, ?> boatClassColumn,
            Column<SwissTimingReplayRaceDTO, ?> startTimeColumn) {
        ListHandler<SwissTimingReplayRaceDTO> result = new ListHandler<SwissTimingReplayRaceDTO>(raceRecords);
        result.setComparator(nameColumn, new Comparator<SwissTimingReplayRaceDTO>() {
            @Override
            public int compare(SwissTimingReplayRaceDTO o1, SwissTimingReplayRaceDTO o2) {
                return new NaturalComparator().compare(o1.getName(), o2.getName());
            }
        });
        result.setComparator(boatClassColumn, new Comparator<SwissTimingReplayRaceDTO>() {
            @Override
            public int compare(SwissTimingReplayRaceDTO o1, SwissTimingReplayRaceDTO o2) {
                return new NaturalComparator(false).compare(getBoatClassNamesAsString(o1), getBoatClassNamesAsString(o2));
            }
        });
        result.setComparator(startTimeColumn, new Comparator<SwissTimingReplayRaceDTO>() {
            @Override
            public int compare(SwissTimingReplayRaceDTO o1, SwissTimingReplayRaceDTO o2) {
                return o1.startTime == null ? -1 : o2.startTime == null ? 1 : o1.startTime
                        .compareTo(o2.startTime);
            }
        });
        return result;
    }

    private void getConnectionHistory() {
        sailingService.getPreviousSwissTimingArchiveConfigurations(new AsyncCallback<List<SwissTimingArchiveConfigurationDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Remote Procedure Call getPreviousConfigurations() - Failure: "
                        + caught.getMessage());
            }

            @Override
            public void onSuccess(List<SwissTimingArchiveConfigurationDTO> result) {
                previousConfigurationsComboBox.clear();
                previousConfigurations.clear();
                Collections.sort(result, (c1, c2) -> c1.getJsonUrl().compareTo(c2.getJsonUrl()));
                for (SwissTimingArchiveConfigurationDTO configEntry : result) {
                    String name = configEntry.getJsonUrl();
                    previousConfigurations.put(name, configEntry);
                    previousConfigurationsComboBox.addItem(name);
                }
                updateJsonUrlFromSelectedPreviousConfiguration();
            }
        });
    }

    private void fillRaces(final SailingServiceAsync sailingService) {
        final String swissTimingJsonUrl = jsonUrlBox.getValue();
        sailingService.listSwissTiminigReplayRaces(swissTimingJsonUrl, new AsyncCallback<List<SwissTimingReplayRaceDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                SwissTimingReplayConnectorPanel.this.errorReporter.reportError("Error trying to list SwissTiming races: "
                        + caught.getMessage());
            }

            @Override
            public void onSuccess(final List<SwissTimingReplayRaceDTO> races) {
                availableSwissTimingRaces.clear();
                availableSwissTimingRaces.addAll(races);
                raceList.getList().clear();
                raceList.getList().addAll(availableSwissTimingRaces);
                filterablePanelEvents.getTextBox().setText(null);
                filterablePanelEvents.updateAll(races);
                // store a successful configuration in the database for later retrieval
                sailingService.storeSwissTimingArchiveConfiguration(swissTimingJsonUrl,
                        new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Exception trying to store configuration in DB: "
                                        + caught.getMessage());
                            }

                            @Override
                            public void onSuccess(Void voidResult) {
                                // refresh list of previous configurations
                                SwissTimingArchiveConfigurationDTO stConfig = new SwissTimingArchiveConfigurationDTO(swissTimingJsonUrl);
                                if (previousConfigurations.put(stConfig.getJsonUrl(), stConfig) == null) {
                                    previousConfigurationsComboBox.addItem(stConfig.getJsonUrl());
                                }
                            }
                        });
            }
        });
    }

    private void trackSelectedRaces(boolean trackWind, boolean correctWindByDeclination, boolean useInternalMarkPassingAlgorithm) {
        RegattaDTO selectedRegatta = getSelectedRegatta();
        RegattaIdentifier regattaIdentifier = null;
        if (selectedRegatta != null) {
            regattaIdentifier = new RegattaName(selectedRegatta.getName());
        }
        
        // Check if the assigned regatta makes sense
        final List<SwissTimingReplayRaceDTO> selectedRaces = new ArrayList<>();  
        for (SwissTimingReplayRaceDTO replayRace : raceList.getList()) {
            if (raceTable.getSelectionModel().isSelected(replayRace)) {
                selectedRaces.add(replayRace);
            }
        }
        if (checkBoatClassOK(selectedRegatta, selectedRaces)) {
            sailingService.replaySwissTimingRace(regattaIdentifier, selectedRaces, trackWind, correctWindByDeclination,
                useInternalMarkPassingAlgorithm, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error trying to register races " + selectedRaces
                                + " for tracking: " + caught.getMessage() + ". Check live/stored URI syntax.");
                    }

                    @Override
                    public void onSuccess(Void result) {
                        regattaRefresher.fillRegattas();
                    }
                });
        }
    }

    private void updateJsonUrlFromSelectedPreviousConfiguration() {
        int selectedIndex = previousConfigurationsComboBox.getSelectedIndex();
        String selectedConfiguration;
        if (selectedIndex >= 0) {
            selectedConfiguration = previousConfigurationsComboBox.getItemText(selectedIndex);
        } else {
            selectedConfiguration = null;
        }
        jsonUrlBox.setText(selectedConfiguration);
    }
    
}

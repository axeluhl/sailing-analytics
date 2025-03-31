package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.ColumnFormatter;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleView.Presenter;
import com.sap.sailing.gwt.ui.adminconsole.yellowbrick.YellowBrickConfigurationDialog;
import com.sap.sailing.gwt.ui.adminconsole.yellowbrick.YellowBrickConfigurationTableWrapper;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.YellowBrickConfigurationWithSecurityDTO;
import com.sap.sailing.gwt.ui.shared.YellowBrickRaceRecordDTO;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.adminconsole.FilterablePanelProvider;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.celltable.AbstractSortableTextColumn;
import com.sap.sse.gwt.client.celltable.CellTableWithCheckboxResources;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.celltable.TableWrapperWithMultiSelectionAndFilter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.panels.AbstractFilterablePanel;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.AccessControlledButtonPanel;

/**
 * Allows the user to start and stop tracking of YellowBrick races using the YellowBrick connector. In particular,
 * previously configured races can be viewed in a table. The user can create a new "connection" by providing a race name
 * for the URL construction.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class YellowBrickEventManagementPanel extends AbstractEventManagementPanel implements FilterablePanelProvider<YellowBrickConfigurationWithSecurityDTO> {
    private final ErrorReporter errorReporter;
    
    private YellowBrickConfigurationTableWrapper connectionsTable;

    private Label loadingMessageLabel;

    private TableWrapperWithMultiSelectionAndFilter<YellowBrickRaceRecordDTO, StringMessages, AdminConsoleTableResources> racesTableWrapper;

    private final UserService userService;
    private final CellTableWithCheckboxResources tableResources;
    private final SailingServiceWriteAsync sailingServiceWrite;

    public YellowBrickEventManagementPanel(final Presenter presenter, StringMessages stringMessages,
            final CellTableWithCheckboxResources tableResources) {
        super(presenter, true, stringMessages);
        this.userService = presenter.getUserService();
        this.errorReporter = presenter.getErrorReporter();
        this.sailingServiceWrite = presenter.getSailingService();
        this.tableResources = tableResources;
        this.setWidget(createContent());
    }
    
    protected Widget createContent() {
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setWidth("100%");
        CaptionPanel connectionsPanel = createConnectionsPanel();
        mainPanel.add(connectionsPanel);
        HorizontalPanel racesPanel = createRacesPanel();
        racesPanel.setWidth("100%");
        mainPanel.add(racesPanel);
        return mainPanel;
    }
    
    protected CaptionPanel createConnectionsPanel() {
        CaptionPanel connectionsPanel = new CaptionPanel("YellowBrick " + stringMessages.connections());
        connectionsPanel.ensureDebugId("YellowBrickConfigurationsSection");
        connectionsPanel.setStyleName("bold");
        VerticalPanel tableAndConfigurationPanel = new VerticalPanel();
        connectionsTable = new YellowBrickConfigurationTableWrapper(userService, sailingServiceWrite, stringMessages,
                errorReporter, true, tableResources, () -> {});
        connectionsTable.refreshYellowBrickConfigurationList();
        final Grid grid = new Grid(1, 2);
        // Add YellowBrick configurations
        final AccessControlledButtonPanel buttonPanel = new AccessControlledButtonPanel(userService, SecuredDomainType.TRACKED_RACE);
        buttonPanel.addUnsecuredAction(stringMessages.refresh(), () -> connectionsTable.refreshYellowBrickConfigurationList());
        Button addCreateAction = buttonPanel.addCreateAction(stringMessages.addYellowBrickConfiguration(),
                () -> new YellowBrickConfigurationDialog(
                        new DialogCallback<YellowBrickConfigurationWithSecurityDTO>() {
                            @Override
                            public void ok(YellowBrickConfigurationWithSecurityDTO editedConfiguration) {
                                sailingServiceWrite.createYellowBrickConfiguration(editedConfiguration.getName(),
                                        editedConfiguration.getRaceUrl(),
                                        editedConfiguration.getUsername(), editedConfiguration.getPassword(),
                                        new MarkedAsyncCallback<Void>(new AsyncCallback<Void>() {
                                            @Override
                                            public void onFailure(Throwable caught) {
                                                reportError("Exception trying to create configuration in DB: "
                                                        + caught.getMessage());
                                            }

                                            @Override
                                            public void onSuccess(Void voidResult) {
                                                connectionsTable.refreshYellowBrickConnectionList(/* selectWhenDone */ editedConfiguration);
                                            }
                                        }));
                            }

                            @Override
                            public void cancel() {
                            }
                        }, userService, errorReporter).show());
        addCreateAction.ensureDebugId("AddConnectionButton");
        buttonPanel.addRemoveAction(stringMessages.remove(), connectionsTable.getSelectionModel(), false, () -> {
            sailingServiceWrite.deleteYellowBrickConfigurations(connectionsTable.getSelectionModel().getSelectedSet(),
                    new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError(
                                    "Exception trying to delete configuration(s) in DB: " + caught.getMessage());
                        }

                        @Override
                        public void onSuccess(Void result) {
                            connectionsTable.refreshYellowBrickConfigurationList();
                        }
                    });
        });
        loadingMessageLabel = new Label();
        final Button listRacesButton = buttonPanel.addUnsecuredAction(stringMessages.listRaces(), () -> {
            loadingMessageLabel.setText(stringMessages.loading());
            fillRaces(sailingServiceWrite);
        });
        listRacesButton.ensureDebugId("ListRacesButton");
        listRacesButton.setEnabled(false);
        buttonPanel.addUnsecuredWidget(loadingMessageLabel);
        connectionsTable.getSelectionModel().addSelectionChangeHandler(e -> {
            final boolean objectSelected = connectionsTable.getSelectionModel().getSelectedSet().size() == 1;
            listRacesButton.setEnabled(objectSelected);
        });
        tableAndConfigurationPanel.add(buttonPanel);
        tableAndConfigurationPanel.add(grid);
        tableAndConfigurationPanel.add(connectionsTable);

        connectionsPanel.setContentWidget(tableAndConfigurationPanel);

        return connectionsPanel;
    }
    
    protected HorizontalPanel createRacesPanel() {
        HorizontalPanel racesPanel = new HorizontalPanel();
        CaptionPanel trackableRacesPanel = createTrackableRacesPanel();
        racesPanel.add(trackableRacesPanel);
        racesPanel.setCellWidth(trackableRacesPanel, "50%");
        CaptionPanel trackedRacesPanel = createTrackedRacesPanel();
        racesPanel.add(trackedRacesPanel);
        racesPanel.setCellWidth(trackedRacesPanel, "50%");
        return racesPanel;
    }
    
    protected CaptionPanel createTrackableRacesPanel() {
        CaptionPanel trackableRacesPanel = new CaptionPanel(stringMessages.trackableRaces());
        trackableRacesPanel.ensureDebugId("YellowBrickTrackableRacesSection");
        trackableRacesPanel.setStyleName("bold");
        FlexTable layoutTable = new FlexTable();
        layoutTable.setWidth("100%");
        ColumnFormatter columnFormatter = layoutTable.getColumnFormatter();
        FlexCellFormatter cellFormatter = layoutTable.getFlexCellFormatter();
        columnFormatter.setWidth(0, "130px");
        // Regatta
        Label regattaForTrackingLabel = new Label(stringMessages.regattaUsedForTheTrackedRace());
        regattaForTrackingLabel.setWordWrap(false);
        int row = 0;
        layoutTable.setWidget(row, 0, regattaForTrackingLabel);
        layoutTable.setWidget(row, 1, getAvailableRegattasListBox());
        // Track settings (wind)
        Label trackSettingsLabel = new Label(stringMessages.trackSettings() + ":");
        final CheckBox trackWindCheckBox = new CheckBox(stringMessages.trackWind());
        trackWindCheckBox.ensureDebugId("YellowBrickTrackWindCheckBox");
        trackWindCheckBox.setWordWrap(false);
        trackWindCheckBox.setValue(Boolean.TRUE);
        final CheckBox correctWindCheckBox = new CheckBox(stringMessages.declinationCheckbox());
        correctWindCheckBox.ensureDebugId("YellowBrickCorrectWindCheckBox");
        correctWindCheckBox.setWordWrap(false);
        correctWindCheckBox.setValue(Boolean.TRUE);
        layoutTable.setWidget(++row, 0, trackSettingsLabel);
        layoutTable.setWidget(row, 1, trackWindCheckBox);
        layoutTable.setWidget(++row, 1, correctWindCheckBox);
        AdminConsoleTableResources tableResources = GWT.create(AdminConsoleTableResources.class);
        racesTableWrapper = new TableWrapperWithMultiSelectionAndFilter<YellowBrickRaceRecordDTO, StringMessages, AdminConsoleTableResources>(
                stringMessages, errorReporter, /* enablePager */ false,
                Optional.of(new EntityIdentityComparator<YellowBrickRaceRecordDTO>() {
                    @Override
                    public boolean representSameEntity(YellowBrickRaceRecordDTO dto1, YellowBrickRaceRecordDTO dto2) {
                        return dto1.getRaceUrl().equals(dto2.getRaceUrl());
                    }
                    @Override
                    public int hashCode(YellowBrickRaceRecordDTO t) {
                        return t.getRaceUrl().hashCode();
                    }
                }), tableResources, Optional.empty(), Optional.empty(), /* filter checkbox label */ stringMessages.filterRaces()) {
                @Override
                public List<String> getSearchableStrings(YellowBrickRaceRecordDTO t) {
                    List<String> strings = new ArrayList<String>();
                    strings.add(t.getName());
                    strings.add(t.getRaceUrl());
                    return strings;
                }
            };
        racesTableWrapper.getTable().ensureDebugId("YellowBrickTrackableRacesCellTable");
        // Races
        TextColumn<YellowBrickRaceRecordDTO> raceNameColumn = new AbstractSortableTextColumn<>(o->o.getName(), racesTableWrapper.getColumnSortHandler());
        TextColumn<YellowBrickRaceRecordDTO> raceUrlColumn = new AbstractSortableTextColumn<>(o->o.getRaceUrl(), racesTableWrapper.getColumnSortHandler());
        TextColumn<YellowBrickRaceRecordDTO> timePointOfLastFixColumn = new AbstractSortableTextColumn<>(
                o -> "" + o.getTimePointOfLastFix(), racesTableWrapper.getColumnSortHandler(),
                Comparator.comparing(o -> o.getTimePointOfLastFix()));
        TextColumn<YellowBrickRaceRecordDTO> numberOfCompetitorsColumn = new AbstractSortableTextColumn<>(
                o -> Integer.toString(o.getNumberOfCompetitors()), racesTableWrapper.getColumnSortHandler(),
                Comparator.comparing(o -> o.getNumberOfCompetitors()));
        racesTableWrapper.addColumn(raceNameColumn, stringMessages.race());
        racesTableWrapper.addColumn(raceUrlColumn, stringMessages.raceUrl());
        racesTableWrapper.addColumn(timePointOfLastFixColumn, stringMessages.timePointOfLastFix());
        racesTableWrapper.addColumn(numberOfCompetitorsColumn, stringMessages.numberOfCompetitors());
        layoutTable.setWidget(++row, 0, racesTableWrapper);
        cellFormatter.setColSpan(row, 0, 2);
        final Button startTrackingButton = new Button(stringMessages.startTracking());
        startTrackingButton.setEnabled(false);
        connectionsTable.getSelectionModel().addSelectionChangeHandler(
                e -> startTrackingButton.setEnabled(connectionsTable.getSelectionModel().getSelectedSet().size() == 1));
        startTrackingButton.ensureDebugId("StartTrackingButton");
        startTrackingButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                trackSelectedRaces(trackWindCheckBox.getValue(), correctWindCheckBox.getValue());
            }
        });
        layoutTable.setWidget(++row, 1, startTrackingButton);
        trackableRacesPanel.setContentWidget(layoutTable);
        return trackableRacesPanel;
    }
    
    protected CaptionPanel createTrackedRacesPanel() {
        CaptionPanel trackedRacesPanel = new CaptionPanel(stringMessages.trackedRaces());
        trackedRacesPanel.ensureDebugId("TrackedRacesSection");
        trackedRacesPanel.setStyleName("bold");
        trackedRacesPanel.setContentWidget(this.trackedRacesListComposite);
        return trackedRacesPanel;
    }
    
    protected void reportError(String message) {
        this.errorReporter.reportError(message);
    }
    
    private void fillRaces(final SailingServiceAsync sailingService) {
        final Set<YellowBrickConfigurationWithSecurityDTO> selectedConnections = connectionsTable.getSelectionModel()
                .getSelectedSet();
        if (!selectedConnections.isEmpty()) {
            YellowBrickConfigurationWithSecurityDTO selectedConnection = selectedConnections.iterator().next();
            sailingService.listYellowBrickRacesInEvent(selectedConnection,
                    new MarkedAsyncCallback<com.sap.sse.common.Util.Pair<String, List<YellowBrickRaceRecordDTO>>>(
                new AsyncCallback<com.sap.sse.common.Util.Pair<String, List<YellowBrickRaceRecordDTO>>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        loadingMessageLabel.setText("");
                        reportError("Error trying to list races: " + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(final com.sap.sse.common.Util.Pair<String, List<YellowBrickRaceRecordDTO>> result) {
                        loadingMessageLabel.setText("");
                        racesTableWrapper.refresh(result.getB());
                    }
                }));
        }
    }

    private void trackSelectedRaces(boolean trackWind, boolean correctWind) {
        final YellowBrickConfigurationWithSecurityDTO selectedConnection = connectionsTable.getSelectionModel()
                .getSelectedSet().iterator().next();
        final String creatorName = selectedConnection.getCreatorName();
        final String raceUrl = selectedConnection.getRaceUrl();
        RegattaDTO selectedRegatta = getSelectedRegatta(); // null meaning "Default Regatta" selection
        RegattaIdentifier regattaIdentifier = null;
        if (selectedRegatta != null) {
            regattaIdentifier = new RegattaName(selectedRegatta.getName());
        }
        // Check if the assigned regatta makes sense; the following cases need to be distinguished:
        //  - non-default regatta explicitly selected: check that boat class matches; disallow loading when there is a mismatch
        //  - "Default Regatta" selected: if race was assigned to a regatta before, use that without further checks;
        //                                otherwise, warn user if a "persistent" regatta with the same boat class already exists
        //                                because it may be an accidental omission to select that regatta for loading
        final RefreshableMultiSelectionModel<YellowBrickRaceRecordDTO> selectionModel = racesTableWrapper.getSelectionModel();
        final List<YellowBrickRaceRecordDTO> selectedRaces = new ArrayList<>(selectionModel.getSelectedSet());
        sailingServiceWrite.trackWithYellowBrick(regattaIdentifier, selectedRaces, trackWind, correctWind,
                creatorName, raceUrl, new MarkedAsyncCallback<Void>(new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        reportError(stringMessages.errorTryingToRegisterRacesForTracking(selectedRaces.toString(), caught.getMessage()));
                    }

                    @Override
                    public void onSuccess(Void result) {
                        YellowBrickEventManagementPanel.this.presenter.getRegattasRefresher().reloadAndCallFillAll();
                    }
                }));
    }
    
    public void refreshYellowBrickConnectors() {
        connectionsTable.refreshYellowBrickConfigurationList();
    }
    
    @Override
    public AbstractFilterablePanel<YellowBrickConfigurationWithSecurityDTO> getFilterablePanel() {
        return connectionsTable.getFilterField();
    }
}

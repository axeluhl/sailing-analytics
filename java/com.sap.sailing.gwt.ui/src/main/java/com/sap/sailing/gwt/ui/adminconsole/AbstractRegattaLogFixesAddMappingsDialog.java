package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.domain.common.racelog.tracking.MappableToDevice;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceIdentifierDTO;
import com.sap.sailing.gwt.ui.shared.DeviceMappingDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.TrackFileImportDeviceIdentifierDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.ui.client.UserService;

public class AbstractRegattaLogFixesAddMappingsDialog extends DataEntryDialog<Collection<DeviceMappingDTO>> {

    private final String leaderboardName;
    private final StringMessages stringMessages;
    private final SimplePanel importWidgetHolder;
    protected final TrackFileImportDeviceIdentifierTableWrapper deviceIdTable;
    private final CompetitorTableWrapper<RefreshableSingleSelectionModel<CompetitorDTO>> competitorTable;
    private final MarkTableWrapper<RefreshableSingleSelectionModel<MarkDTO>> markTable;
    private final BoatTableWrapper<RefreshableSingleSelectionModel<BoatDTO>> boatTable;

    private TrackFileImportDeviceIdentifierDTO deviceToSelect;
    private CompetitorWithBoatDTO compToSelect;
    private MarkDTO markToSelect;
    private BoatDTO boatToSelect;
    private boolean inInstableTransitionState = false;

    public AbstractRegattaLogFixesAddMappingsDialog(SailingServiceAsync sailingService, UserService userService,
            final ErrorReporter errorReporter, final StringMessages stringMessages, String leaderboardName,
            DialogCallback<Collection<DeviceMappingDTO>> callback) {
        super(stringMessages.add(stringMessages.deviceMappings()), stringMessages.add(stringMessages.deviceMappings()),
                stringMessages.add(), stringMessages.cancel(),
                new DataEntryDialog.Validator<Collection<DeviceMappingDTO>>() {
                    @Override
                    public String getErrorMessage(Collection<DeviceMappingDTO> valueToValidate) {
                        if (!valueToValidate.isEmpty()){
                            return null;
                        } else {
                            return stringMessages.pleaseCreateAtLeastOneMappingBy();
                        }
                    }
                }, true, callback);
        this.leaderboardName = leaderboardName;
        this.stringMessages = stringMessages;
        importWidgetHolder = new SimplePanel();
        deviceIdTable = new TrackFileImportDeviceIdentifierTableWrapper(sailingService, stringMessages, errorReporter);
        registerSelectionChangeHandler(deviceIdTable.getSelectionModel(), this::deviceSelectionChanged);
        competitorTable = new CompetitorTableWrapper<>(sailingService, userService, stringMessages, errorReporter,
                /* multiSelection */ false, /* enable pager */ true, /* filterCompetitorWithBoat */ false, /* filterCompetitorsWithoutBoat */ false);
        markTable = new MarkTableWrapper<RefreshableSingleSelectionModel<MarkDTO>>(
                /* multiSelection */ false, sailingService, stringMessages, errorReporter);
        boatTable = new BoatTableWrapper<RefreshableSingleSelectionModel<BoatDTO>>(sailingService, userService, stringMessages,
                errorReporter, /* multiSelection */ false, /* enable Pager */ true, /* allowActions */ false);

        registerSelectionChangeHandler(competitorTable.getSelectionModel(), this::mappedToSelectionChanged);
        registerSelectionChangeHandler(markTable.getSelectionModel(), this::mappedToSelectionChanged);
        registerSelectionChangeHandler(boatTable.getSelectionModel(), this::mappedToSelectionChanged);

        getBoatRegistrations(sailingService, errorReporter);
        getCompetitorRegistrations(sailingService, errorReporter);
        getMarks(sailingService, errorReporter);
    }

    private void getMarks(SailingServiceAsync sailingService, final ErrorReporter errorReporter) {
        sailingService.getMarksInRegattaLog(leaderboardName, new AsyncCallback<Iterable<MarkDTO>>() {
            @Override
            public void onSuccess(Iterable<MarkDTO> result) {
                markTable.refresh(result);
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Could not load marks: " + caught.getMessage());
            }
        });
    }

    private void getBoatRegistrations(final SailingServiceAsync sailingService, final ErrorReporter errorReporter) {
        sailingService.getBoatRegistrationsForLeaderboard(leaderboardName, new AsyncCallback<Collection<BoatDTO>>() {
            @Override
            public void onSuccess(Collection<BoatDTO> result) {
                boatTable.filterBoats(result);
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Could not load boats: " + caught.getMessage());
            }
        });
    }

    private void getCompetitorRegistrations(SailingServiceAsync sailingService, final ErrorReporter errorReporter) {
        sailingService.getCompetitorRegistrationsForLeaderboard(leaderboardName,
                new AsyncCallback<Collection<CompetitorDTO>>() {
                    @Override
                    public void onSuccess(Collection<CompetitorDTO> result) {
                        competitorTable.refreshCompetitorList(result);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Could not load competitors: " + caught.getMessage());
                    }
                });
    }

    private static <T> void selectOrClear(SingleSelectionModel<T> selectionModel, T object) {
        if (object == null) {
            selectionModel.clear();
        } else {
            selectionModel.setSelected(object, true);
        }
    }

    /**
     * Avoid programmatic deselections that re-trigger the selection listeners and lead to a loop.
     */
    private void select() {
        if (inInstableTransitionState) {
            if (deviceIdTable.getSelectionModel().getSelectedObject() == deviceToSelect
                    && competitorTable.getSelectionModel().getSelectedObject() == compToSelect
                    && boatTable.getSelectionModel().getSelectedObject() == boatToSelect
                    && markTable.getSelectionModel().getSelectedObject() == markToSelect) {
                inInstableTransitionState = false;
            }
        } else {
            inInstableTransitionState = true;
            selectOrClear(deviceIdTable.getSelectionModel(), deviceToSelect);
            selectOrClear(competitorTable.getSelectionModel(), compToSelect);
            selectOrClear(boatTable.getSelectionModel(), boatToSelect);
            selectOrClear(markTable.getSelectionModel(), markToSelect);
        }
    }

    private <T> void registerSelectionChangeHandler(SingleSelectionModel<T> selectionModel, Consumer<T> callback) {
        selectionModel.addSelectionChangeHandler(event -> callback.accept(selectionModel.getSelectedObject()));
    }

    private void mappedToSelectionChanged(MappableToDevice mappedTo) {
        if (!inInstableTransitionState) {
            deviceIdTable.setMappedObjectForSelectedDevice(mappedTo);

            if (mappedTo instanceof CompetitorWithBoatDTO) {
                compToSelect = (CompetitorWithBoatDTO) mappedTo;
                boatToSelect = null;
                markToSelect = null;
            } else if (mappedTo instanceof BoatDTO) {
                compToSelect = null;
                boatToSelect = (BoatDTO) mappedTo;
                markToSelect = null;
            } else if (mappedTo instanceof MarkDTO) {
                compToSelect = null;
                boatToSelect = null;
                markToSelect = (MarkDTO) mappedTo;
            }
        }
        select();
        validateAndUpdate();
    }

    private void deviceSelectionChanged(TrackFileImportDeviceIdentifierDTO deviceId) {
        if (!inInstableTransitionState) {
            deviceToSelect = deviceId;
            compToSelect = null;
            boatToSelect = null;
            markToSelect = null;

            if (deviceId != null) {
                final MappableToDevice mappedTo = deviceIdTable.getMappedObjectForDeviceId(deviceId);
                if (mappedTo instanceof CompetitorWithBoatDTO) {
                    compToSelect = (CompetitorWithBoatDTO) mappedTo;
                } else if (mappedTo instanceof BoatDTO) {
                    boatToSelect = (BoatDTO) mappedTo;
                } else if (mappedTo instanceof MarkDTO) {
                    markToSelect = (MarkDTO) mappedTo;
                }
            }
        }
        select();
    }

    @Override
    protected Widget getAdditionalWidget() {
        HorizontalPanel panel = new HorizontalPanel();
        VerticalPanel leftPanel = new VerticalPanel();
        VerticalPanel tablesPanel = new VerticalPanel();
        CaptionPanel marksPanel = new CaptionPanel(stringMessages.mark());
        CaptionPanel boatsPanel = new CaptionPanel(stringMessages.boat());
        CaptionPanel competitorsPanel = new CaptionPanel(stringMessages.competitor());

        leftPanel.add(importWidgetHolder);
        leftPanel.add(deviceIdTable);
        panel.add(leftPanel);
        panel.add(tablesPanel);
        tablesPanel.add(marksPanel);
        tablesPanel.add(boatsPanel);
        tablesPanel.add(competitorsPanel);

        marksPanel.setContentWidget(markTable.asWidget());
        boatsPanel.setContentWidget(boatTable.asWidget());
        competitorsPanel.setContentWidget(competitorTable.asWidget());

        return panel;
    }

    @Override
    protected Collection<DeviceMappingDTO> getResult() {
        List<DeviceMappingDTO> result = new ArrayList<>();
        for (Map.Entry<TrackFileImportDeviceIdentifierDTO, MappableToDevice> deviceEntry : deviceIdTable.getMappings().entrySet()) {
            final TrackFileImportDeviceIdentifierDTO device = deviceEntry.getKey();
            final DeviceIdentifierDTO deviceIdDto = new DeviceIdentifierDTO("FILE", device.uuidAsString);
            final MappableToDevice mappedTo = deviceEntry.getValue();
            final DeviceMappingDTO mapping = new DeviceMappingDTO(deviceIdDto, device.from, device.to, mappedTo, null);
            result.add(mapping);
        }
        return result;
    }

    protected void setImportWidget(Widget importWidget) {
        importWidgetHolder.setWidget(importWidget);
    }
}

package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
import com.sap.sailing.gwt.ui.shared.TrackFileImportDeviceIdentifierDTO;
import com.sap.sailing.gwt.ui.shared.TypedDeviceMappingDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public abstract class AbstractRegattaLogSensorDataAddMappingsDialog extends DataEntryDialog<Collection<TypedDeviceMappingDTO>> {

    private final String leaderboardName;
    private final SimplePanel importWidgetHolder;
    protected final TrackFileImportDeviceIdentifierTableWrapper deviceIdTable;
    private final BoatTableWrapper<RefreshableSingleSelectionModel<BoatDTO>> boatTable;
    private final CompetitorTableWrapper<RefreshableSingleSelectionModel<CompetitorDTO>> competitorTable;
    private final StringMessages stringMessages;

    private TrackFileImportDeviceIdentifierDTO deviceToSelect;
    private CompetitorWithBoatDTO compToSelect;
    private BoatDTO boatToSelect;
    private boolean inInstableTransitionState = false;

    public AbstractRegattaLogSensorDataAddMappingsDialog(SailingServiceAsync sailingService,
            final ErrorReporter errorReporter, final StringMessages stringMessages, String leaderboardName,
            DialogCallback<Collection<TypedDeviceMappingDTO>> callback) {
        super(stringMessages.add(stringMessages.deviceMappings()), stringMessages.add(stringMessages.deviceMappings()),
                stringMessages.add(), stringMessages.cancel(),
                new DataEntryDialog.Validator<Collection<TypedDeviceMappingDTO>>() {
                    @Override
                    public String getErrorMessage(Collection<TypedDeviceMappingDTO> valueToValidate) {
                        return valueToValidate.isEmpty() ? stringMessages.pleaseCreateAtLeastOneMappingForCompetitor() : null;
                    }
                }, true, callback);
        this.stringMessages = stringMessages;
        deviceIdTable = new TrackFileImportDeviceIdentifierTableWrapper(sailingService, stringMessages, errorReporter);
        deviceIdTable.removeTrackNameColumn();

        importWidgetHolder = new SimplePanel();
        deviceIdTable.getSelectionModel().addSelectionChangeHandler(
                event -> deviceSelectionChanged(deviceIdTable.getSelectionModel().getSelectedObject()));

        boatTable = new BoatTableWrapper<RefreshableSingleSelectionModel<BoatDTO>>(sailingService, stringMessages,
                errorReporter, /* multiSelection */ false, /* enable Pager */ true, /* allowActions */ false);
        competitorTable = new CompetitorTableWrapper<>(sailingService, stringMessages, errorReporter,
                /* multiSelection */ false, /* enablePager */ true, /* filterCompetitorWithBoat */ false, /* filterCompetitorsWithoutBoat */ false);

        boatTable.getSelectionModel().addSelectionChangeHandler(event -> {
            this.mappedToSelectionChanged(boatTable.getSelectionModel().getSelectedObject());
            validateAndUpdate();
        });
        competitorTable.getSelectionModel().addSelectionChangeHandler(event -> {
            this.mappedToSelectionChanged(competitorTable.getSelectionModel().getSelectedObject());
            validateAndUpdate();
        });

        this.leaderboardName = leaderboardName;
        getBoatRegistrations(sailingService, errorReporter);
        getCompetitorRegistrations(sailingService, errorReporter);
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

    @Override
    protected Widget getAdditionalWidget() {
        HorizontalPanel panel = new HorizontalPanel();
        VerticalPanel leftPanel = new VerticalPanel();
        VerticalPanel tablesPanel = new VerticalPanel();
        CaptionPanel competitorsPanel = new CaptionPanel(stringMessages.competitor());
        CaptionPanel boatsPanel = new CaptionPanel(stringMessages.boat());
        leftPanel.add(importWidgetHolder);
        leftPanel.add(deviceIdTable);
        panel.add(leftPanel);
        panel.add(tablesPanel);
        tablesPanel.add(boatsPanel);
        tablesPanel.add(competitorsPanel);
        boatsPanel.setContentWidget(boatTable.asWidget());
        competitorsPanel.setContentWidget(competitorTable.asWidget());
        return panel;
    }
    
    protected abstract String getSelectedImporterType();

    @Override
    protected Collection<TypedDeviceMappingDTO> getResult() {
        List<TypedDeviceMappingDTO> result = new ArrayList<>();
        String dataType = getSelectedImporterType();
        for (Map.Entry<TrackFileImportDeviceIdentifierDTO, MappableToDevice> deviceEntry : deviceIdTable.getMappings().entrySet()) {
            final TrackFileImportDeviceIdentifierDTO device = deviceEntry.getKey();
            final DeviceIdentifierDTO deviceIdDto = new DeviceIdentifierDTO("FILE", device.uuidAsString);
            final MappableToDevice mappedTo = deviceIdTable.getMappings().get(device);
            result.add(new TypedDeviceMappingDTO(deviceIdDto, device.from, device.to, mappedTo, null, dataType));
        }
        return result;
    }

    protected void setImportWidget(Widget importWidget) {
        importWidgetHolder.setWidget(importWidget);
    }

    // TODO Refactor!

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
                    && boatTable.getSelectionModel().getSelectedObject() == boatToSelect) {
                inInstableTransitionState = false;
            }
        } else {
            inInstableTransitionState = true;
            selectOrClear(deviceIdTable.getSelectionModel(), deviceToSelect);
            selectOrClear(competitorTable.getSelectionModel(), compToSelect);
            selectOrClear(boatTable.getSelectionModel(), boatToSelect);
        }
    }

    private void mappedToSelectionChanged(MappableToDevice mappedTo) {
        if (!inInstableTransitionState) {
            deviceIdTable.setMappedObjectForSelectedDevice(mappedTo);

            if (mappedTo instanceof CompetitorWithBoatDTO) {
                compToSelect = (CompetitorWithBoatDTO) mappedTo;
                boatToSelect = null;
            } else if (mappedTo instanceof BoatDTO) {
                compToSelect = null;
                boatToSelect = (BoatDTO) mappedTo;
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

            if (deviceId != null) {
                final MappableToDevice mappedTo = deviceIdTable.getMappedObjectForDeviceId(deviceId);
                if (mappedTo instanceof CompetitorWithBoatDTO) {
                    compToSelect = (CompetitorWithBoatDTO) mappedTo;
                } else if (mappedTo instanceof BoatDTO) {
                    boatToSelect = (BoatDTO) mappedTo;
                }
            }
        }
        select();
    }
}

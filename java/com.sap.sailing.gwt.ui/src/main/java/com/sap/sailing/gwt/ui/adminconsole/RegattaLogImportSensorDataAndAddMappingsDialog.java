package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.racelog.tracking.MappableToDevice;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceIdentifierDTO;
import com.sap.sailing.gwt.ui.shared.TrackFileImportDeviceIdentifierDTO;
import com.sap.sailing.gwt.ui.shared.TypedDeviceMappingDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class RegattaLogImportSensorDataAndAddMappingsDialog extends DataEntryDialog<Collection<TypedDeviceMappingDTO>> {
    private final String leaderboardName;
    private final SensorDataImportWidget importWidget;
    private final TrackFileImportDeviceIdentifierTableWrapper deviceIdTable;
    protected final CompetitorTableWrapper<RefreshableSingleSelectionModel<CompetitorDTO>> competitorTable;
    private final StringMessages stringMessages;

    private final Map<TrackFileImportDeviceIdentifierDTO, CompetitorDTO> mappings = new HashMap<>();

    private TrackFileImportDeviceIdentifierDTO deviceToSelect;
    private CompetitorDTO compToSelect;
    private boolean inInstableTransitionState = false;

    public RegattaLogImportSensorDataAndAddMappingsDialog(SailingServiceAsync sailingService,
            final ErrorReporter errorReporter, final StringMessages stringMessages, String leaderboardName,
            DialogCallback<Collection<TypedDeviceMappingDTO>> callback) {
        super(stringMessages.add(stringMessages.deviceMappings()), stringMessages.add(stringMessages.deviceMappings()),
                stringMessages.add(), stringMessages.cancel(),
                new DataEntryDialog.Validator<Collection<TypedDeviceMappingDTO>>() {
                    @Override
                    public String getErrorMessage(Collection<TypedDeviceMappingDTO> valueToValidate) {
                        return valueToValidate.isEmpty() ? "!! Please create at least one mapping by [...] !!" : null;
                    }
                }, true, callback);
        this.stringMessages = stringMessages;
        deviceIdTable = new TrackFileImportDeviceIdentifierTableWrapper(sailingService, stringMessages, errorReporter);
        importWidget = new SensorDataImportWidget(deviceIdTable, stringMessages, sailingService, errorReporter);
        deviceIdTable.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                deviceSelectionChanged(deviceIdTable.getSelectionModel().getSelectedObject());
            }
        });
        competitorTable = new CompetitorTableWrapper<>(sailingService, stringMessages, errorReporter, /* multiSelection */
                false, true);

        competitorTable.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                mappedToSelectionChanged(competitorTable.getSelectionModel().getSelectedObject());
            }
        });

        this.leaderboardName = leaderboardName;

        getCompetitorRegistrations(sailingService, errorReporter);
    }

    void getCompetitorRegistrations(SailingServiceAsync sailingService, final ErrorReporter errorReporter) {
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
                    && competitorTable.getSelectionModel().getSelectedObject() == compToSelect) {
                inInstableTransitionState = false;
            }
        } else {
            inInstableTransitionState = true;
            selectOrClear(deviceIdTable.getSelectionModel(), deviceToSelect);
            selectOrClear(competitorTable.getSelectionModel(), compToSelect);
        }
    }

    private void mappedToSelectionChanged(CompetitorDTO mappedTo) {
        if (!inInstableTransitionState) {
            TrackFileImportDeviceIdentifierDTO device = deviceIdTable.getSelectionModel().getSelectedObject();
            if (device != null) {
                mappings.put(device, mappedTo);
            }
            compToSelect = mappedTo;
        }
        select();
        validate();
    }

    private void deviceSelectionChanged(TrackFileImportDeviceIdentifierDTO deviceId) {
        if (!inInstableTransitionState) {
            deviceToSelect = deviceId;
            compToSelect = deviceId != null ? mappings.get(deviceId) : null;
        }
        select();
    }

    @Override
    protected Widget getAdditionalWidget() {
        HorizontalPanel panel = new HorizontalPanel();
        VerticalPanel leftPanel = new VerticalPanel();
        VerticalPanel tablesPanel = new VerticalPanel();
        CaptionPanel competitorsPanel = new CaptionPanel(stringMessages.competitor());
        leftPanel.add(importWidget);
        leftPanel.add(deviceIdTable);
        panel.add(leftPanel);
        panel.add(tablesPanel);
        tablesPanel.add(competitorsPanel);
        competitorsPanel.setContentWidget(competitorTable.asWidget());
        return panel;
    }

    @Override
    protected Collection<TypedDeviceMappingDTO> getResult() {
        List<TypedDeviceMappingDTO> result = new ArrayList<>();
        String dataType = importWidget.getSelectedImporterType();
        for (TrackFileImportDeviceIdentifierDTO device : mappings.keySet()) {
            DeviceIdentifierDTO deviceIdDto = new DeviceIdentifierDTO("FILE", device.uuidAsString);
            MappableToDevice mappedTo = mappings.get(device);
            result.add(new TypedDeviceMappingDTO(deviceIdDto, device.from, device.to, mappedTo, null, dataType));
        }
        return result;
    }

}

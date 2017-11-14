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
import com.sap.sailing.gwt.ui.shared.DeviceMappingDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.TrackFileImportDeviceIdentifierDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class RegattaLogImportFixesAndAddMappingsDialog extends DataEntryDialog<Collection<DeviceMappingDTO>> {
    private String leaderboardName;
    TrackFileImportWidget importWidget;
    private TrackFileImportDeviceIdentifierTableWrapper deviceIdTable;
    protected final CompetitorTableWrapper<RefreshableSingleSelectionModel<CompetitorDTO>> competitorTable;
    protected final MarkTableWrapper<RefreshableSingleSelectionModel<MarkDTO>> markTable;
    private final StringMessages stringMessages;

    private final Map<TrackFileImportDeviceIdentifierDTO, MappableToDevice> mappings = new HashMap<>();

    private TrackFileImportDeviceIdentifierDTO deviceToSelect;
    private CompetitorDTO compToSelect;
    private MarkDTO markToSelect;
    private boolean inInstableTransitionState = false;

    public RegattaLogImportFixesAndAddMappingsDialog(SailingServiceAsync sailingService,
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
        this.stringMessages = stringMessages;
        deviceIdTable = new TrackFileImportDeviceIdentifierTableWrapper(sailingService, stringMessages, errorReporter);
        importWidget = new TrackFileImportWidget(deviceIdTable, stringMessages, sailingService, errorReporter);
        deviceIdTable.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                deviceSelectionChanged(deviceIdTable.getSelectionModel().getSelectedObject());
            }
        });
        competitorTable = new CompetitorTableWrapper<>(sailingService, stringMessages, errorReporter, /* multiSelection */
                false, true, /* showOnlyCompetitorsWithBoat */ false);
        markTable = new MarkTableWrapper<RefreshableSingleSelectionModel<MarkDTO>>(
        /* multiSelection */false, sailingService, stringMessages, errorReporter);

        competitorTable.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                mappedToSelectionChanged(competitorTable.getSelectionModel().getSelectedObject());
            }
        });
        markTable.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                mappedToSelectionChanged(markTable.getSelectionModel().getSelectedObject());
            }
        });

        this.leaderboardName = leaderboardName;

        getCompetitorRegistrations(sailingService, errorReporter);
        getMarks(sailingService, errorReporter);
    }

    void getMarks(SailingServiceAsync sailingService, final ErrorReporter errorReporter) {
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
                    && competitorTable.getSelectionModel().getSelectedObject() == compToSelect
                    && markTable.getSelectionModel().getSelectedObject() == markToSelect) {
                inInstableTransitionState = false;
            }
        } else {
            inInstableTransitionState = true;
            selectOrClear(deviceIdTable.getSelectionModel(), deviceToSelect);
            selectOrClear(competitorTable.getSelectionModel(), compToSelect);
            selectOrClear(markTable.getSelectionModel(), markToSelect);
        }
    }

    private void mappedToSelectionChanged(MappableToDevice mappedTo) {
        if (!inInstableTransitionState) {
            TrackFileImportDeviceIdentifierDTO device = deviceIdTable.getSelectionModel().getSelectedObject();
            if (device != null) {
                mappings.put(device, mappedTo);
            }

            if (mappedTo instanceof CompetitorDTO) {
                markToSelect = null;
                compToSelect = (CompetitorDTO) mappedTo;
            } else {
                compToSelect = null;
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
            markToSelect = null;

            if (deviceId != null) {
                MappableToDevice mappedTo = mappings.get(deviceId);
                if (mappedTo instanceof CompetitorDTO) {
                    compToSelect = (CompetitorDTO) mappedTo;
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
        CaptionPanel competitorsPanel = new CaptionPanel(stringMessages.competitor());

        leftPanel.add(importWidget);
        leftPanel.add(deviceIdTable);
        panel.add(leftPanel);
        panel.add(tablesPanel);
        tablesPanel.add(marksPanel);
        tablesPanel.add(competitorsPanel);

        marksPanel.setContentWidget(markTable.asWidget());
        competitorsPanel.setContentWidget(competitorTable.asWidget());

        return panel;
    }

    @Override
    protected Collection<DeviceMappingDTO> getResult() {
        List<DeviceMappingDTO> result = new ArrayList<>();
        for (TrackFileImportDeviceIdentifierDTO device : mappings.keySet()) {
            DeviceIdentifierDTO deviceIdDto = new DeviceIdentifierDTO("FILE", device.uuidAsString);
            MappableToDevice mappedTo = mappings.get(device);
            DeviceMappingDTO mapping = new DeviceMappingDTO(deviceIdDto, device.from, device.to, mappedTo, null);
            result.add(mapping);
        }
        return result;
    }

}

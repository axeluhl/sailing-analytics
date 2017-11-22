package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
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

    public RegattaLogImportSensorDataAndAddMappingsDialog(SailingServiceAsync sailingService,
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

        importWidget = new SensorDataImportWidget(deviceIdTable, stringMessages, sailingService, errorReporter);
        deviceIdTable.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                CompetitorDTO mappedComp = deviceIdTable.getMappedCompetitorForCurrentSelection();
                if (mappedComp != null) {
                    competitorTable.getSelectionModel().setSelected(mappedComp, true);
                }
            }
        });
        competitorTable = new CompetitorTableWrapper<>(sailingService, stringMessages, errorReporter,
                /* multiSelection */ false, /* enablePager */ true, /* showOnlyCompetitorsWithBoat */ false);
        competitorTable.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                deviceIdTable.didSelectCompetitorForMapping(competitorTable.getSelectionModel().getSelectedObject());
                validateAndUpdate();
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
        for (TrackFileImportDeviceIdentifierDTO device : deviceIdTable.getMappings().keySet()) {
            DeviceIdentifierDTO deviceIdDto = new DeviceIdentifierDTO("FILE", device.uuidAsString);
            MappableToDevice mappedTo = deviceIdTable.getMappings().get(device);
            result.add(new TypedDeviceMappingDTO(deviceIdDto, device.from, device.to, mappedTo, null, dataType));
        }
        return result;
    }

}

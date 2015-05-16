package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.List;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceMappingDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class RegattaLogTrackingDeviceMappingsDialog extends AbstractLogTrackingDeviceMappingsDialog {
    protected String leaderboardName;

    public RegattaLogTrackingDeviceMappingsDialog(final SailingServiceAsync sailingService,
            final StringMessages stringMessages, final ErrorReporter errorReporter, final String leaderboardName) {

        super(sailingService, stringMessages, errorReporter);
        this.leaderboardName = leaderboardName;
        
        refresh();
    }

    @Override
    protected void refresh() {
        sailingService.getDeviceMappingsFromLogHierarchy(leaderboardName,
                new AsyncCallback<List<DeviceMappingDTO>>() {
                    @Override
                    public void onSuccess(List<DeviceMappingDTO> result) {
                        mappings = result;

                        updateChart();
                        deviceMappingTable.refresh(mappings);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Could not load mappings for marks: " + caught.getMessage());
                    }
                });
    }

    @Override
    protected void showAddMappingDialog(DeviceMappingDTO mapping) {
        new AddDeviceMappingToRegattaLogDialog(sailingService, errorReporter, stringMessages, leaderboardName).show();
    }

    @Override
    protected void importFixes() {
        new RegattaLogImportFixesAndAddMappingsDialog(sailingService, errorReporter, stringMessages, leaderboardName, new DataEntryDialog.DialogCallback<Collection<DeviceMappingDTO>>() {

                    @Override
                    public void ok(Collection<DeviceMappingDTO> editedObject) {
                        for (DeviceMappingDTO mapping : editedObject) {
                            sailingService.addDeviceMappingToRegattaLog(leaderboardName,
                                    mapping, new AsyncCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void result) {
                                            refresh();
                                        }

                                        @Override
                                        public void onFailure(Throwable caught) {
                                            errorReporter.reportError(caught.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void cancel() {
                    }
                }).show();
    }

    @Override
    protected FieldUpdater<DeviceMappingDTO, String> getActionColFieldUpdater() {

        return new FieldUpdater<DeviceMappingDTO, String>() {

            @Override
            public void update(int index, final DeviceMappingDTO dto, String value) {
                if (RaceLogTrackingDeviceMappingsImagesBarCell.ACTION_CLOSE.equals(value)) {
                    new SetTimePointDialog(stringMessages, stringMessages.setClosingTimePoint(),
                            new DataEntryDialog.DialogCallback<java.util.Date>() {
                                @Override
                                public void ok(java.util.Date editedObject) {
                                    sailingService.closeOpenEndedDeviceMapping(leaderboardName, dto, editedObject, new AsyncCallback<Void>() {
                                                @Override
                                                public void onSuccess(Void result) {
                                                    refresh();
                                                }

                                                @Override
                                                public void onFailure(Throwable caught) {
                                                    errorReporter.reportError("Could not close open ended mapping: "
                                                            + caught.getMessage());
                                                }
                                            });
                                }

                                @Override
                                public void cancel() {
                                }
                            }).show();
                } else if (RaceLogTrackingDeviceMappingsImagesBarCell.ACTION_REMOVE.equals(value)) {
                    sailingService.revokeRaceAndRegattaLogEvents(leaderboardName,
                            dto.originalRaceLogEventIds, new AsyncCallback<Void>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    errorReporter.reportError("Could not remove mappings: " + caught.getMessage());
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    refresh();
                                }

                            });
                }

            }
        };
    }
}
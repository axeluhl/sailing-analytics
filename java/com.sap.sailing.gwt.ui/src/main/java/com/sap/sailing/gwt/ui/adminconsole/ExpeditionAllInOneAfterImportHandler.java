package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceMappingDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.TypedDeviceMappingDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

public class ExpeditionAllInOneAfterImportHandler {
    
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    protected EventDTO event;
    private RegattaDTO regatta;
    private StrippedLeaderboardDTO leaderboard;

    public ExpeditionAllInOneAfterImportHandler(UUID eventId, String regattaName, String leaderboardName, final SailingServiceAsync sailingService, final ErrorReporter errorReporter,
            final StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
                
        sailingService.getEventById(eventId, false, new AsyncCallback<EventDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onSuccess(EventDTO result) {
                event = result;
                sailingService.getRegattaByName(regattaName, new AsyncCallback<RegattaDTO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        // TODO Auto-generated method stub
                        
                    }

                    @Override
                    public void onSuccess(RegattaDTO result) {
                        regatta = result;
                        sailingService.getLeaderboard(leaderboardName, new AsyncCallback<StrippedLeaderboardDTO>() {
                            @Override
                            public void onSuccess(StrippedLeaderboardDTO result) {
                                leaderboard = result;
                                showCompetitorRegistration();
                            }
                            
                            @Override
                            public void onFailure(Throwable caught) {
                                // TODO Auto-generated method stub
                                
                            }
                        });
                    }
                });
            }
        });
    }

    private void showCompetitorRegistration() {
        new RegattaLogCompetitorRegistrationDialog(
                regatta.boatClass == null ? null : regatta.boatClass.getName(), sailingService,
                stringMessages, errorReporter, true, leaderboard.getName(),
                new DialogCallback<Set<CompetitorDTO>>() {
            @Override
            public void ok(final Set<CompetitorDTO> competitors) {
                continueWithCompetitors(competitors);
            }

            @Override
            public void cancel() {
                // TODO Auto-generated method stub
            }}).show();
    }
    

    private final void continueWithCompetitors(final Set<CompetitorDTO> competitors) {
        if (competitors.isEmpty()) {
            Window.alert("TODO: no competitor added -> cancelling import");
        } else {
            // TODO check competitor count vs imported device ID count
            // TODO if there is exactly one competitor and one device ID, we could auto-map those
            
            final String leaderboardName = leaderboard.getName();
            new RegattaLogSensorDataAddMappingsDialog(sailingService, errorReporter, stringMessages,
                    leaderboardName, /* TODO: Device IDs */ Collections.emptySet(),
                    /* TODO: importer type */ null, new DialogCallback<Collection<TypedDeviceMappingDTO>>() {

                @Override
                public void ok(Collection<TypedDeviceMappingDTO> mappings) {
                    new AddTypedDeviceMappingsToRegattaLog(leaderboardName, mappings, () -> {
                        new RegattaLogFixesAddMappingsDialog(sailingService, errorReporter, stringMessages,
                                leaderboardName, /* TODO: Device IDs */ Collections.emptySet(),
                                new DialogCallback<Collection<DeviceMappingDTO>>() {

                            @Override
                            public void ok(Collection<DeviceMappingDTO> mappings) {
                                new AddDeviceMappingsToRegattaLog(leaderboardName, mappings, () -> {
                                    // TODO show dialog with links to event and RaceBoard
                                });
                            }

                            @Override
                            public void cancel() {
                                // TODO Auto-generated method stub
                                
                            }}).show();
                    });
                }

                @Override
                public void cancel() {
                    // TODO Auto-generated method stub
                    
                }}).show();
        }
    }
    

    private class AddTypedDeviceMappingsToRegattaLog {
        private int callCount = 0;
        private final Runnable callback;
        public AddTypedDeviceMappingsToRegattaLog(String leaderboardName, Collection<TypedDeviceMappingDTO> mappings, Runnable callback) {
            this.callback = callback;
            for (TypedDeviceMappingDTO mapping : mappings) {
                callCount++;
                sailingService.addTypedDeviceMappingToRegattaLog(leaderboardName, mapping, new AsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        callCount--;
                        runCallbackIfNoCallIsRunning();
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(caught.getMessage());
                    }
                });
            }
        }
        private void runCallbackIfNoCallIsRunning() {
            if (callCount == 0) {
                callback.run();
            }
        }
    }
    
    private class AddDeviceMappingsToRegattaLog {
        private int callCount = 0;
        private final Runnable callback;
        public AddDeviceMappingsToRegattaLog(String leaderboardName, Collection<DeviceMappingDTO> mappings, Runnable callback) {
            this.callback = callback;
            for (DeviceMappingDTO mapping : mappings) {
                callCount++;
                sailingService.addDeviceMappingToRegattaLog(leaderboardName, mapping, new AsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        callCount--;
                        runCallbackIfNoCallIsRunning();
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(caught.getMessage());
                    }
                });
            }
        }
        private void runCallbackIfNoCallIsRunning() {
            if (callCount == 0) {
                callback.run();
            }
        }
    }
}

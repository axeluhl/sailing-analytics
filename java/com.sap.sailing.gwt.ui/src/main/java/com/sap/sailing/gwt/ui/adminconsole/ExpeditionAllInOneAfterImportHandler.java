package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceMappingDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.TrackFileImportDeviceIdentifierDTO;
import com.sap.sailing.gwt.ui.shared.TypedDeviceMappingDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

public class ExpeditionAllInOneAfterImportHandler {
    
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    private final RegattaNameAndRaceName regattaAndRaceIdentifier;
    private final String leaderboardGroupName;
    private final String raceColumnName;
    private final String fleetName;
    protected EventDTO event;
    private RegattaDTO regatta;
    private StrippedLeaderboardDTO leaderboard;
    private List<TrackFileImportDeviceIdentifierDTO> gpsFixesDeviceIDs;
    private List<TrackFileImportDeviceIdentifierDTO> sensorFixesDeviceIDs;
    private final String sensorImporterType;

    public ExpeditionAllInOneAfterImportHandler(UUID eventId, String regattaName, String leaderboardName,
            String leaderboardGroupName, String raceName, String raceColumnName, String fleetName,
            List<String> gpsDeviceIds, List<String> sensorDeviceIds, String sensorImporterType,
            final SailingServiceAsync sailingService, final ErrorReporter errorReporter,
            final StringMessages stringMessages) {
        this.leaderboardGroupName = leaderboardGroupName;
        this.raceColumnName = raceColumnName;
        this.fleetName = fleetName;
        this.sensorImporterType = sensorImporterType;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.regattaAndRaceIdentifier = new RegattaNameAndRaceName(regattaName, raceName);
                
        sailingService.getEventById(eventId, false, new DataLoadingCallback<EventDTO>() {
            @Override
            public void onSuccess(EventDTO result) {
                event = result;
                sailingService.getRegattaByName(regattaName, new DataLoadingCallback<RegattaDTO>() {
                    @Override
                    public void onSuccess(RegattaDTO result) {
                        regatta = result;
                        sailingService.getLeaderboard(leaderboardName,
                                new DataLoadingCallback<StrippedLeaderboardDTO>() {
                            @Override
                            public void onSuccess(StrippedLeaderboardDTO result) {
                                leaderboard = result;
                                sailingService.getTrackFileImportDeviceIds(gpsDeviceIds,
                                    new DataLoadingCallback<List<TrackFileImportDeviceIdentifierDTO>>() {
                                        @Override
                                        public void onSuccess(List<TrackFileImportDeviceIdentifierDTO> result) {
                                            gpsFixesDeviceIDs = result;
                                            sailingService.getTrackFileImportDeviceIds(sensorDeviceIds,
                                                        new DataLoadingCallback<List<TrackFileImportDeviceIdentifierDTO>>() {
                                                @Override
                                                public void onSuccess(List<TrackFileImportDeviceIdentifierDTO> result) {
                                                    sensorFixesDeviceIDs = result;
                                                    showCompetitorRegistration();
                                                }
                                            });
                                        }
                                    });
                            }
                        });
                    }
                });
            }
        });
    }

    private void showCompetitorRegistration() {
        new RegattaLogCompetitorRegistrationDialog(regatta.boatClass == null ? null : regatta.boatClass.getName(),
                sailingService, stringMessages, errorReporter, true, leaderboard.getName(),
                new CancelImportDialogCallback<Set<CompetitorDTO>>() {
            @Override
            public void ok(final Set<CompetitorDTO> competitors) {
                if (competitors.isEmpty()) {
                    Window.alert(stringMessages.importCanceledNoCompetitorAdded());
                } else {
                    sailingService.setCompetitorRegistrationsInRegattaLog(leaderboard.getName(),
                        competitors, new AsyncCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                mapCompetitorsToGPSFixDeviceIds(competitors);
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Failed to register competitors!");
                            }
                        });
                }
            }
        }).show();
    }
    
    private void mapCompetitorsToGPSFixDeviceIds(final Set<CompetitorDTO> mappedCompetitors) {
        if (gpsFixesDeviceIDs.size() == 1 && mappedCompetitors.size() == 1) {
            // If there is exactly one device and one Competitor, the mapping is automatically added without user interaction
            final TrackFileImportDeviceIdentifierDTO deviceIdentifierDTO = gpsFixesDeviceIDs.iterator().next();
            final CompetitorDTO competitor = mappedCompetitors.iterator().next();
            saveCompetitorGPSMapping(mappedCompetitors, Collections.singleton(new DeviceMappingDTO(deviceIdentifierDTO, deviceIdentifierDTO.from, deviceIdentifierDTO.to, competitor, null)));
        } else {
            new RegattaLogFixesAddMappingsDialog(sailingService, errorReporter, stringMessages,
                    leaderboard.getName(), gpsFixesDeviceIDs,
                    new CancelImportDialogCallback<Collection<DeviceMappingDTO>>() {
                
                @Override
                public void ok(Collection<DeviceMappingDTO> mappings) {
                    saveCompetitorGPSMapping(mappedCompetitors, mappings);
                }
            }).show();
        }
    }

    private void saveCompetitorGPSMapping(final Set<CompetitorDTO> mappedCompetitors, final Collection<DeviceMappingDTO> mappings) {
        new AddDeviceMappingsToRegattaLog(leaderboard.getName(), mappings, () -> {
            mapCompetitorsToSensorFixDeviceIds(mappedCompetitors);
        });
    }

    private final void mapCompetitorsToSensorFixDeviceIds(final Set<CompetitorDTO> mappedCompetitors) {
        if (sensorFixesDeviceIDs.size() == 1 && mappedCompetitors.size() == 1) {
            // If there is exactly one device and one Competitor, the mapping is automatically added without user interaction
            final TrackFileImportDeviceIdentifierDTO deviceIdentifierDTO = sensorFixesDeviceIDs.iterator().next();
            final CompetitorDTO competitor = mappedCompetitors.iterator().next();
            saveCompetitorSensorFixMapping(Collections.singleton(new TypedDeviceMappingDTO(deviceIdentifierDTO, deviceIdentifierDTO.from, deviceIdentifierDTO.to, competitor, null, sensorImporterType)));
        } else {
            new RegattaLogSensorDataAddMappingsDialog(sailingService, errorReporter, stringMessages, leaderboard.getName(),
                    sensorFixesDeviceIDs, sensorImporterType,
                    new CancelImportDialogCallback<Collection<TypedDeviceMappingDTO>>() {
    
                @Override
                public void ok(Collection<TypedDeviceMappingDTO> mappings) {
                    saveCompetitorSensorFixMapping(mappings);
                }
            }).show();
        }
    }
    
    private void saveCompetitorSensorFixMapping(final Collection<TypedDeviceMappingDTO> mappings) {
        new AddTypedDeviceMappingsToRegattaLog(leaderboard.getName(), mappings, () -> {
            continueWithMappedDevices();
        });
    }

    private final void continueWithMappedDevices() {
        List<RegattaNameAndRaceName> racesToStopAndStartTrackingFor = new ArrayList<>();
        racesToStopAndStartTrackingFor.add(regattaAndRaceIdentifier);
        sailingService.removeAndUntrackRaces(racesToStopAndStartTrackingFor, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Failed to track race after import!");
            }

            @Override
            public void onSuccess(Void result) {
                final List<Triple<String, String, String>> leaderboardRaceColumnFleetNames = new ArrayList<>();
                leaderboardRaceColumnFleetNames.add(new Triple<>(leaderboard.name, raceColumnName, fleetName));
                sailingService.startRaceLogTracking(leaderboardRaceColumnFleetNames, /* trackWind */ true,
                        /* TODO correctWindByDeclination */ true, new AsyncCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                new ExpeditionAllInOneImportResultDialog(event.id, regatta.getName(),
                                        regattaAndRaceIdentifier.getRaceName(), leaderboard.getName(),
                                        leaderboardGroupName).show();
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError(stringMessages.errorStartingTracking(
                                        Util.toStringOrNull(leaderboardRaceColumnFleetNames), caught.getMessage()));
                            }
                        });
            }
        });
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
                        errorReporter.reportError("Failed to add device mappings!");
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
                        errorReporter.reportError("Failed to add device mappings!");
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

    private abstract class DataLoadingCallback<T> implements AsyncCallback<T> {

        @Override
        public final void onFailure(Throwable caught) {
            errorReporter.reportError("Failed loading importer data from server!");
        }
    }

    private abstract class CancelImportDialogCallback<T> implements DialogCallback<T> {

        @Override
        public final void cancel() {
            Window.alert(stringMessages.importCanceledByUser());
        }
    }
}

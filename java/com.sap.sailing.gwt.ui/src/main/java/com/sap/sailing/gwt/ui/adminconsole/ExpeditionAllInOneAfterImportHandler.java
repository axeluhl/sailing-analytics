package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.security.ui.client.UserService;

/**
 * This is the UI wizard to be executed after
 * {@link com.sap.sailing.server.gateway.trackfiles.impl.ExpeditionAllInOneImporter ExpeditionAllInOneImporter}
 * succeeded running. This provides the functionality to add new {@link com.sap.sailing.domain.base.Competitor
 * Competitors} to the {@link com.sap.sailing.domain.base.Regatta Regatta} and automatically maps the imported GPS and
 * Bravo tracks to a selected {@link com.sap.sailing.domain.base.Competitor Competitor}. In addition, the
 * {@link com.sap.sailing.domain.tracking.TrackedRace TrackedRace} is retracked after the process to ensure that newly
 * added competitors are correctly visible afterwards. After the import finished successfully, a dialog is provided to
 * the user that shows links to the event page and race board.
 */
public class ExpeditionAllInOneAfterImportHandler {
    
    private final SailingServiceAsync sailingService;
    private final UserService userService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    private final String leaderboardGroupName;
    protected EventDTO event;
    private RegattaDTO regatta;
    private StrippedLeaderboardDTO leaderboard;
    private List<TrackFileImportDeviceIdentifierDTO> gpsFixesDeviceIDs;
    private List<TrackFileImportDeviceIdentifierDTO> sensorFixesDeviceIDs;
    private final String sensorImporterType;
    private List<Triple<String, String, String>> raceEntries;
    private String regattaName;
    @SuppressWarnings("unused") // the following could become useful in order to show the start times used for a split
    private Iterable<TimePoint> startTimes;

    public ExpeditionAllInOneAfterImportHandler(UUID eventId, String regattaName, String leaderboardName,
            String leaderboardGroupName, List<Triple<String,  String, String>> raceEntries,
            List<String> gpsDeviceIds, List<String> sensorDeviceIds, String sensorImporterType,
            Iterable<TimePoint> startTimes, final SailingServiceAsync sailingService, final UserService userService, final ErrorReporter errorReporter,
            final StringMessages stringMessages) {
        this.leaderboardGroupName = leaderboardGroupName;
        this.sensorImporterType = sensorImporterType;
        this.sailingService = sailingService;
        this.userService = userService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.raceEntries = raceEntries;
        this.regattaName = regattaName;
        this.startTimes = startTimes;
        // TODO from the start times, suggest the user to split the session into one session per start, with start tracking at n minutes before start
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
    
    private class RegattaLogCompetitorRegistrationAndSelectionDialog extends RegattaLogCompetitorRegistrationDialog {
        public RegattaLogCompetitorRegistrationAndSelectionDialog(String boatClass, SailingServiceAsync sailingService, final UserService userService,
                StringMessages stringMessages, ErrorReporter errorReporter, boolean editable, String leaderboardName,
                boolean canBoatsOfCompetitorsChangePerRace) {
            this(boatClass, sailingService, userService, stringMessages, errorReporter, editable, leaderboardName,
                    canBoatsOfCompetitorsChangePerRace, new ValidatorForCompetitorRegistrationDialog(stringMessages),
                    new CallbackForCompetitorRegistrationDialog());
        }
        
        public RegattaLogCompetitorRegistrationAndSelectionDialog(String boatClass, SailingServiceAsync sailingService, final UserService userService,
                StringMessages stringMessages, ErrorReporter errorReporter, boolean editable, String leaderboardName,
                boolean canBoatsOfCompetitorsChangePerRace, ValidatorForCompetitorRegistrationDialog validator,
                CallbackForCompetitorRegistrationDialog callback) {
            super(boatClass, sailingService, userService, stringMessages, errorReporter, editable, leaderboardName,
                    canBoatsOfCompetitorsChangePerRace, validator, callback);
            validator.setCompetitorRegistrationsPanel(competitorRegistrationsPanel);
            callback.setCompetitorRegistrationsPanel(competitorRegistrationsPanel);
        }
    }
    
    private static class ValidatorForCompetitorRegistrationDialog implements Validator<Set<CompetitorDTO>> {
        private CompetitorRegistrationsPanel competitorRegistrationsPanel;
        private StringMessages stringMessages;
        public ValidatorForCompetitorRegistrationDialog(StringMessages stringMessages) {
            this.stringMessages = stringMessages;
        }
        @Override
        public String getErrorMessage(Set<CompetitorDTO> valueToValidate) {
            if (competitorRegistrationsPanel == null || competitorRegistrationsPanel.getSelectedRegisteredCompetitors().size() != 1) {
                return stringMessages.selectOneCompetitorToMapTheImportedData();
            }
            return null;
        }
        public void setCompetitorRegistrationsPanel(CompetitorRegistrationsPanel competitorRegistrationsPanel) {
            this.competitorRegistrationsPanel = competitorRegistrationsPanel;
        }
    }
    
    private class CallbackForCompetitorRegistrationDialog extends CancelImportDialogCallback<Set<CompetitorDTO>> {
        private CompetitorRegistrationsPanel competitorRegistrationsPanel;
        
        @Override
        public void ok(Set<CompetitorDTO> competitors) {
            if (competitors.isEmpty()) {
                Notification.notify(stringMessages.importCanceledNoCompetitorAdded(), NotificationType.ERROR);
            } else {
                sailingService.setCompetitorRegistrationsInRegattaLog(leaderboard.getName(),
                    competitors, new AsyncCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                                mapCompetitorsToGPSFixDeviceIds(competitorRegistrationsPanel == null ? competitors
                                        : competitorRegistrationsPanel.getSelectedRegisteredCompetitors());
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError("Failed to register competitors!");
                        }
                    });
            }
        }
        public void setCompetitorRegistrationsPanel(CompetitorRegistrationsPanel competitorRegistrationsPanel) {
            this.competitorRegistrationsPanel = competitorRegistrationsPanel;
        }
    }

    private void showCompetitorRegistration() {
        new RegattaLogCompetitorRegistrationAndSelectionDialog(regatta.boatClass == null ? null : regatta.boatClass.getName(),
                sailingService, userService, stringMessages, errorReporter, true, leaderboard.getName(),
                leaderboard.canBoatsOfCompetitorsChangePerRace).show();
    }
    
    private void mapCompetitorsToGPSFixDeviceIds(final Set<CompetitorDTO> mappedCompetitors) {
        if (gpsFixesDeviceIDs.size() == 1 && mappedCompetitors.size() == 1) {
            // If there is exactly one device and one Competitor, the mapping is automatically added without user interaction
            final TrackFileImportDeviceIdentifierDTO deviceIdentifierDTO = gpsFixesDeviceIDs.iterator().next();
            final CompetitorDTO competitor = mappedCompetitors.iterator().next();
            saveCompetitorGPSMapping(mappedCompetitors, Collections.singleton(new DeviceMappingDTO(deviceIdentifierDTO, deviceIdentifierDTO.from, deviceIdentifierDTO.to, competitor, null)));
        } else {
            new RegattaLogFixesAddMappingsDialog(sailingService, userService, errorReporter, stringMessages,
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
        } else if (sensorFixesDeviceIDs.size() > 0) {
            new RegattaLogSensorDataAddMappingsDialog(sailingService, userService, errorReporter, stringMessages, leaderboard.getName(),
                    sensorFixesDeviceIDs, sensorImporterType,
                    new CancelImportDialogCallback<Collection<TypedDeviceMappingDTO>>() {
    
                @Override
                public void ok(Collection<TypedDeviceMappingDTO> mappings) {
                    saveCompetitorSensorFixMapping(mappings);
                }
            }).show();
        } else {
            // there can be zero sensor fix devices -> skipping the mapping step
            continueWithMappedDevices();
        }
    }
    
    private void saveCompetitorSensorFixMapping(final Collection<TypedDeviceMappingDTO> mappings) {
        new AddTypedDeviceMappingsToRegattaLog(leaderboard.getName(), mappings, () -> {
            continueWithMappedDevices();
        });
    }

    private final void continueWithMappedDevices() {
        List<RegattaNameAndRaceName> racesToStopAndStartTrackingFor = new ArrayList<>();
        final List<Triple<String, String, String>> leaderboardRaceColumnFleetNames = new ArrayList<>();
        final List<Pair<String, String>> raceNames = new ArrayList<>();
        for(Triple<String, String, String> race:raceEntries) {
            String raceName = race.getA();
            String raceColumnName = race.getB();
            String fleetName= race.getC();
            racesToStopAndStartTrackingFor.add(new RegattaNameAndRaceName(regattaName, raceName));
            leaderboardRaceColumnFleetNames.add(new Triple<>(leaderboard.getName(), raceColumnName, fleetName));
            raceNames.add(new Pair<String, String>(raceName, raceColumnName));
        }
        
        sailingService.removeAndUntrackRaces(racesToStopAndStartTrackingFor, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Failed to track race after import!");
            }

            @Override
            public void onSuccess(Void result) {
                sailingService.startRaceLogTracking(leaderboardRaceColumnFleetNames, /* trackWind */ false,
                        /* correctWindByDeclination */ true, new AsyncCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                new ExpeditionAllInOneImportResultDialog(event.id, regatta.getName(),
                                        raceNames, leaderboard.getName(),
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
            Notification.notify(stringMessages.importCanceledByUser(), NotificationType.WARNING);
        }
    }
}

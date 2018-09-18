package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.dto.RaceColumnInSeriesDTO;
import com.sap.sailing.domain.common.dto.RegattaCreationParametersDTO;
import com.sap.sailing.domain.common.dto.SeriesCreationParametersDTO;
import com.sap.sailing.gwt.ui.client.EventsRefresher;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

public class CreateRegattaCallback implements DialogCallback<RegattaDTO>{

    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final EventsRefresher eventsRefresher;
    private final RegattaRefresher regattaRefresher;
    private final StringMessages stringMessages;
    private final List<EventDTO> existingEvents;

    public CreateRegattaCallback(SailingServiceAsync sailingService, StringMessages stringMessages, ErrorReporter errorReporter, RegattaRefresher regattaRefresher, EventsRefresher eventsRefresher, List<EventDTO> existingEvents) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.regattaRefresher = regattaRefresher;
        this.eventsRefresher = eventsRefresher;
        this.stringMessages = stringMessages;
        this.existingEvents = existingEvents;
    }
    
    @Override
    public void ok(RegattaDTO newRegatta) {
        createNewRegatta(newRegatta, existingEvents);
    }

    @Override
    public void cancel() {
    }

    private void createNewRegatta(final RegattaDTO newRegatta, final List<EventDTO> existingEvents) {
        LinkedHashMap<String, SeriesCreationParametersDTO> seriesStructure = new LinkedHashMap<String, SeriesCreationParametersDTO>();
        for (SeriesDTO seriesDTO : newRegatta.series) {
            SeriesCreationParametersDTO seriesPair = new SeriesCreationParametersDTO(seriesDTO.getFleets(),
                    seriesDTO.isMedal(), seriesDTO.isFleetsCanRunInParallel(), seriesDTO.isStartsWithZeroScore(),
                    seriesDTO.isFirstColumnIsNonDiscardableCarryForward(), seriesDTO.getDiscardThresholds(),
                    seriesDTO.hasSplitFleetContiguousScoring(), seriesDTO.getMaximumNumberOfDiscards());
            seriesStructure.put(seriesDTO.getName(), seriesPair);
        }
        sailingService.createRegatta(newRegatta.getName(), newRegatta.boatClass==null?null:newRegatta.boatClass.getName(),
                newRegatta.canBoatsOfCompetitorsChangePerRace, newRegatta.canCompetitorsRegisterToOpenRegatta, newRegatta.startDate, newRegatta.endDate, 
                new RegattaCreationParametersDTO(seriesStructure), true,
                newRegatta.scoringScheme, newRegatta.defaultCourseAreaUuid, newRegatta.buoyZoneRadiusInHullLengths, newRegatta.useStartTimeInference,
                newRegatta.controlTrackingFromStartAndFinishTimes,
                newRegatta.rankingMetricType, new AsyncCallback<RegattaDTO>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to create new regatta " + newRegatta.getName() + ": " + t.getMessage());
            }

            @Override
            public void onSuccess(RegattaDTO regatta) {
                // if regatta creation was successful, add race columns as modeled in the creation dialog;
                // note that the SeriesCreationParametersDTO don't describe race columns.
                createDefaultRacesIfDefaultSeriesIsPresent(newRegatta);
                fillRegattas();
                fillEvents(); // events have their associated regattas
                openCreateDefaultRegattaLeaderboardDialog(regatta, existingEvents);
            }
        });
    }

    private void createDefaultRacesIfDefaultSeriesIsPresent(final RegattaDTO newRegatta) {
        for (final SeriesDTO series: newRegatta.series) {
            if (series.getName().equals(Series.DEFAULT_NAME) && !series.getRaceColumns().isEmpty()) {
                final List<Pair<String, Integer>> raceColumnNamesToAddWithInsertIndex = new ArrayList<>();
                for (RaceColumnDTO newRaceColumn : series.getRaceColumns()) {
                    // We could use an index counter here because we're assuming that we're creating
                    // races starting at index 0. However, to make things concurrency-safe, we have to
                    // assume that while the regatta already exists on the server, some other activity
                    // may already have started to create races for it. Better safe than sorry, append
                    // at the end, using -1 as "insertIndex."
                    raceColumnNamesToAddWithInsertIndex.add(new Pair<>(newRaceColumn.getName(), -1));
                }
                sailingService.addRaceColumnsToSeries(newRegatta.getRegattaIdentifier(), series.getName(), raceColumnNamesToAddWithInsertIndex,
                        new AsyncCallback<List<RaceColumnInSeriesDTO>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error trying to add race columns " + raceColumnNamesToAddWithInsertIndex
                                + " to series " + series.getName() + ": " + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(List<RaceColumnInSeriesDTO> raceColumns) {
                        fillRegattas();
                    }
                });
            }
        }
    }
    
    private void fillRegattas() {
        if (regattaRefresher != null){
            regattaRefresher.fillRegattas();
        }
    }
    
    private void fillEvents() {
        if (eventsRefresher != null) {
            eventsRefresher.fillEvents();
        }
    }
    
    private void openCreateDefaultRegattaLeaderboardDialog(final RegattaDTO newRegatta, final List<EventDTO> existingEvents) {
        CreateDefaultRegattaLeaderboardDialog dialog = new CreateDefaultRegattaLeaderboardDialog(sailingService, stringMessages, errorReporter, newRegatta, new DialogCallback<RegattaIdentifier>() {
            @Override
            public void ok(RegattaIdentifier regattaIdentifier) {
                sailingService.createRegattaLeaderboard(regattaIdentifier, /* displayName */ null, new int[]{},
                        new AsyncCallback<StrippedLeaderboardDTO>() {
                    @Override
                    public void onFailure(Throwable t) {
                        errorReporter.reportError("Error trying to create default regatta leaderboard for " + newRegatta.getName()
                                + ": " + t.getMessage());
                    }

                    @Override
                    public void onSuccess(StrippedLeaderboardDTO result) {
                        if (newRegatta.defaultCourseAreaUuid != null) {
                            // Show the event's leaderboard groups and allow the user to pick one to assign the regatta leaderboard to
                            final EventDTO event = getEventForCourseArea(existingEvents, newRegatta.defaultCourseAreaUuid);
                            if (!event.getLeaderboardGroups().isEmpty()) {
                                openRegattaLeaderboardToLeaderboardGroupOfEventLinkingDialog(result, event);
                            }
                        }
                    }

                });
            }

            @Override
            public void cancel() {
            }
        });
        dialog.ensureDebugId("CreateDefaultRegattaLeaderboardDialog");
        dialog.show();
    }
    
    
    /**
     * When a new regatta with a new regatta leaderboard has been created, the user will now be given the chance to link
     * the regatta leaderboard into a leaderboard group of the event out of which the regatta chose its default course area.
     * 
     * @param newRegattaLeaderboard the new regatta leaderboard that the user may link now to a leaderboard group of an event
     * @param eventToLinkRegattaTo an event that has at least one {@link EventDTO#getLeaderboardGroups() leaderboard group}
     */
    private void openRegattaLeaderboardToLeaderboardGroupOfEventLinkingDialog(final StrippedLeaderboardDTO newRegattaLeaderboard, EventDTO eventToLinkRegattaTo) {
        LinkRegattaLeaderboardToLeaderboardGroupOfEventDialog dialog = new LinkRegattaLeaderboardToLeaderboardGroupOfEventDialog(sailingService, stringMessages, errorReporter, newRegattaLeaderboard, eventToLinkRegattaTo,
                new DialogCallback<LeaderboardGroupDTO>() {
                    @Override
                    public void ok(final LeaderboardGroupDTO selectedLeaderboardGroup) {
                        final List<String> leaderboardNames = new ArrayList<>();
                        for (StrippedLeaderboardDTO leaderboard : selectedLeaderboardGroup.getLeaderboards()) {
                            leaderboardNames.add(leaderboard.name);
                        }
                        leaderboardNames.add(newRegattaLeaderboard.name);
                        sailingService.updateLeaderboardGroup(selectedLeaderboardGroup.getName(), selectedLeaderboardGroup.getName(), selectedLeaderboardGroup.description,
                                selectedLeaderboardGroup.getDisplayName(), leaderboardNames, selectedLeaderboardGroup.getOverallLeaderboardDiscardThresholds(),
                                selectedLeaderboardGroup.getOverallLeaderboardScoringSchemeType(), new MarkedAsyncCallback<Void>(new AsyncCallback<Void>() {
                                    @Override
                                    public void onFailure(Throwable caught) {
                                        errorReporter.reportError(stringMessages.failedToLinkLeaderboardToLeaderboardGroup(newRegattaLeaderboard.name, selectedLeaderboardGroup.getName()));
                                    }

                                    @Override
                                    public void onSuccess(Void result) {
                                    }
                                }));
                    }

                    @Override
                    public void cancel() {
                    }
        });
        dialog.ensureDebugId("LinkRegattaLeaderboardToLeaderboardGroupOfEventDialog");
        dialog.show();
    }

    private EventDTO getEventForCourseArea(final List<EventDTO> existingEvents, final UUID courseAreaId) {
        EventDTO result = null;
        eventLoop:
        for (final EventDTO event : existingEvents) {
            if (event.venue != null) {
                for (CourseAreaDTO courseArea : event.venue.getCourseAreas()) {
                    if (courseArea.id.equals(courseAreaId)) {
                        result = event;
                        break eventLoop;
                    }
                }
            }
        }
        return result;
    }
}

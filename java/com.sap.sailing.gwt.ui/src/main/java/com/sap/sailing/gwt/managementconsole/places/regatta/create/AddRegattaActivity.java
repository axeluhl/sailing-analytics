package com.sap.sailing.gwt.managementconsole.places.regatta.create;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.RankingMetrics;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.CourseAreaDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.dto.RaceColumnInSeriesDTO;
import com.sap.sailing.domain.common.dto.RegattaCreationParametersDTO;
import com.sap.sailing.domain.common.dto.SeriesCreationParametersDTO;
import com.sap.sailing.gwt.managementconsole.app.ManagementConsoleClientFactory;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsoleActivity;
import com.sap.sailing.gwt.managementconsole.places.regatta.overview.RegattaOverviewPlace;
import com.sap.sailing.gwt.ui.adminconsole.LeaderboardGroupDialog.LeaderboardGroupDescriptor;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTOWithSecurity;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;

public class AddRegattaActivity extends AbstractManagementConsoleActivity<AddRegattaPlace> {

    private final UUID eventId;
    private AddRegattaView addRegattaView;
    
    public AddRegattaActivity(final ManagementConsoleClientFactory clientFactory, final AddRegattaPlace place) {
        super(clientFactory, place);
        this.eventId = place.getEventId();
    }
    
    @Override
    public void start(final AcceptsOneWidget container, final EventBus eventBus) {
        addRegattaView = new AddRegattaViewImpl();
        new AddRegattaViewPresenter(addRegattaView);
        container.setWidget(addRegattaView);
    }
   
    private class AddRegattaViewPresenter implements AddRegattaView.Presenter {

        public AddRegattaViewPresenter(AddRegattaView addRegattaView) {
            addRegattaView.setPresenter(this);
        }

        @Override
        public void addRegatta(String regattaName, String boatClassName, RankingMetrics ranking, Integer racesCount, ScoringSchemeType scoringSystem) {
            
            getClientFactory().getSailingService().getEventById(eventId, false, new AsyncCallback<EventDTO>() {
            
                @Override
                public final void onFailure(Throwable t) {
                    throw new RuntimeException(t);
                }
                
                @Override
                public void onSuccess(EventDTO event) { 
                    SeriesDTO series = new SeriesDTO();
                    series.setName(Series.DEFAULT_NAME);
                    series.setMedal(false);
                    series.setStartsWithZeroScore(false);
                    series.setSplitFleetContiguousScoring(false);
                    series.setFirstColumnIsNonDiscardableCarryForward(false);
                    series.setFleets(Collections.singletonList(new FleetDTO(LeaderboardNameConstants.DEFAULT_FLEET_NAME, 0, null)));
                    List<RaceColumnDTO> races = new ArrayList<RaceColumnDTO>();
                    for (int i = 1; i <= racesCount; i++) {
                        RaceColumnDTO raceColumnDTO = new RaceColumnInSeriesDTO(series.getName(), regattaName);
                        raceColumnDTO.setName("R"+i);
                        races.add(raceColumnDTO);
                    }
                    series.setRaceColumns(races);               
                   
                    RegattaDTO regattaDTO = new RegattaDTO();
                    regattaDTO.series = Collections.singletonList(series);
                    //regattaDTO.startDate = startDateBox.getValue();
                    //regattaDTO.endDate = endDateBox.getValue();
                    regattaDTO.scoringScheme = scoringSystem;
                    regattaDTO.useStartTimeInference = false; //useStartTimeInferenceCheckBox.getValue();
                    regattaDTO.controlTrackingFromStartAndFinishTimes = false; //controlTrackingFromStartAndFinishTimesCheckBox.getValue();
                    regattaDTO.autoRestartTrackingUponCompetitorSetChange = false; //autoRestartTrackingUponCompetitorSetChangeCheckBox.getValue();
                    regattaDTO.buoyZoneRadiusInHullLengths = Regatta.DEFAULT_BUOY_ZONE_RADIUS_IN_HULL_LENGTHS;
                    
                    //regattaDTO.competitorRegistrationType = CompetitorRegistrationType.valueOf(competitorRegistrationTypeListBox.getSelectedValue());
                    //regattaDTO.registrationLinkSecret = registrationLinkWithQRCode.getSecret();       

                    regattaDTO.setName(regattaName); // trim to particularly avoid trailing blanks
                    regattaDTO.boatClass = new BoatClassDTO(boatClassName, Distance.NULL, Distance.NULL);
                    regattaDTO.canBoatsOfCompetitorsChangePerRace = false;
                    regattaDTO.rankingMetricType = ranking;
                    
                    regattaDTO.courseAreas = new ArrayList<>();
                    Util.addAll(event.venue.getCourseAreas(), regattaDTO.courseAreas);
                    
                    createNewRegatta(event, regattaDTO, new ArrayList<EventDTO>(), racesCount);
                }
          
            });
        }
        
    
        
        @Override
        public void cancelAddRegatta() {
            getClientFactory().getPlaceController().goTo(new RegattaOverviewPlace(eventId));
        }            
        
        private void createNewRegatta(final EventDTO event, final RegattaDTO newRegatta, final List<EventDTO> existingEvents, Integer racesCount) {
            LinkedHashMap<String, SeriesCreationParametersDTO> seriesStructure = new LinkedHashMap<String, SeriesCreationParametersDTO>();
            for (SeriesDTO seriesDTO : newRegatta.series) {
                SeriesCreationParametersDTO seriesPair = new SeriesCreationParametersDTO(seriesDTO.getFleets(),
                        seriesDTO.isMedal(), seriesDTO.isFleetsCanRunInParallel(), seriesDTO.isStartsWithZeroScore(),
                        seriesDTO.isFirstColumnIsNonDiscardableCarryForward(), seriesDTO.getDiscardThresholds(),
                        seriesDTO.hasSplitFleetContiguousScoring(), seriesDTO.getMaximumNumberOfDiscards());
                seriesStructure.put(seriesDTO.getName(), seriesPair);
            }
            
            getClientFactory().getSailingService().createRegatta(newRegatta.getName(),
                    newRegatta.boatClass == null ? null : newRegatta.boatClass.getName(),
                    newRegatta.canBoatsOfCompetitorsChangePerRace, newRegatta.competitorRegistrationType,
                    newRegatta.registrationLinkSecret, newRegatta.startDate, newRegatta.endDate,
                    new RegattaCreationParametersDTO(seriesStructure), true, newRegatta.scoringScheme,
                    Util.mapToArrayList(newRegatta.courseAreas, CourseAreaDTO::getId), newRegatta.buoyZoneRadiusInHullLengths,
                    newRegatta.useStartTimeInference, newRegatta.controlTrackingFromStartAndFinishTimes,
                    newRegatta.autoRestartTrackingUponCompetitorSetChange, newRegatta.rankingMetricType,
                    new AsyncCallback<RegattaDTO>() {
                @Override
                public void onFailure(Throwable t) {
                    throw new RuntimeException(t);
                }

                @Override
                public void onSuccess(RegattaDTO regatta) {
                    //createDefaultRacesIfDefaultSeriesIsPresent(event, regatta);
                    createRegattaLeaderboard(event, newRegatta, racesCount);
                    // TODO check regatta name for uniqueness
                }
            });
        }
        
        private void createRegattaLeaderboard(final EventDTO event, final RegattaDTO newRegatta, Integer racesCount) {
            RegattaIdentifier regattaIdentifier = newRegatta.getRegattaIdentifier();
            getClientFactory().getSailingService().createRegattaLeaderboard((RegattaName)regattaIdentifier,
                    /* displayName */ null, new int[] {},
                    new AsyncCallback<StrippedLeaderboardDTOWithSecurity>() {
            @Override
            public void onFailure(Throwable t) {
                throw new RuntimeException(t);
            }
    
            @Override
            public void onSuccess(StrippedLeaderboardDTOWithSecurity result) {
                if (!newRegatta.courseAreas.isEmpty()) {
                    addRegattaToRegattaLeaderboard(result, event, newRegatta, racesCount);
                }    
            }
        });
        }
        
        private void addRegattaToRegattaLeaderboard(StrippedLeaderboardDTOWithSecurity newRegattaLeaderboard, EventDTO event, final RegattaDTO newRegatta, Integer racesCount) {
            List<LeaderboardGroupDTO> leaderboardGroups = event.getLeaderboardGroups();
            if (leaderboardGroups == null || leaderboardGroups.isEmpty()) {
                createLeaderboardGroupAndAddRegattaToLeaderboard(newRegattaLeaderboard, event, newRegatta, racesCount);
            } else {
                addRegattaToRegattaLeaderboard(newRegattaLeaderboard, event.getLeaderboardGroups().get(0), event, newRegatta, racesCount);
            }
        }
        
        private void createLeaderboardGroupAndAddRegattaToLeaderboard(StrippedLeaderboardDTOWithSecurity newRegattaLeaderboard, EventDTO event, final RegattaDTO newRegatta, Integer racesCount) {
            String leaderboardName = "Leaderboard" + System.currentTimeMillis();
            LeaderboardGroupDescriptor newGroup = new LeaderboardGroupDescriptor(leaderboardName, leaderboardName, leaderboardName,
                    false, false, new int[0] , null);
            getClientFactory().getSailingService().createLeaderboardGroup(newGroup.getName(), newGroup.getDescription(),
                    newGroup.getDisplayName(), newGroup.isDisplayLeaderboardsInReverseOrder(),
                    newGroup.getOverallLeaderboardDiscardThresholds(), newGroup.getOverallLeaderboardScoringSchemeType(), new MarkedAsyncCallback<LeaderboardGroupDTO>(
                            new AsyncCallback<LeaderboardGroupDTO>() {
                                @Override
                                public void onFailure(Throwable t) {
                                    throw new RuntimeException(t);
                                }
                                @Override
                                public void onSuccess(LeaderboardGroupDTO newGroup) {
                                    event.addLeaderboardGroup(newGroup);
                                    updateEvent(newRegattaLeaderboard, event, newRegatta, racesCount);
                                }
                            }));
            
        }
        
        private void updateEvent(StrippedLeaderboardDTOWithSecurity newRegattaLeaderboard, EventDTO event, final RegattaDTO newRegatta, Integer racesCount) {
            getClientFactory().getSailingService().updateEvent(event.id, event.getName(), event.getDescription(),
                    event.startDate, event.endDate, event.venue, event.isPublic,
                    event.getLeaderboardGroupIds(), event.getOfficialWebsiteURL(), event.getBaseURL(),
                    event.getSailorsInfoWebsiteURLs(), event.getImages(), event.getVideos(),
                    event.getWindFinderReviewedSpotsCollectionIds(), new AsyncCallback<EventDTO>() {
                        @Override
                        public void onFailure(Throwable t) {
                            throw new RuntimeException(t);
                        }   
                        @Override
                        public void onSuccess(EventDTO newEvent) {      
                            LeaderboardGroupDTO newGroup = newEvent.getLeaderboardGroups().get(0);
                            addRegattaToRegattaLeaderboard(newRegattaLeaderboard, newGroup, newEvent, newRegatta, racesCount);
                        }
                    });
                        
        }

        private void addRegattaToRegattaLeaderboard(StrippedLeaderboardDTOWithSecurity newRegattaLeaderboard, LeaderboardGroupDTO leaderboardGroup, EventDTO event,
                final RegattaDTO newRegatta, Integer racesCount) {
            final List<String> leaderboardNames = new ArrayList<>();
            for (StrippedLeaderboardDTO leaderboard : leaderboardGroup.getLeaderboards()) {
                leaderboardNames.add(leaderboard.getName());
            }
            leaderboardNames.add(newRegattaLeaderboard.getName());
            getClientFactory().getSailingService().updateLeaderboardGroup(leaderboardGroup.getId(),
                    leaderboardGroup.getName(), leaderboardGroup.getName(),
                    leaderboardGroup.getDescription(), leaderboardGroup.getDisplayName(),
                    leaderboardNames, leaderboardGroup.getOverallLeaderboardDiscardThresholds(),
                    leaderboardGroup.getOverallLeaderboardScoringSchemeType(),
                    new MarkedAsyncCallback<Void>(new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable t) {
                            throw new RuntimeException(t);
                        }

                        @Override
                        public void onSuccess(Void result) {
                            createDefaultRacesIfDefaultSeriesIsPresent(newRegatta);
                        }
                   }));
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
            
                    getClientFactory().getSailingService().addRaceColumnsToSeries(newRegatta.getRegattaIdentifier(), series.getName(), raceColumnNamesToAddWithInsertIndex,
                            new AsyncCallback<List<RaceColumnInSeriesDTO>>() {
                        @Override
                        public void onFailure(Throwable t) {
                            throw new RuntimeException(t);
                        }
    
                        @Override
                        public void onSuccess(List<RaceColumnInSeriesDTO> raceColumns) {
                            updateSeries(newRegatta, series);
                            getClientFactory().getPlaceController().goTo(new RegattaOverviewPlace(eventId));
                        }
                    });     
                }
            }    
        }
        
        private void updateSeries(final RegattaDTO newRegatta, final SeriesDTO series) {
            getClientFactory().getSailingService().updateSeries(newRegatta.getRegattaIdentifier(), series.getName(), series.getName(),
                    series.isMedal(), series.isFleetsCanRunInParallel(), series.getDiscardThresholds(),
                series.isStartsWithZeroScore(),
                series.isFirstColumnIsNonDiscardableCarryForward(),
                series.hasSplitFleetContiguousScoring(), series.getMaximumNumberOfDiscards(),
                series.getFleets(), new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable t) {
                        throw new RuntimeException(t);
                    }

                    @Override
                    public void onSuccess(Void result) {
                        getClientFactory().getPlaceController().goTo(new RegattaOverviewPlace(eventId));
                    }
                });
        }
    }

    
    
}

package com.sap.sailing.gwt.home.communication.user.profile.sailorprofile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileNumericStatisticType;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileNumericStatisticType.StatisticType;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileStatisticDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileStatisticDTO.SingleEntry;
import com.sap.sailing.server.preferences.SailorProfilePreference;
import com.sap.sailing.server.preferences.SailorProfilePreferences;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.settings.GwtIncompatible;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

// added 
//import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
//import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.MaxPointsReason;


/**
 * {@link SailingAction} implementation to load all events, the competitors in a sailor profile with a specific uuid for
 * the currently logged in user have participated in to be shown on the sailor profile details page in the events
 * container.
 */
public class GetNumericStatisticForSailorProfileAction
        implements SailingAction<SailorProfileStatisticDTO>, SailorProfileConverter {

    private SailorProfileNumericStatisticType type;
    private UUID uuid;
    
    //added
//    private static final boolean INCLUDE_COMPETITOR_IN_FIELD_AVERAGE = false; //TODO

    public GetNumericStatisticForSailorProfileAction(UUID uuid, SailorProfileNumericStatisticType type) {
        this.uuid = uuid;
        this.type = type;
    }

    /**
     * GWT serialization only
     */
    @SuppressWarnings("unused")
    private GetNumericStatisticForSailorProfileAction() {
    }

    @Override
    @GwtIncompatible
    public SailorProfileStatisticDTO execute(SailingDispatchContext ctx) throws DispatchException {
        final Map<SimpleCompetitorWithIdDTO, ArrayList<SingleEntry>> result = new HashMap<>(); // Map with sailor and statistic values
//        final Map<SimpleCompetitorWithIdDTO, ArrayList<SingleEntry>> aggregatesForOtherCompetitors = new HashMap<>();
        final Map<SimpleCompetitorWithIdDTO, ArrayList<SingleEntry>> aggregateForOtherCompetitors = new HashMap<>(); // aggregate wether than aggregates
        // added globalAggregator
        CompetitorAndBoatStore store = ctx.getRacingEventService().getCompetitorAndBoatStore(); // Get available sailors and boats
        SailorProfilePreferences prefs = ctx.getPreferenceForCurrentUser(SailorProfilePreferences.PREF_NAME); // List of the user's sailor profiles
        SailorProfilePreference pref = findSailorProfile(store, prefs); // Select a specific profile
        for (Competitor competitor : pref.getCompetitors()) { // prefs is the selected sailor profile // // getCompetitors returns all sailors stored in this profile
            final Aggregator aggregator = determineAggregator(); // new aggregator for each competitor
            final Aggregator aggregatorForOtherCompetitors = determineAggregator(); // new aggregator for other competitors
            if (aggregator == null) {
                continue;
            }
            for (Event event : ctx.getRacingEventService().getAllEvents()) {
                // determine end of event, or now in live case
                TimePoint end = event.getEndDate();
                if (end == null) {
                    end = MillisecondsTimePoint.now();
                }
                for (LeaderboardGroup leaderboardGroup : event.getLeaderboardGroups()) { // Sailing events (e.g., "KielerWoche2023")
                    for (Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) { // Boat classes (e.g., "49er")
                        // check if this leaderboard contains at least one of the selected competitors
                        if (!Util.contains(leaderboard.getCompetitors(), competitor)) { // All leaderboards (Gold Fleet, Silver Fleet, Qualifying)
                            continue;
                        }
                        String regattaName = leaderboard.getName();
                        Regatta regatta = ctx.getRacingEventService().getRegattaByName(regattaName);
                        if (regatta == null) {
                            continue;
                        }
                        // skip, if the leaderboard is not part of this event (e.g. shared leaderboard group)
                        if (!leaderboard.isPartOfEvent(event)) {
                            continue;
                        }
                        for (TrackedRace tr : leaderboard.getTrackedRaces()) { // Loop through leaderboard races (e.g., "Race 1", "Medal Race")
                            if (Util.contains(tr.getRace().getCompetitors(), competitor)) { // Get all race participants via tr.getRace().getCompetitors()
                                extractValue(competitor, aggregator, end, leaderboard, tr, leaderboardGroup, event); // Extract performance value for competitor and add it to the aggregator
//                                for (final Competitor otherCompetitor : tr.getRace().getCompetitors()) {
//                                    if (!Util.contains(tr.getRace().getCompetitors(), competitor)) {
//                                        extractValue(otherCompetitor, aggregatorForOtherCompetitors, end, leaderboard, tr, leaderboardGroup, event);
//                                    }
//                                }
                                // Added with other validation
                                for (final Competitor otherCompetitor : tr.getRace().getCompetitors()) {
                                    if (!otherCompetitor.equals(competitor)) {
                                        extractValue(otherCompetitor, aggregatorForOtherCompetitors, end, leaderboard, tr, leaderboardGroup, event);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            result.put(new SimpleCompetitorWithIdDTO(competitor), aggregator.getResult()); 
//            aggregatesForOtherCompetitors.put(new SimpleCompetitorWithIdDTO(competitor), aggregatorForOtherCompetitors.getResult()); 
            aggregateForOtherCompetitors.put(new SimpleCompetitorWithIdDTO(competitor), aggregatorForOtherCompetitors.getResult()); 

        }
        List<String> competitorNames = StreamSupport.stream(pref.getCompetitors().spliterator(), false)
                .map(Competitor::getName).collect(Collectors.toList());
        String serializedQuery = DataMiningQueryCreatorForSailorProfiles.getSerializedDataMiningQuery(type,
                competitorNames);
        // add the for the "Team" competitor
        keepOnlyBestIfNecessary(result, type.getAggregationType());
//        return new SailorProfileStatisticDTO(result, aggregatesForOtherCompetitors, serializedQuery); // return the SailorProfileStatisticDTO
        return new SailorProfileStatisticDTO(result, aggregateForOtherCompetitors, serializedQuery); // return the SailorProfileStatisticDTO
    }

    /**
     * This method is responsible for getting a Value and adding it to the aggregator based on the
     * SailorProfileNumericStatisticType
     */
    @GwtIncompatible
    private void extractValue(Competitor competitor, final Aggregator aggregator, TimePoint end,
            Leaderboard leaderboard, TrackedRace tr, LeaderboardGroup leaderboardGroup, Event event) {
        switch (type) {
        case MAX_SPEED:
            getMaxSpeedInRaces(leaderboard, competitor, aggregator, tr, end, leaderboardGroup.getName(), leaderboardGroup.getId(), 
                    event.getId());
            break;
        case BEST_DISTANCE_TO_START:
            aggregator.add(tr.getDistanceToStartLine(competitor, 0), tr.getStartOfRace(), tr.getStartOfRace(),
                    tr.getRaceIdentifier(), leaderboard.getName(), leaderboardGroup.getName(), leaderboardGroup.getId(),
                    event.getId(), tr.getRace().getName());
            break;
        case BEST_STARTLINE_SPEED:
            Speed speed = tr.getSpeedWhenCrossingStartLine(competitor);
            aggregator.add(speed, tr.getStartOfRace(), tr.getStartOfRace(), tr.getRaceIdentifier(),
                    leaderboard.getName(), leaderboardGroup.getName(), leaderboardGroup.getId(), event.getId(),
                    tr.getRace().getName());
            break;
        case AVERAGE_STARTLINE_DISTANCE:
            Distance distance = tr.getDistanceToStartLine(competitor, 0);
                   
            aggregator.add(distance, null, null, null, null, null, null, null, null);
            break;    
         // added
        case AVERAGE_STARTLINE_DISTANCE_WITH_VALIDATION: // durchschnitte für einen competitor, z.B. mammazentrum1, dann mammazentrum2 etc.
            Distance distance_2 = tr.getDistanceToStartLine(competitor, 0);
            if (distance_2 != null && isValidRaceForStatistics(competitor, leaderboard, tr, end)) {                
                aggregator.add(distance_2, null, null, null, null, null, null, null, null);
            }
//            for (Competitor competitor_2 : pref.getCompetitors()) {
//                if (Util.contains(tr.getRace().getCompetitors(), competitor)) {
//                    Distance distance_2 = tr.getDistanceToStartLine(competitor, 0);
//                    
//                    if (distance_2 != null && isValidRaceForStatistics(leaderboard, competitor, tr, end)) {
//                        aggregator.add(distance_2, null, null, null, null, null, null, null, null);
//                    }
//                }
//            }
            break;
        case FIELD_AVERAGE_STARTLINE_DISTANCE_WITH_VALIDATION: // für jeden competitor durchschnitt aller anderen competitors in den jeweiligen rennen berechnen 
            // For each race: add all other competitors to the aggregator
            for (Competitor otherCompetitor : tr.getRace().getCompetitors()) {
                if (!otherCompetitor.equals(competitor)) {  // Not the current competitor
                    Distance otherDistance = tr.getDistanceToStartLine(otherCompetitor, 0);
                    if (otherDistance != null && isValidRaceForStatistics(competitor, leaderboard, tr, end)) {
                        aggregator.add(otherDistance, null, null, null, null, null, null, null, null);
                    }
                }
            }
            break;
//        case AVERAGE_VMG:
//            
//            break;
        default:
            break;
        }
    }
    
    // added
    /** check if the values are valid for calculation */
    @GwtIncompatible
    private boolean isValidRaceForStatistics(Competitor competitor, Leaderboard leaderboard, TrackedRace tr, TimePoint end) {
        // Find the RaceColumn for this TrackedRace
        RaceColumn raceColumn = null;
        for (RaceColumn rc : leaderboard.getRaceColumns()) {
            if (rc.getTrackedRace(competitor) == tr) {
                raceColumn = rc;
                break;
            }
        }
        
        // Check MaxPointsReason
        MaxPointsReason reason = null;
        if (raceColumn != null) {
            reason = leaderboard.getMaxPointsReason(competitor, raceColumn, end);
        }
        
        // Only valid races: null (normal), NONE, STP (standard penalty), RDG (redress)
        return reason == null || reason == MaxPointsReason.NONE || 
               reason == MaxPointsReason.STP || reason == MaxPointsReason.RDG;
    }


    /** reduces result map to only the best competitor if statistic is not average */
    @GwtIncompatible
    private void keepOnlyBestIfNecessary(Map<SimpleCompetitorWithIdDTO, ArrayList<SingleEntry>> results,
            StatisticType type) {
        final Set<SimpleCompetitorWithIdDTO> competitorsToRemove = new HashSet<>();
        if (type == StatisticType.HIGHEST_IS_BEST || type == StatisticType.LOWEST_IS_BEST) {
            // set best value to min/max depending whether highest/lowest is best
            double best = (type == StatisticType.HIGHEST_IS_BEST) ? Double.MIN_VALUE : Double.MAX_VALUE;
            SimpleCompetitorWithIdDTO bestCompetitor = null;
            for (Entry<SimpleCompetitorWithIdDTO, ArrayList<SingleEntry>> mapEntry : results.entrySet()) {
                for (SingleEntry statisticEntry : mapEntry.getValue()) {
                    // lower is better -> best must be bigger then the value of this entry to update the best
                    // higher is better -> best must be smaller then the value of this entry to update the best
                    if ((type == StatisticType.LOWEST_IS_BEST && best > statisticEntry.getValue())
                            || type == StatisticType.HIGHEST_IS_BEST && best < statisticEntry.getValue()) {
                        best = statisticEntry.getValue();
                        competitorsToRemove.add(bestCompetitor);
                        bestCompetitor = mapEntry.getKey();
                    } else {
                        competitorsToRemove.add(mapEntry.getKey());
                        continue;
                    }
                }
            }
        }
        competitorsToRemove.forEach(competitor -> results.remove(competitor));
    }

    /**
     * Determines the required aggregator based on SailorProfileNumericStatisticType
     */
    @GwtIncompatible
    private Aggregator determineAggregator() {
        final Aggregator aggregator;
        switch (type.getAggregationType()) {
        case AVERAGE:
            aggregator = new AverageAggregator();
            break;
        case HIGHEST_IS_BEST:
            aggregator = new MinMaxAggregator(true);
            break;
        case LOWEST_IS_BEST:
            aggregator = new MinMaxAggregator(false);
            break;
        default:
            aggregator = null;
            break;
        }
        return aggregator;
    }

    @GwtIncompatible
    private SingleEntry getMaxSpeedInRaces(Leaderboard leaderboard, Competitor competitor, Aggregator aggregator,
            TrackedRace tr, TimePoint endOfEvent, String bestLeaderboardGroupName, UUID bestLeaderboardGroupId,
            UUID eventId) {
        Pair<GPSFixMoving, Speed> bestFix = leaderboard.getMaximumSpeedOverGround(competitor, endOfEvent);
        SingleEntry newBetterResult = null;
        if (bestFix != null) {
            NavigableSet<MarkPassing> markPassings = tr.getMarkPassings(competitor);
            if (!markPassings.isEmpty()) {
                TimePoint from = markPassings.first().getTimePoint();
                // only count to last known markpassing (and finish as race end this way)
                TimePoint to = markPassings.last().getTimePoint();
                com.sap.sse.common.Util.Pair<GPSFixMoving, Speed> maxSpeed = tr.getTrack(competitor)
                        .getMaximumSpeedOverGround(from, to);

                if (maxSpeed != null && maxSpeed.getA() != null && maxSpeed.getB() != null) {
                    aggregator.add(maxSpeed.getB(), bestFix.getA().getTimePoint(), tr.getStartOfRace(),
                            tr.getRaceIdentifier(), leaderboard.getName(), bestLeaderboardGroupName, bestLeaderboardGroupId, eventId,
                            tr.getRace().getName());
                }
            }

        }
        return newBetterResult;
    }

    @GwtIncompatible
    private SailorProfilePreference findSailorProfile(CompetitorAndBoatStore store, SailorProfilePreferences prefs) {
        if (prefs == null) {
            throw new NullPointerException("no sailor profile present");
        } else {
            for (SailorProfilePreference p : prefs.getSailorProfiles()) {
                if (p.getUuid().equals(uuid)) {
                    return p;
                }
            }
            return null;
        }
    }

    /**
     * A helper to reduce redundant code, that offers a simple interface to add new values. This class should be null
     * safe so that the caller does not have to ensure this
     */
    @GwtIncompatible
    interface Aggregator {
        default void add(Distance distance, TimePoint bestTimePointOrNull, TimePoint startTimePointOrNull,
                RegattaAndRaceIdentifier regattaAndRaceIdentifierOrNull, String bestLeaderboardName,
                String bestLeaderboardGroupName, UUID bestLeaderboardGroupId, UUID eventId, String bestRaceName) {
            if (distance != null) {
                add(distance.getMeters(), bestTimePointOrNull, startTimePointOrNull, regattaAndRaceIdentifierOrNull,
                        bestLeaderboardName, bestLeaderboardGroupName, bestLeaderboardGroupId, eventId, bestRaceName);
            }
        }

        void add(Double value, TimePoint bestTime, TimePoint startTime, RegattaAndRaceIdentifier race,
                String bestLeaderboardName, String bestLeaderboardGroupName, UUID bestLeaderboardGroupId,
                UUID bestEventId, String bestRaceName);

        default void add(Speed speed, TimePoint bestTimePointOrNull, TimePoint startTimePointOrNull,
                RegattaAndRaceIdentifier regattaAndRaceIdentifierOrNull, String bestLeaderboardName,
                String bestLeaderboardGroupName, UUID bestLeaderboardGroupId, UUID eventId, String bestRaceName) {
            if (speed != null) {
                add(speed.getKnots(), bestTimePointOrNull, startTimePointOrNull, regattaAndRaceIdentifierOrNull,
                        bestLeaderboardName, bestLeaderboardGroupName, bestLeaderboardGroupId, eventId, bestRaceName);
            }
        }

        ArrayList<SingleEntry> getResult();
    }

    @GwtIncompatible
    private static class MinMaxAggregator implements Aggregator {
        private boolean max;
        private Double bestValue;
        private String bestLeaderboardName;
        private String bestLeaderboardGroupName;
        private UUID bestLeaderboardGroupId;
        private String bestRaceName;
        private UUID bestEventId;
        private TimePoint bestTimePoint;
        private TimePoint startTimePoint;
        private RegattaAndRaceIdentifier bestRace;

        public MinMaxAggregator(boolean max) {
            this.max = max;
        }

        @Override
        public void add(Double value, TimePoint bestTime, TimePoint startTime, RegattaAndRaceIdentifier race,
                String bestLeaderboardName, String bestLeaderboardGroupName, UUID bestLeaderboardGroupId,
                UUID bestEventId, String bestRaceName) {
            if (value != null) {
                if (this.bestValue == null || ((max && value > bestValue) || (!max && value < bestValue))) {
                    this.bestValue = value;
                    this.bestTimePoint = bestTime;
                    this.startTimePoint = startTime;
                    this.bestRace = race;
                    this.bestLeaderboardName = bestLeaderboardName;
                    this.bestLeaderboardGroupName = bestLeaderboardGroupName;
                    this.bestLeaderboardGroupId = bestLeaderboardGroupId;
                    this.bestEventId = bestEventId;
                    this.bestRaceName = bestRaceName;
                }
            }
        }

        @Override
        public ArrayList<SingleEntry> getResult() {
            ArrayList<SingleEntry> result = new ArrayList<>();
            if (bestValue != null) {
                // not all timepoints are serializable, ensure we use a compatible one
                result.add(new SingleEntry(bestValue, bestRace, new MillisecondsTimePoint(bestTimePoint.asMillis()),
                        new MillisecondsTimePoint(startTimePoint.asMillis()), bestLeaderboardName,
                        bestLeaderboardGroupName, bestLeaderboardGroupId, bestEventId, bestRaceName));
            }
            return result;
        }
    }

    @GwtIncompatible
    private static class AverageAggregator implements Aggregator {
        double averageCount = 0;
        Double average = null;

        @Override
        public void add(Double value, TimePoint bestTime, TimePoint startTime, RegattaAndRaceIdentifier race,
                String bestLeaderboardName, String bestLeaderboardGroupName, UUID bestLeaderboardGroupId, UUID eventId,
                String bestRaceName) {
            if (value != null) {
                averageCount++;
                if (average == null) {
                    average = value;
                } else {
                    average = average * ((averageCount - 1) / averageCount) + value * (1 / averageCount);
                }
            }
        }

        @Override
        public ArrayList<SingleEntry> getResult() {
            ArrayList<SingleEntry> result = new ArrayList<>();
            if (averageCount > 0) {
                result.add(new SingleEntry(average, null, null, null, null, null, null, null, null));
            }
            return result;
        }
    }
}
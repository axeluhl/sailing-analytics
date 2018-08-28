package com.sap.sailing.gwt.home.communication.user.profile.sailorprofile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.UUID;

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
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileNumericStatisticType;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileStatisticDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileStatisticDTO.SingleEntry;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.server.impl.preferences.model.SailorProfilePreference;
import com.sap.sailing.server.impl.preferences.model.SailorProfilePreferences;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.settings.GwtIncompatible;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

/**
 * {@link SailingAction} implementation to load all events, the competitors in a sailor profile with a specific uuid for
 * the currently logged in user have participated in to be shown on the sailor profile details page in the events
 * container.
 */
public class GetNumericStatisticForSailorProfileAction
        implements SailingAction<SailorProfileStatisticDTO>, SailorProfileConverter {

    private SailorProfileNumericStatisticType type;
    private UUID uuid;

    public GetNumericStatisticForSailorProfileAction(UUID uuid, SailorProfileNumericStatisticType type) {
        this.uuid = uuid;
        this.type = type;
    }

    /**
     * GWT serialisation only
     */
    @SuppressWarnings("unused")
    private GetNumericStatisticForSailorProfileAction() {
    }

    @Override
    @GwtIncompatible
    public SailorProfileStatisticDTO execute(SailingDispatchContext ctx) throws DispatchException {
        final Map<String, ArrayList<SingleEntry>> result = new HashMap<>();

        CompetitorAndBoatStore store = ctx.getRacingEventService().getCompetitorAndBoatStore();

        SailorProfilePreferences prefs = ctx.getPreferenceForCurrentUser(SailorProfilePreferences.PREF_NAME);
        SailorProfilePreference pref = findSailorProfile(store, prefs);

        for (Competitor competitor : pref.getCompetitors()) {
            final Aggregator aggregator = determineAggregator();
            if (aggregator == null) {
                continue;
            }
            String competitorIdAsString = competitor.getId().toString();
            for (Event event : ctx.getRacingEventService().getAllEvents()) {
                // determine end of event, or now in live case
                TimePoint end = event.getEndDate();
                if (end == null) {
                    end = MillisecondsTimePoint.now();
                }
                for (LeaderboardGroup leaderboardGroup : event.getLeaderboardGroups()) {
                    for (Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
                        // check if this leaderboard contains at least one of the selected competitors
                        if (!Util.contains(leaderboard.getCompetitors(), competitor)) {
                            continue;
                        }
                        String regattaName = leaderboard.getName();
                        Regatta regatta = ctx.getRacingEventService().getRegattaByName(regattaName);
                        if (regatta == null) {
                            continue;
                        }
                        // skip, if the leaderboard is not part of this event (e.g. shared leaderboard group)
                        if (!HomeServiceUtil.isPartOfEvent(event, leaderboard)) {
                            continue;
                        }
                        for (TrackedRace tr : leaderboard.getTrackedRaces()) {
                            if (Util.contains(tr.getRace().getCompetitors(), competitor)) {
                                extractValue(competitor, aggregator, end, leaderboard, tr);
                            }
                        }
                    }
                }
            }
            result.put(competitorIdAsString, aggregator.getResult());
        }
        return new SailorProfileStatisticDTO(result);
    }

    /**
     * This method is responsible for getting a Value and adding it to the aggregator based on the
     * SailorProfileNumericStatisticType
     */
    @GwtIncompatible
    private void extractValue(Competitor competitor, final Aggregator aggregator, TimePoint end,
            Leaderboard leaderboard, TrackedRace tr) {
        switch (type) {
        case MAX_SPEED:
            getMaxSpeedInRaces(leaderboard, competitor, aggregator, tr, end);
            break;
        case BEST_DISTANCE_TO_START:
            aggregator.add(tr.getDistanceToStartLine(competitor, 0), tr.getStartOfRace(), tr.getRaceIdentifier());
            break;
        case BEST_STARTLINE_SPEED:
            Speed speed = tr.getSpeedWhenCrossingStartLine(competitor);
            aggregator.add(speed, tr.getStartOfRace(), tr.getRaceIdentifier());
            break;
        case AVERAGE_STARTLINE_DISTANCE:
            aggregator.add(tr.getDistanceToStartLine(competitor, 0).getMeters(), null, null);
        default:
            break;
        }
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
            TrackedRace tr, TimePoint endOfEvent) {
        Pair<GPSFixMoving, Speed> bestFix = leaderboard.getMaximumSpeedOverGround(competitor, endOfEvent);
        SingleEntry newBetterResult = null;
        if (bestFix != null) {
            NavigableSet<MarkPassing> markPassings = tr.getMarkPassings(competitor);
            if (!markPassings.isEmpty()) {
                TimePoint from = markPassings.first().getTimePoint();
                // only count to last known markpassing (and finish as race end this
                // way)
                TimePoint to = markPassings.last().getTimePoint();
                com.sap.sse.common.Util.Pair<GPSFixMoving, Speed> maxSpeed = tr.getTrack(competitor)
                        .getMaximumSpeedOverGround(from, to);

                if (maxSpeed != null && maxSpeed.getA() != null && maxSpeed.getB() != null) {
                    final double maxSpeedInMetersPerSecond = maxSpeed.getB().getMetersPerSecond();
                    aggregator.add(maxSpeed.getB(), bestFix.getA().getTimePoint(), tr.getRaceIdentifier());
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
        default void add(Distance distance, TimePoint timePointOrNull,
                RegattaAndRaceIdentifier regattaAndRaceIdentifierOrNull) {
            if (distance != null) {
                add(distance.getMeters(), timePointOrNull, regattaAndRaceIdentifierOrNull);
            }
        }

        default void add(Speed speed, TimePoint timePointOrNull,
                RegattaAndRaceIdentifier regattaAndRaceIdentifierOrNull) {
            if (speed != null) {
                add(speed.getMetersPerSecond(), timePointOrNull, regattaAndRaceIdentifierOrNull);
            }
        }

        void add(Double value, TimePoint timePointOrNull, RegattaAndRaceIdentifier regattaAndRaceIdentifierOrNull);

        ArrayList<SingleEntry> getResult();
    }

    @GwtIncompatible
    private static class MinMaxAggregator implements Aggregator {
        private boolean max;
        private Double bestValue;
        private TimePoint bestTimePoint;
        private RegattaAndRaceIdentifier bestRace;

        public MinMaxAggregator(boolean max) {
            this.max = true;
        }

        @Override
        public void add(Double value, TimePoint time, RegattaAndRaceIdentifier race) {
            if (value != null) {
                if (this.bestValue == null) {
                    this.bestValue = value;
                    this.bestTimePoint = time;
                    this.bestRace = race;
                } else {
                    if ((max && value > bestValue) || (!max && value < bestValue)) {
                        this.bestValue = value;
                        this.bestTimePoint = time;
                        this.bestRace = race;
                    }
                }
            }
        }

        @Override
        public ArrayList<SingleEntry> getResult() {
            ArrayList<SingleEntry> result = new ArrayList<>();
            if (bestValue != null) {
                // not all timepoints are serializable, ensure we use a compatible one
                result.add(new SingleEntry(bestValue, bestRace, new MillisecondsTimePoint(bestTimePoint.asMillis())));
            }
            return result;
        }
    }

    @GwtIncompatible
    private static class AverageAggregator implements Aggregator {
        double averageCount = 0;
        Double average = null;

        @Override
        public void add(Double value, TimePoint time, RegattaAndRaceIdentifier race) {
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
                result.add(new SingleEntry(average, null, null));
            }
            return result;
        }
    }
}

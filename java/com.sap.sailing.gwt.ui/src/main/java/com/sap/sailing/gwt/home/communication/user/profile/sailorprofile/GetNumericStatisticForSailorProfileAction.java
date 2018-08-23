package com.sap.sailing.gwt.home.communication.user.profile.sailorprofile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.UUID;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Regatta;
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

    public GetNumericStatisticForSailorProfileAction() {
    }

    @Override
    @GwtIncompatible
    public SailorProfileStatisticDTO execute(SailingDispatchContext ctx) throws DispatchException {
        final Map<String, SingleEntry> result = new HashMap<>();

        CompetitorAndBoatStore store = ctx.getRacingEventService().getCompetitorAndBoatStore();

        SailorProfilePreferences prefs = ctx.getPreferenceForCurrentUser(SailorProfilePreferences.PREF_NAME);
        SailorProfilePreference pref = findSailorProfile(store, prefs);

        for (Event event : ctx.getRacingEventService().getAllEvents()) {
            for (LeaderboardGroup leaderboardGroup : event.getLeaderboardGroups()) {
                if (leaderboardGroup.hasOverallLeaderboard()) {
                    leaderboardGroup.getOverallLeaderboard();

                }
                for (Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
                    // check if this leaderboard contains at least one of the selected competitors
                    Collection<Competitor> containedCompetitors = new ArrayList<>();
                    for (Competitor competitor : pref.getCompetitors()) {
                        if (leaderboard.getCompetitors() != null
                                && Util.contains(leaderboard.getCompetitors(), competitor)) {
                            containedCompetitors.add(competitor);
                        }
                    }

                    // skip if none of the selected competitors is in this leaderboard
                    if (containedCompetitors.size() == 0) {
                        continue;
                    }

                    // create and add ParticipatedRegattaDTO for each of the selected competitors who was in this
                    // leaderboard
                    for (Competitor competitor : containedCompetitors) {
                        // regatta name is equal to the leaderboard name
                        String regattaName = leaderboard.getName();

                        Regatta regatta = ctx.getRacingEventService().getRegattaByName(regattaName);
                        if (regatta == null) {
                            continue;
                        }

                        // skip, if the regatta is not part of this event (e.g. shared leaderboard group)
                        if (!HomeServiceUtil.isPartOfEvent(event, leaderboard)) {
                            TimePoint end = event.getEndDate();
                            if (end == null) {
                                end = MillisecondsTimePoint.now();
                            }
                            String competitorIdAsString = competitor.getId().toString();
                            SingleEntry lastBestResult = result.get(competitorIdAsString);
                            SingleEntry newBetterResult = null;
                            for (TrackedRace tr : leaderboard.getTrackedRaces()) {
                                if (Util.contains(tr.getRace().getCompetitors(), competitor)) {
                                    switch (type) {
                                    case MAX_SPEED:
                                        newBetterResult = getMaxSpeedInRaces(leaderboard, competitor, end,
                                                lastBestResult, tr);
                                        break;
                                    case BEST_DISTANCE_TO_START:
                                        Distance distance = tr.getDistanceToStartLine(competitor, 0);
                                        if (lastBestResult == null
                                                || distance.getMeters() < lastBestResult.getValue()) {
                                            newBetterResult = new SingleEntry(distance.getMeters(),
                                                    tr.getRaceIdentifier(), tr.getStartOfRace());
                                        }
                                        break;
                                    case BEST_STARTLINE_SPEED:
                                        Speed speed = tr.getSpeedWhenCrossingStartLine(competitor);
                                        if (lastBestResult == null
                                                || speed.getMetersPerSecond() < lastBestResult.getValue()) {
                                            newBetterResult = new SingleEntry(speed.getMetersPerSecond(),
                                                    tr.getRaceIdentifier(), tr.getStartOfRace());
                                        }
                                        break;
                                    default:
                                        break;
                                    }
                                }
                            }

                            if (newBetterResult != null) {
                                // convert to a serializeable type
                                result.put(competitorIdAsString, newBetterResult);
                            }

                        }
                    }
                }
            }
        }
        return new SailorProfileStatisticDTO(result);
    }

    @GwtIncompatible
    private SingleEntry getMaxSpeedInRaces(Leaderboard leaderboard, Competitor competitor, TimePoint end,
            SingleEntry lastBestResult, TrackedRace tr) {
        Pair<GPSFixMoving, Speed> bestFix = leaderboard.getMaximumSpeedOverGround(competitor, end);
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
                    if (lastBestResult == null || lastBestResult.getValue() < maxSpeedInMetersPerSecond) {
                        newBetterResult = new SingleEntry(maxSpeedInMetersPerSecond, tr.getRaceIdentifier(),
                                bestFix.getA().getTimePoint());
                    }
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
}

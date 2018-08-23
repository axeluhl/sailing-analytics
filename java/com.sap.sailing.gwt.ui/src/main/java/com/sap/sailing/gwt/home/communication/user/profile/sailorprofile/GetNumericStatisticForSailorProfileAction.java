package com.sap.sailing.gwt.home.communication.user.profile.sailorprofile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileNumericStatisticType;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileStatisticDTO;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.server.impl.preferences.model.SailorProfilePreference;
import com.sap.sailing.server.impl.preferences.model.SailorProfilePreferences;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;
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
        Double bestValue = null;
        TimePoint timeOfBest = null;
        String bestCompetitorIdAsString = null;

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
                            if(end == null) {
                                end = MillisecondsTimePoint.now();
                            }
                            switch (type) {
                            case MAX_SPEED:
                                Pair<GPSFixMoving, Speed> bestFix = leaderboard.getMaximumSpeedOverGround(competitor,
                                        end);
                                if (bestFix != null) {
                                    double bestForLeaderboard = bestFix.getB().getKilometersPerHour();
                                    if (bestValue == null || bestValue < bestForLeaderboard) {
                                        bestValue = bestForLeaderboard;
                                        timeOfBest = new MillisecondsTimePoint(
                                                bestFix.getA().getTimePoint().asMillis());
                                        bestCompetitorIdAsString = competitor.getId().toString();
                                    }
                                }
                                break;
                            default:
                                break;

                            }
                            
                        }
                    }
                }
            }
        }
        return new SailorProfileStatisticDTO(bestValue, timeOfBest, bestCompetitorIdAsString);
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

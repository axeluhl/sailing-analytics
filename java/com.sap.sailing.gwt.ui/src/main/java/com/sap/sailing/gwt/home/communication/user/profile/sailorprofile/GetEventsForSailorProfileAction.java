package com.sap.sailing.gwt.home.communication.user.profile.sailorprofile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.ParticipatedEventDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.ParticipatedRegattaDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileEventsDTO;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.server.impl.preferences.model.SailorProfilePreference;
import com.sap.sailing.server.impl.preferences.model.SailorProfilePreferences;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

/**
 * {@link SailingAction} implementation to load all events, the competitors in a sailor profile with a specific uuid for
 * the currently logged in user have participated in to be shown on the sailor profile details page in the events
 * container.
 */
public class GetEventsForSailorProfileAction implements SailingAction<SailorProfileEventsDTO>, SailorProfileConverter {

    private UUID uuid;

    public GetEventsForSailorProfileAction(UUID uuid) {
        this.uuid = uuid;
    }

    public GetEventsForSailorProfileAction() {
    }

    @Override
    @GwtIncompatible
    public SailorProfileEventsDTO execute(SailingDispatchContext ctx) throws DispatchException {

        CompetitorAndBoatStore store = ctx.getRacingEventService().getCompetitorAndBoatStore();

        SailorProfilePreferences prefs = ctx.getPreferenceForCurrentUser(SailorProfilePreferences.PREF_NAME);
        SailorProfilePreference pref = findSailorProfile(store, prefs);
        Collection<ParticipatedEventDTO> participatedEvents = new ArrayList<>();

        for (Event event : ctx.getRacingEventService().getAllEvents()) {
            Collection<ParticipatedRegattaDTO> participatedRegattas = new ArrayList<>();
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
                        int rank = 0;
                        try {
                            rank = leaderboard.getTotalRankOfCompetitor(competitor, MillisecondsTimePoint.now());
                        } catch (NoWindException e1) {
                            // ignore
                        }
                        // final String leaderboardName = leaderboard.getDisplayName();
                        final double points = leaderboard.getNetPoints(competitor, MillisecondsTimePoint.now());

                        // regatta name is equal to the leaderboard name
                        String regattaName = leaderboard.getName();

                        Regatta regatta = ctx.getRacingEventService().getRegattaByName(regattaName);
                        String clubName = competitor.getName();

                        if (regatta == null) {
                            continue;
                        }

                        // skip, if the regatta is not part of this event (e.g. shared leaderboard group)
                        if (!HomeServiceUtil.isPartOfEvent(event, leaderboard)) {

                            // skip, if overall leaderboard is not part of this event, don't skip if this is a regatta
                            // during an event which is not part of an overall leaderboard
                            if (leaderboardGroup.hasOverallLeaderboard()
                                    && !HomeServiceUtil.isPartOfEvent(event, leaderboardGroup.getOverallLeaderboard()))
                                continue;
                        }

                        participatedRegattas.add(
                                new ParticipatedRegattaDTO(regattaName, rank, new SimpleCompetitorWithIdDTO(competitor),
                                        "" + regatta.getId(), "" + event.getId(), points));
                    }
                }
            }
            if (participatedRegattas.size() > 0) {
                participatedEvents
                        .add(new ParticipatedEventDTO(event.getName(), event.getId().toString(), participatedRegattas));
            }
        }

        return new SailorProfileEventsDTO(participatedEvents);
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

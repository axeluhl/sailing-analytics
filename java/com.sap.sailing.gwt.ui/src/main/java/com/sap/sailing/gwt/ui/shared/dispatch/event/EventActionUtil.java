package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.gwt.ui.shared.dispatch.DTO;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.general.EventState;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

@GwtIncompatible
public final class EventActionUtil {
    private EventActionUtil() {
    }
    
    protected interface CalculationWithEvent<T extends DTO> {
        ResultWithTTL<T> calculateWithEvent(Event event);
    }
    
    protected interface RaceCallback {
        void doForRace(RaceContext context);
    }
    
    public static <T extends DTO> ResultWithTTL<T> withLiveRaceOrDefaultSchedule(DispatchContext context, UUID eventId, CalculationWithEvent<T> callback) {
        TimePoint now = MillisecondsTimePoint.now();
        Event event = context.getRacingEventService().getEvent(eventId);
        EventState eventState = HomeServiceUtil.calculateEventState(event);
        if(eventState == EventState.UPCOMING || eventState == EventState.PLANNED) {
            Duration tillStart = now.until(event.getStartDate());
            double hoursTillStart = tillStart.asHours();
            long ttl = 1000 * 60 * 60;
            if(hoursTillStart < 24) {
                ttl = 1000 * 60 * 30;
            }
            if(hoursTillStart < 2) {
                ttl = 1000 * 60 * 15;
            }
            ttl = Math.min(ttl, tillStart.asMillis());
            return new ResultWithTTL<T>(ttl, null);
        }
        if(eventState == EventState.FINISHED) {
            return new ResultWithTTL<T>(1000 * 60 * 60 * 12, null);
        }
        return callback.calculateWithEvent(event);
    }

    public static void forRacesOfEvent(DispatchContext context, UUID eventId, RaceCallback callback) {
        Event event = context.getRacingEventService().getEvent(eventId);
        for (LeaderboardGroup lg : event.getLeaderboardGroups()) {
            for (Leaderboard lb : lg.getLeaderboards()) {
                for(RaceColumn raceColumn : lb.getRaceColumns()) {
                    for(Fleet fleet : raceColumn.getFleets()) {
                        callback.doForRace(new RaceContext(event, lb, raceColumn, fleet, context.getRacingEventService()));
                    }
                }
            }
        }
    }
    
    public static void forRacesOfRegatta(DispatchContext context, UUID eventId, String regattaName, RaceCallback callback) {
        Event event = context.getRacingEventService().getEvent(eventId);
        // TODO check that the leaderboard is part of the event
        Leaderboard lb = context.getRacingEventService().getLeaderboardByName(regattaName);
        for(RaceColumn raceColumn : lb.getRaceColumns()) {
            for(Fleet fleet : raceColumn.getFleets()) {
                callback.doForRace(new RaceContext(event, lb, raceColumn, fleet, context.getRacingEventService()));
            }
        }
    }
}

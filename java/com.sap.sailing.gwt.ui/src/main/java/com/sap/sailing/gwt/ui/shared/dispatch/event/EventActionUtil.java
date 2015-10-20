package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.gwt.ui.shared.dispatch.DTO;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchException;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.general.EventState;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
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
    
    protected interface LeaderboardCallback {
        void doForLeaderboard(LeaderboardContext context);
    }
    
    public static LeaderboardContext getOverallLeaderboardContext(DispatchContext context, UUID eventId) {
        RacingEventService service = context.getRacingEventService();
        Event event = service.getEvent(eventId);
        if(!HomeServiceUtil.isFakeSeries(event)) {
            throw new DispatchException("The given event is not a series event.");
        }
        LeaderboardGroup leaderboardGroup = Util.get(event.getLeaderboardGroups(), 0);
        Leaderboard overallLeaderboard = leaderboardGroup.getOverallLeaderboard();
        return new LeaderboardContext(context, event, leaderboardGroup, overallLeaderboard);
    }
    
    public static LeaderboardContext getLeaderboardContext(DispatchContext context, UUID eventId, String leaderboardId) {
        RacingEventService service = context.getRacingEventService();
        Event event = service.getEvent(eventId);
        for(LeaderboardGroup leaderboardGroup : event.getLeaderboardGroups()) {
            for(Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
                if(leaderboard.getName().equals(leaderboardId)) {
                    return new LeaderboardContext(context, event, leaderboardGroup, leaderboard);
                }
            }
        }
        throw new DispatchException("The leaderboard is not part of the given event.");
    }
    
    public static Duration getEventStateDependentTTL(DispatchContext context, UUID eventId, Duration liveTTL) {
        Event event = context.getRacingEventService().getEvent(eventId);
        return getEventStateDependentTTL(event, liveTTL);
    }
    
    private static Duration getEventStateDependentTTL(Event event, Duration liveTTL) {
        EventState eventState = HomeServiceUtil.calculateEventState(event);
        if(eventState == EventState.RUNNING) {
            return liveTTL;
        }
        return calculateTtlForNonLiveEvent(event, eventState);
    }

    public static <T extends DTO> ResultWithTTL<T> withLiveRaceOrDefaultSchedule(DispatchContext context, UUID eventId, CalculationWithEvent<T> callback) {
        Event event = context.getRacingEventService().getEvent(eventId);
        EventState eventState = HomeServiceUtil.calculateEventState(event);
        if(eventState != EventState.RUNNING) {
            return new ResultWithTTL<T>(calculateTtlForNonLiveEvent(event, eventState), null);
        }
        return callback.calculateWithEvent(event);
    }

    public static Duration calculateTtlForNonLiveEvent(Event event, EventState eventState) {
        TimePoint now = MillisecondsTimePoint.now();
        if(eventState == EventState.UPCOMING || eventState == EventState.PLANNED) {
            Duration tillStart = now.until(event.getStartDate());
            double hoursTillStart = tillStart.asHours();
            long ttl = Duration.ONE_HOUR.asMillis();
            if(hoursTillStart < 36) {
                ttl = Duration.ONE_MINUTE.times(30).asMillis();
            }
            if(hoursTillStart < 3) {
                ttl = Duration.ONE_MINUTE.times(15).asMillis();
            }
            ttl = Math.min(ttl, tillStart.asMillis());
            return new MillisecondsDurationImpl(ttl);
        }
        if(eventState == EventState.FINISHED) {
            return Duration.ONE_HOUR.times(12);
        }
        return Duration.NULL;
    }
    
    public static void forLeaderboardsOfEvent(DispatchContext context, UUID eventId, LeaderboardCallback callback) {
        RacingEventService service = context.getRacingEventService();
        Event event = service.getEvent(eventId);
        if (event == null) {
            throw new RuntimeException("Event not found");
        }
        forLeaderboardsOfEvent(context, event, callback);
    }

    public static void forLeaderboardsOfEvent(DispatchContext context, Event event, LeaderboardCallback callback) {
        for (LeaderboardGroup leaderboardGroup : event.getLeaderboardGroups()) {
            for (Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
                callback.doForLeaderboard(new LeaderboardContext(context, event, leaderboardGroup, leaderboard));
            }
        }
    }
    
    public static void forRacesOfEvent(final DispatchContext context, UUID eventId, final RaceCallback callback) {
        forLeaderboardsOfEvent(context, eventId, new LeaderboardCallback() {
            @Override
            public void doForLeaderboard(LeaderboardContext leaderboardContext) {
                leaderboardContext.forRaces(callback);
            }
        });
    }
    
    public static void forRacesOfRegatta(DispatchContext context, UUID eventId, String regattaName, RaceCallback callback) {
        getLeaderboardContext(context, eventId, regattaName).forRaces(callback);
    }
}

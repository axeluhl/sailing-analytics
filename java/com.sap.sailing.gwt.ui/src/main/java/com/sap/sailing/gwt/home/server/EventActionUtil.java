package com.sap.sailing.gwt.home.server;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.gwt.dispatch.client.DTO;
import com.sap.sailing.gwt.dispatch.client.ResultWithTTL;
import com.sap.sailing.gwt.dispatch.client.exceptions.DispatchException;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.event.EventState;
import com.sap.sailing.gwt.server.HomeServiceUtil;
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
    
    public interface CalculationWithEvent<T extends DTO> {
        ResultWithTTL<T> calculateWithEvent(Event event);
    }
    
    public interface RaceCallback {
        void doForRace(RaceContext context);
    }
    
    public interface LeaderboardCallback {
        void doForLeaderboard(LeaderboardContext context);
    }
    
    public static LeaderboardContext getOverallLeaderboardContext(SailingDispatchContext context, UUID eventId) {
        RacingEventService service = context.getRacingEventService();
        Event event = service.getEvent(eventId);
        if(!HomeServiceUtil.isFakeSeries(event)) {
            throw new DispatchException("The given event is not a series event.");
        }
        LeaderboardGroup leaderboardGroup = Util.get(event.getLeaderboardGroups(), 0);
        Leaderboard overallLeaderboard = leaderboardGroup.getOverallLeaderboard();
        return new LeaderboardContext(context, event, leaderboardGroup, overallLeaderboard);
    }
    
    public static LeaderboardContext getLeaderboardContext(SailingDispatchContext context, UUID eventId, String leaderboardId) {
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
    
    public static Duration getEventStateDependentTTL(SailingDispatchContext context, UUID eventId, Duration liveTTL) {
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

    public static <T extends DTO> ResultWithTTL<T> withLiveRaceOrDefaultSchedule(SailingDispatchContext context, UUID eventId, CalculationWithEvent<T> callback) {
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
    
    public static void forLeaderboardsOfEvent(SailingDispatchContext context, UUID eventId, LeaderboardCallback callback) {
        RacingEventService service = context.getRacingEventService();
        Event event = service.getEvent(eventId);
        if (event == null) {
            throw new RuntimeException("Event not found");
        }
        forLeaderboardsOfEvent(context, event, callback);
    }

    public static void forLeaderboardsOfEvent(SailingDispatchContext context, Event event, LeaderboardCallback callback) {
        for (LeaderboardGroup leaderboardGroup : event.getLeaderboardGroups()) {
            for (Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
                callback.doForLeaderboard(new LeaderboardContext(context, event, leaderboardGroup, leaderboard));
            }
        }
    }
    
    public static void forRacesOfEvent(final SailingDispatchContext context, UUID eventId, final RaceCallback callback) {
        forLeaderboardsOfEvent(context, eventId, new LeaderboardCallback() {
            @Override
            public void doForLeaderboard(LeaderboardContext leaderboardContext) {
                leaderboardContext.forRaces(callback);
            }
        });
    }
    
    public static void forRacesOfRegatta(SailingDispatchContext context, UUID eventId, String regattaName, RaceCallback callback) {
        getLeaderboardContext(context, eventId, regattaName).forRaces(callback);
    }
}

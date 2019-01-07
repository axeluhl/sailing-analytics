package com.sap.sailing.gwt.home.communication.anniversary;

import java.net.URL;
import java.util.Date;
import java.util.Map;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.anniversary.DetailedRaceInfo;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.common.dto.AnniversaryType;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.event.EventState;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.gwt.server.HomeServiceUtil.EventVisitor;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;

public class GetAnniversariesAction implements SailingAction<ResultWithTTL<AnniversariesDTO>> {

    private static final int SHOW_IF_FEWER_RACES_TILL_NEXT_ANNIVERSARY = 500;
    private static final int DAYS_TO_SHOW_PAST_ANNIVERSARY = 14;

    @Override
    @GwtIncompatible
    public ResultWithTTL<AnniversariesDTO> execute(SailingDispatchContext context) {
        final AnniversariesDTO anniversaries = new AnniversariesDTO();
        final RacingEventService service = context.getRacingEventService();
        final Map<Integer, Pair<DetailedRaceInfo, AnniversaryType>> knownAnnivs = service.getKnownAnniversaries();
        final int currentRaceCount = service.getCurrentRaceCount();

        final Pair<Integer, AnniversaryType> next = service.getNextAnniversary();
        
        if (next != null && (next.getA() - currentRaceCount) < SHOW_IF_FEWER_RACES_TILL_NEXT_ANNIVERSARY) {
            if (!knownAnnivs.containsKey(next.getA())) {
                anniversaries.addValue(new AnniversaryDTO(next.getA(), currentRaceCount, next.getB()));
            }
        }

        knownAnnivs.forEach((target, anniversaryInfo) -> {
            final DetailedRaceInfo raceinfo = anniversaryInfo.getA();
            if (daysUntilNow(raceinfo.getStartOfRace().asDate()) < DAYS_TO_SHOW_PAST_ANNIVERSARY) {
                anniversaries.addValue(new AnniversaryDTO(target, currentRaceCount, anniversaryInfo.getB(),
                        raceinfo.getEventID(), raceinfo.getLeaderboardName(),
                        raceinfo.getRemoteUrl() == null ? null : raceinfo.getRemoteUrl().toExternalForm(),
                        raceinfo.getIdentifier().getRaceName(), raceinfo.getIdentifier().getRegattaName(),
                        raceinfo.getEventName(), raceinfo.getLeaderboardDisplayName(), raceinfo.getEventType()));
            }
        });

        TimeToLiveCalculator timeToLiveCalculator = new TimeToLiveCalculator();
        HomeServiceUtil.forAllPublicEvents(service, context.getRequest(), timeToLiveCalculator);
        return new ResultWithTTL<AnniversariesDTO>(timeToLiveCalculator.getTimeToLive(), anniversaries);
    }

    @GwtIncompatible
    private class TimeToLiveCalculator implements EventVisitor {

        private Duration timeToLive = Duration.ONE_MINUTE.times(5);

        @Override
        public void visit(EventBase event, boolean onRemoteServer, URL baseURL) {
            if (HomeServiceUtil.calculateEventState(event) == EventState.RUNNING) {
                timeToLive = Duration.ONE_MINUTE;
            }
        }

        public Duration getTimeToLive() {
            return timeToLive;
        }

    }

    // FIXME Remove as soon as DateUtil is refactored to work on server-side too
    private static int daysUntilNow(Date date) {
        final long dayInMillis = 1000 * 60 * 60 * 24;
        return (int) (new Date().getTime() / dayInMillis - date.getTime() / dayInMillis);
    }
}

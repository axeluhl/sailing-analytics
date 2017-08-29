package com.sap.sailing.gwt.home.communication.anniversary;

import java.net.URL;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.anniversary.DetailedRaceInfo;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.common.dto.AnniversaryType;
import com.sap.sailing.gwt.common.client.DateUtil;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.event.EventState;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.gwt.server.HomeServiceUtil.EventVisitor;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;

public class GetAnniversariesAction implements SailingAction<ResultWithTTL<AnniversariesDTO>> {

    private static final int SHOW_IF_LESS_RACES_TILL_NEXT_ANNIVERSARY = 500;
    private static final int DAYS_TO_SHOW_PAST_ANNIVERSARY = 14;

    @Override
    @GwtIncompatible
    public ResultWithTTL<AnniversariesDTO> execute(SailingDispatchContext context) {
        final AnniversariesDTO anniversaries = new AnniversariesDTO();
        final RacingEventService service = context.getRacingEventService();
        final Integer nextCountdown = service.getNextAnniversaryCountdown();

        if (nextCountdown != null && nextCountdown < SHOW_IF_LESS_RACES_TILL_NEXT_ANNIVERSARY) {
            final Pair<Integer, AnniversaryType> next = service.getNextAnniversary();
            anniversaries.addValue(new AnniversaryDTO(next.getA(), nextCountdown, next.getB()));
        }

        service.getKnownAnniversaries().forEach((target, anniversaryInfo) -> {
            final DetailedRaceInfo raceinfo = anniversaryInfo.getA();
            if (DateUtil.daysUntilNow(raceinfo.getStartOfRace().asDate()) < DAYS_TO_SHOW_PAST_ANNIVERSARY) {
                anniversaries.addValue(new AnniversaryDTO(target, anniversaryInfo.getB(), raceinfo.getEventID(),
                        raceinfo.getLeaderboardName(), raceinfo.getRemoteUrl().toExternalForm(),
                        raceinfo.getIdentifier().getRaceName(), raceinfo.getIdentifier().getRegattaName()));
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
}

package com.sap.sailing.gwt.home.communication.anniversary;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.anniversary.DetailedRaceInfo;
import com.sap.sailing.domain.common.dto.AnniversaryType;
import com.sap.sailing.gwt.common.client.DateUtil;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.dispatch.shared.commands.ListResult;

public class GetAnniversariesAction implements SailingAction<ListResult<AnniversaryDTO>> {

    private static final int SHOW_IF_LESS_RACES_TILL_NEXT_ANNIVERSARY = 500;
    private static final int DAYS_TO_SHOW_PAST_ANNIVERSARY = 14;

    @Override
    @GwtIncompatible
    public ListResult<AnniversaryDTO> execute(SailingDispatchContext context) {
        final ListResult<AnniversaryDTO> result = new ListResult<>();
        final RacingEventService service = context.getRacingEventService();
        Integer nextCountdown = service.getNextAnniversaryCountdown();
        if (nextCountdown != null && nextCountdown < SHOW_IF_LESS_RACES_TILL_NEXT_ANNIVERSARY) {
            Pair<Integer, AnniversaryType> next = service.getNextAnniversary();
            result.addValue(new AnniversaryDTO(next.getA(), nextCountdown, next.getB()));
        }

        Map<Integer, Pair<DetailedRaceInfo, AnniversaryType>> knownAnniversaries = service.getKnownAnniversaries();
        for (Entry<Integer, Pair<DetailedRaceInfo, AnniversaryType>> anniversary : knownAnniversaries.entrySet()) {
            int daysSinceAnniversary = Math
                    .abs(DateUtil.daysFromNow(anniversary.getValue().getA().getStartOfRace().asDate()));
            if (daysSinceAnniversary < DAYS_TO_SHOW_PAST_ANNIVERSARY) {
                DetailedRaceInfo raceinfo = anniversary.getValue().getA();
                AnniversaryType anniversaryType = anniversary.getValue().getB();
                String raceName = raceinfo.getIdentifier().getRaceName();
                String regattaName = raceinfo.getIdentifier().getRegattaName();
                UUID eventID = raceinfo.getEventID();
                result.addValue(new AnniversaryDTO(anniversary.getKey(), -daysSinceAnniversary, anniversaryType,
                        eventID, raceinfo.getLeaderboardName(), raceinfo.getRemoteUrl().toExternalForm(), raceName,
                        regattaName));
            }
        }
        return result;
    }
}

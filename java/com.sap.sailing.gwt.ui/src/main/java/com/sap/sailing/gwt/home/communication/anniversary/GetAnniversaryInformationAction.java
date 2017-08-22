package com.sap.sailing.gwt.home.communication.anniversary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.anniversary.DetailedRaceInfo;
import com.sap.sailing.domain.common.dto.AnniversaryType;
import com.sap.sailing.gwt.common.client.DateUtil;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.anniversary.GetAnniversaryInformationDTO.AnniversaryInformation;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.Util.Pair;

public class GetAnniversaryInformationAction implements SailingAction<GetAnniversaryInformationDTO> {
    private static final int SHOW_IF_LESS_RACES_TILL_NEXT_ANNIVERSARY = 500;
    private static final int DAYS_TO_SHOW_PAST_ANNIVERSARY = 14;

    public GetAnniversaryInformationAction() {
    }

    @Override
    @GwtIncompatible
    public GetAnniversaryInformationDTO execute(SailingDispatchContext context) {
        List<AnniversaryInformation> result = new ArrayList<>();
        RacingEventService service = context.getRacingEventService();
        Integer nextCountdown = service.getNextAnniversaryCountdown();
        if (nextCountdown != null && nextCountdown < SHOW_IF_LESS_RACES_TILL_NEXT_ANNIVERSARY) {
            Pair<Integer, AnniversaryType> next = service.getNextAnniversary();
            result.add(
                    new AnniversaryInformation(next.getA(), nextCountdown, next.getB(), null, null, null, null, null));
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
                result.add(new AnniversaryInformation(anniversary.getKey(), -daysSinceAnniversary, anniversaryType,
                        eventID, raceinfo.getLeaderboardName(), raceinfo.getRemoteUrl().toExternalForm(), raceName,
                        regattaName));
            }
        }
        return new GetAnniversaryInformationDTO(result);
    }
}

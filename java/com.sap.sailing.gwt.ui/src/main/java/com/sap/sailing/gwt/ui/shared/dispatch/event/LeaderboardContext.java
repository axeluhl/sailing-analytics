package com.sap.sailing.gwt.ui.shared.dispatch.event;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventActionUtil.RaceCallback;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaWithProgressDTO;

public class LeaderboardContext {

    private final Event event;
    private final LeaderboardGroup leaderboardGroup;
    private final Leaderboard leaderboard;

    public LeaderboardContext(Event event, LeaderboardGroup leaderboardGroup, Leaderboard leaderboard) {
        this.event = event;
        this.leaderboardGroup = leaderboardGroup;
        this.leaderboard = leaderboard;
    }
    
    public void forRaces(DispatchContext context, RaceCallback callback) {
        for(RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            for(Fleet fleet : raceColumn.getFleets()) {
                callback.doForRace(new RaceContext(event, leaderboard, raceColumn, fleet, context.getRacingEventService()));
            }
        }
    }

    public RegattaWithProgressDTO getRegattaWithProgress(DispatchContext context) {
        RegattaProgressCalculator regattaProgressCalculator = new RegattaProgressCalculator();
        forRaces(context, regattaProgressCalculator);
        RegattaWithProgressDTO regattaDTO = new RegattaWithProgressDTO(regattaProgressCalculator.getResult());
        HomeServiceUtil.fillRegattaFields(leaderboardGroup, leaderboard, regattaDTO);
        return regattaDTO;
    }
}

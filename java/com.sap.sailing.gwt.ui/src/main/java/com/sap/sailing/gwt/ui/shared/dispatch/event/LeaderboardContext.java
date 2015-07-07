package com.sap.sailing.gwt.ui.shared.dispatch.event;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventActionUtil.RaceCallback;

public class LeaderboardContext {

    private final Event event;
    private final Leaderboard leaderboard;

    public LeaderboardContext(Event event, Leaderboard leaderboard) {
        this.event = event;
        this.leaderboard = leaderboard;
    }
}

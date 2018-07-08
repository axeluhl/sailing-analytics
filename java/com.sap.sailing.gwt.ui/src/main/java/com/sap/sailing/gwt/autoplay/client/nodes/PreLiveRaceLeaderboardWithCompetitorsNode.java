package com.sap.sailing.gwt.autoplay.client.nodes;

import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.nodes.base.FiresPlaceNode;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.leaderboard.AbstractPreRaceLeaderboardWithImagePlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.leaderboard.PreRaceLeaderboardWithCompetitorPlace;

public class PreLiveRaceLeaderboardWithCompetitorsNode extends FiresPlaceNode {
    private final AutoPlayClientFactory cf;

    public PreLiveRaceLeaderboardWithCompetitorsNode(AutoPlayClientFactory cf) {
        super(PreLiveRaceLeaderboardWithCompetitorsNode.class.getName());
        this.cf = cf;
    }

    public void onStart() {
        AbstractPreRaceLeaderboardWithImagePlace place = new PreRaceLeaderboardWithCompetitorPlace();
        setPlaceToGo(place);
        firePlaceChangeAndStartTimer();
        getBus().fireEvent(new AutoPlayHeaderEvent(cf.getAutoPlayCtxSignalError().getPreLiveRace().getRegattaName(),
                cf.getAutoPlayCtxSignalError().getPreLiveRace().getRaceName()));
    };
}

package com.sap.sailing.gwt.autoplay.client.nodes;

import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.nodes.base.FiresPlaceNode;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.leaderboard.AbstractPreRaceLeaderBoardWithImagePlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.leaderboard.PreRaceLeaderBoardWithCompetitorPlace;

public class PreLiveRaceLeaderBoardWithCompetitorsNode extends FiresPlaceNode {
    private final AutoPlayClientFactory cf;

    public PreLiveRaceLeaderBoardWithCompetitorsNode(AutoPlayClientFactory cf) {
        super(PreLiveRaceLeaderBoardWithCompetitorsNode.class.getName());
        this.cf = cf;
    }

    public void onStart() {
        AbstractPreRaceLeaderBoardWithImagePlace place = new PreRaceLeaderBoardWithCompetitorPlace();
        setPlaceToGo(place);
        firePlaceChangeAndStartTimer();
        getBus().fireEvent(new AutoPlayHeaderEvent(cf.getAutoPlayCtx().getPreLiveRace().getRegattaName(),
                cf.getAutoPlayCtx().getPreLiveRace().getRaceName()));
    };
}

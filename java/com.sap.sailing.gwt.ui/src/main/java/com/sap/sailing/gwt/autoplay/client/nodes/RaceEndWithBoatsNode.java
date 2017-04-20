package com.sap.sailing.gwt.autoplay.client.nodes;

import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.autoplay.client.app.sixtyinch.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.nodes.base.FiresPlaceNode;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderbordAction;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;

public class RaceEndWithBoatsNode extends FiresPlaceNode {
    private final AutoPlayClientFactorySixtyInch cf;

    public RaceEndWithBoatsNode(AutoPlayClientFactorySixtyInch cf) {
        this.cf = cf;
    }


    public void onStart() {

        UUID eventId = cf.getSlideCtx().getSettings().getEventId();
        String leaderBoardName = cf.getSlideCtx().getSettings().getLeaderBoardName();

        cf.getDispatch().execute(new GetMiniLeaderbordAction(eventId, leaderBoardName),
                new AsyncCallback<ResultWithTTL<GetMiniLeaderboardDTO>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        // fireEvent(new DataLoadFailureEvent(MiniLeaderboardLoader.this, caught));
                        firePlaceChangeAndStartTimer();
                    }

                    @Override
                    public void onSuccess(ResultWithTTL<GetMiniLeaderboardDTO> resultTTL) {
                        /*
                         * GetMiniLeaderboardDTO dto = resultTTL.getDto(); RaceEndWithBoatsPlace place = new
                         * RaceEndWithBoatsPlace(); place.setLeaderBoardDTO(dto);
                         * place.setLifeRace(cf.getSlideCtx().getLifeRace()); setPlaceToGo(place);
                         * firePlaceChangeAndStartTimer();
                         */
                    }
                });
    };
}

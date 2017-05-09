package com.sap.sailing.gwt.autoplay.client.nodes;

import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.nodes.base.FiresPlaceNode;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderbordAction;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;

public class PreLeaderBoardWithFlagsNode extends FiresPlaceNode {
    private final AutoPlayClientFactory cf;

    public PreLeaderBoardWithFlagsNode(AutoPlayClientFactory cf) {
        this.cf = cf;
    }


    public void onStart() {

        UUID eventId = cf.getSlideCtx().getSettings().getEventId();
        String leaderBoardName = cf.getSlideCtx().getSettings().getLeaderboardName();
        cf.getDispatch().execute(new GetMiniLeaderbordAction(eventId, leaderBoardName),
                new AsyncCallback<ResultWithTTL<GetMiniLeaderboardDTO>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        // fireEvent(new DataLoadFailureEvent(MiniLeaderboardLoader.this, caught));
                    }

                    @Override
                    public void onSuccess(ResultWithTTL<GetMiniLeaderboardDTO> resultTTL) {
                        /*
                         * GetMiniLeaderboardDTO dto = resultTTL.getDto(); AbstractPreRaceLeaderBoardWithImagePlace
                         * place = new AbstractPreRaceLeaderBoardWithImagePlace(); place.setLeaderBoardDTO(dto);
                         * setPlaceToGo(place); firePlaceChangeAndStartTimer();
                         */
                    }
                });
    };
}

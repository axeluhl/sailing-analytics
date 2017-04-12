package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes;

import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.TimedTransitionSimpleNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide0.AbstractPreRaceLeaderBoardWithImagePlace;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderbordAction;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;

public class PreLeaderBoardWithFlagsNode extends TimedTransitionSimpleNode {
    private final AutoPlayClientFactorySixtyInch cf;

    public PreLeaderBoardWithFlagsNode(AutoPlayClientFactorySixtyInch cf) {
        super("PreLeaderBoardWithFlagsNode", 30000);
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
                        GetMiniLeaderboardDTO dto = resultTTL.getDto();
                        AbstractPreRaceLeaderBoardWithImagePlace place = new AbstractPreRaceLeaderBoardWithImagePlace();
                        place.setLeaderBoardDTO(dto);
                        setPlaceToGo(place);
                        firePlaceChangeAndStartTimer();
                    }
                });

    };
}

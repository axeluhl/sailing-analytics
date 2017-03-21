package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes;

import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.TimedTransitionSimpleNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide1.Slide1Place;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderbordAction;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;

public class Slide1Node extends TimedTransitionSimpleNode {
    private final AutoPlayClientFactorySixtyInch cf;

    public Slide1Node(AutoPlayClientFactorySixtyInch cf) {
        super("slide1", 10000);
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
                    }

                    @Override
                    public void onSuccess(ResultWithTTL<GetMiniLeaderboardDTO> result) {

                        GetMiniLeaderboardDTO dto = result.getDto();
                        Slide1Place place = new Slide1Place();
                        place.setLeaderBoardDTO(dto);
                        setPlaceToGo(place);
                        firePlaceChangeAndStartTimer();
                    }
                });
    };
}

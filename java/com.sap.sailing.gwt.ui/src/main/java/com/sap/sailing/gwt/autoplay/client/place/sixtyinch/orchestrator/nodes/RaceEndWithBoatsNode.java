package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes;

import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.TimedTransitionSimpleNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.HelperSixty;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide8.RaceEndWithBoatsPlace;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderbordAction;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;

public class RaceEndWithBoatsNode extends TimedTransitionSimpleNode {
    private final AutoPlayClientFactorySixtyInch cf;

    public RaceEndWithBoatsNode(AutoPlayClientFactorySixtyInch cf) {
        super("slide1", 30000);
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
                        HelperSixty.getLifeRace(cf.getSailingService(), cf.getErrorReporter(),
                                cf.getSlideCtx().getEvent(), leaderBoardName, cf.getDispatch(),
                                new AsyncCallback<RegattaAndRaceIdentifier>() {

                                    @Override
                                    public void onSuccess(RegattaAndRaceIdentifier lifeRace) {
                                        GetMiniLeaderboardDTO dto = resultTTL.getDto();
                                        RaceEndWithBoatsPlace place = new RaceEndWithBoatsPlace();
                                        place.setLeaderBoardDTO(dto);
                                        place.setLifeRace(lifeRace);
                                        setPlaceToGo(place);
                                        firePlaceChangeAndStartTimer();
                                    }

                                    @Override
                                    public void onFailure(Throwable caught) {

                                    }
                                });

                    }
                });

    };
}

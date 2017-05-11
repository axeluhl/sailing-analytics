package com.sap.sailing.gwt.autoplay.client.nodes;

import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.nodes.base.FiresPlaceNode;
import com.sap.sailing.gwt.autoplay.client.places.screens.afterliveraceloop.boats.RaceEndWithCompetitorsTop3Place;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderbordAction;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;

public class RaceEndWithCompetitorsNode extends FiresPlaceNode {
    private final AutoPlayClientFactory cf;

    public RaceEndWithCompetitorsNode(AutoPlayClientFactory cf) {
        this.cf = cf;
    }

    public void onStart() {

        UUID eventId = cf.getAutoPlayCtx().getContextDefinition().getEventId();
        String leaderBoardName = cf.getAutoPlayCtx().getContextDefinition().getLeaderboardName();

        cf.getDispatch().execute(new GetMiniLeaderbordAction(eventId, leaderBoardName),
                new AsyncCallback<ResultWithTTL<GetMiniLeaderboardDTO>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        firePlaceChangeAndStartTimer();
                        getBus().fireEvent(new AutoPlayHeaderEvent("", ""));
                    }

                    @Override
                    public void onSuccess(ResultWithTTL<GetMiniLeaderboardDTO> resultTTL) {
                        GetMiniLeaderboardDTO dto = resultTTL.getDto();
                        RaceEndWithCompetitorsTop3Place place = new RaceEndWithCompetitorsTop3Place();
                        place.setLeaderBoardDTO(dto);
                        place.setLifeRace(cf.getAutoPlayCtx().getLastRace());
                        setPlaceToGo(place);
                        firePlaceChangeAndStartTimer();
                        getBus().fireEvent(new AutoPlayHeaderEvent(cf.getAutoPlayCtx().getLastRace().getRegattaName(),
                                StringMessages.INSTANCE.results() + " "
                                        + cf.getAutoPlayCtx().getLastRace().getRaceName()));
                    }
                });
    };
}

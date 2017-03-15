package com.sap.sailing.gwt.autoplay.client.dataloader;

import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.events.DataLoadFailureEvent;
import com.sap.sailing.gwt.autoplay.client.events.EventChanged;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderbordAction;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;

public class MiniLeaderboardLoader extends AutoPlayDataLoaderBase<AutoPlayClientFactorySixtyInch> {
    @Override
    protected void onStartedLoading() {
        getEventBus().addHandler(EventChanged.TYPE, new EventChanged.Handler() {
            @Override
            public void onEventChanged(EventChanged e) {
                doLoadData();
            }
        });
    }
    @Override
    protected void doLoadData() {
        UUID eventId = getClientFactory().getSlideCtx().getSettings().getEventId();
        String leaderBoardName = getClientFactory().getSlideCtx().getSettings().getLeaderBoardName();
        if (eventId != null && leaderBoardName != null) {
            GetMiniLeaderbordAction leaderboardAction = new GetMiniLeaderbordAction(eventId, leaderBoardName);
            getClientFactory().getDispatch().execute(leaderboardAction,
                    new AsyncCallback<ResultWithTTL<GetMiniLeaderboardDTO>>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            fireEvent(new DataLoadFailureEvent(MiniLeaderboardLoader.this, caught));
                        }

                        @Override
                        public void onSuccess(ResultWithTTL<GetMiniLeaderboardDTO> result) {
                            setLoadingIntervallInMs(result.cacheTotalTimeToLiveMillis());
                            GetMiniLeaderboardDTO dto = result.getDto();
                            getClientFactory().getSlideCtx().updateMiniLeaderboardDTO(dto);
                        }
                    });
        }
    }
}

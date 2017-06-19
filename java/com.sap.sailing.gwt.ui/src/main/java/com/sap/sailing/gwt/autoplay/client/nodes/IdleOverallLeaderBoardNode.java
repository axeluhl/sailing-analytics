package com.sap.sailing.gwt.autoplay.client.nodes;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.nodes.base.FiresPlaceNode;
import com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.leaderboardsixty.IdleOverallLeaderBoardPlace;
import com.sap.sailing.gwt.autoplay.client.shared.SixtyInchLeaderBoard;
import com.sap.sailing.gwt.autoplay.client.utils.AutoplayHelper;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings.RaceColumnSelectionStrategies;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardEntryPoint;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;

public class IdleOverallLeaderBoardNode extends FiresPlaceNode {
    private final AutoPlayClientFactory cf;
    private Timer timer;

    public IdleOverallLeaderBoardNode(AutoPlayClientFactory cf) {
        super(IdleOverallLeaderBoardNode.class.getName());
        this.cf = cf;

    }

    public void onStart() {
        List<DetailType> overallDetails = new ArrayList<>();
        overallDetails.add(DetailType.OVERALL_RANK);
        overallDetails.add(DetailType.REGATTA_RANK);

        List<DetailType> raceDetails = new ArrayList<>();
        // raceDetails.add(DetailType.RACE_RANK);

        final LeaderboardSettings leaderboardSettings = new LeaderboardSettings(null, null, raceDetails, overallDetails,
                null, null, null, false, null, null, /* ascending */ true, /* updateUponPlayStateChange */ true,
                RaceColumnSelectionStrategies.EXPLICIT, /* showAddedScores */ true,
                /* showOverallRacesCompleted */ false, true, false, false, true);

        timer = new com.sap.sse.gwt.client.player.Timer(
                // perform the first request as "live" but don't by default auto-play
                PlayModes.Live, PlayStates.Playing,
                /* delayBetweenAutoAdvancesInMilliseconds */ LeaderboardEntryPoint.DEFAULT_REFRESH_INTERVAL_MILLIS);

        cf.getSailingService().getOverallLeaderboardNamesContaining(
                cf.getAutoPlayCtx().getContextDefinition().getLeaderboardName(),
                new MarkedAsyncCallback<List<String>>(new AsyncCallback<List<String>>() {
                    @Override
                    public void onSuccess(List<String> result) {
                        if (result.size() == 1) {
                            String overallLeaderboardName = result.get(0);
                            String selectedLeaderboardsGroup = AutoplayHelper.getSelectedLeaderboardsGroup(
                                    cf.getAutoPlayCtx().getEvent(),
                                    cf.getAutoPlayCtx().getContextDefinition().getLeaderboardName());
                            CompetitorSelectionProvider provider = new CompetitorSelectionModel(true);
                            SixtyInchLeaderBoard leaderboardPanel = new SixtyInchLeaderBoard(cf.getSailingService(),
                                    new AsyncActionsExecutor(), leaderboardSettings, true, null, provider, timer,
                                    selectedLeaderboardsGroup, overallLeaderboardName, cf.getErrorReporter(),
                                    StringMessages.INSTANCE, null, false, null, false, null, false, true, false, false,
                                    false);

                            IdleOverallLeaderBoardPlace place = new IdleOverallLeaderBoardPlace(leaderboardPanel);

                            setPlaceToGo(place);
                            firePlaceChangeAndStartTimer();
                            getBus().fireEvent(new AutoPlayHeaderEvent(cf.getAutoPlayCtx().getEvent().getName(),
                                    cf.getAutoPlayCtx().getContextDefinition().getLeaderboardName()));
                        } else {
                            GWT.log("Not found any overleaderboardname");
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        // DO NOTHING
                    }

                }));
    }

    @Override
    public void onStop() {
        if (timer != null) {
            timer.pause();
        }
        super.onStop();
    }
}

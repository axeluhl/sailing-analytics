package com.sap.sailing.gwt.autoplay.client.nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.nodes.base.FiresPlaceNode;
import com.sap.sailing.gwt.autoplay.client.nodes.base.ProvidesDuration;
import com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.leaderboardsixty.IdleSixtyInchLeaderBoardPlace;
import com.sap.sailing.gwt.settings.client.leaderboard.MultiRaceLeaderboardSettings;
import com.sap.sailing.gwt.settings.client.leaderboard.RaceColumnSelectionStrategies;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.FlagImageResolverImpl;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardEntryPoint;
import com.sap.sailing.gwt.ui.leaderboard.MultiRaceLeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.SixtyInchLeaderBoardStyle;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;

public class IdleSixtyInchLeaderBoardNode extends FiresPlaceNode implements ProvidesDuration {
    private static final Logger logger = Logger.getLogger(IdleSixtyInchLeaderBoardNode.class.getName());
    private final AutoPlayClientFactory cf;
    private Timer timer;
    private Consumer<Integer> durationConsumer;

    public IdleSixtyInchLeaderBoardNode(AutoPlayClientFactory cf) {
        super(IdleSixtyInchLeaderBoardNode.class.getName());
        this.cf = cf;
    }

    public void onStart() {
        List<DetailType> overallDetails = new ArrayList<>();
        overallDetails.add(DetailType.OVERALL_RANK);
        overallDetails.add(DetailType.REGATTA_RANK);

        List<DetailType> raceDetails = new ArrayList<>();
        // raceDetails.add(DetailType.RACE_RANK);
        final MultiRaceLeaderboardSettings leaderboardSettings = new MultiRaceLeaderboardSettings(
                /* maneuverDetailsToShow */ null, /* legDetailsToShow */ null, raceDetails, overallDetails,
                /* namesOfRaceColumnsToShow */ null, /* numberOfLastRacesToShow */ null,
                /* delayBetweenAutoAdvancesInMilliseconds */ null, RaceColumnSelectionStrategies.EXPLICIT,
                /* showAddedScores */ true, /* showCompetitorShortNameColumn */ true,
                /* showCompetitorFullNameColumn */ false, /* showCompetitorBoatInfoColumn */ false,
                /* isCompetitorNationalityColumnVisible */ true);

        timer = new Timer(
                // perform the first request as "live" but don't by default auto-play
                PlayModes.Live, PlayStates.Playing,
                /* delayBetweenAutoAdvancesInMilliseconds */ LeaderboardEntryPoint.DEFAULT_REFRESH_INTERVAL_MILLIS);
        String leaderBoard = cf.getAutoPlayCtx().getContextDefinition().getLeaderboardName();

        cf.getSailingService().getOverallLeaderboardNamesContaining(leaderBoard,
                new MarkedAsyncCallback<List<String>>(new AsyncCallback<List<String>>() {

                    @Override
                    public void onSuccess(List<String> result) {
                        String leaderboardName = null;
                        if (result.isEmpty()) {
                            leaderboardName = leaderBoard;
                        } else {
                            leaderboardName = result.get(0);
                        }
                        CompetitorSelectionProvider provider = new CompetitorSelectionModel(true);

                        MultiRaceLeaderboardPanel leaderboardPanel = new MultiRaceLeaderboardPanel(null, null,
                                cf.getSailingService(), new AsyncActionsExecutor(), leaderboardSettings, false,
                                provider, timer, null, leaderboardName, cf.getErrorReporter(), StringMessages.INSTANCE,
                                false, null, false, null, false, true, false, false, false,
                                new SixtyInchLeaderBoardStyle(true), FlagImageResolverImpl.get(),
                                Arrays.asList(DetailType.values()));

                        IdleSixtyInchLeaderBoardPlace place = new IdleSixtyInchLeaderBoardPlace(leaderboardPanel, provider,
                                durationConsumer);

                        setPlaceToGo(place);
                        firePlaceChangeAndStartTimer();
                        getBus().fireEvent(new AutoPlayHeaderEvent(cf.getAutoPlayCtx().getEvent().getName(),
                                cf.getAutoPlayCtx().getContextDefinition().getLeaderboardName()));
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        logger.log(Level.SEVERE, "Remote call for Leaderboard loading failed", caught);
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

    @Override
    public void setDurationConsumer(Consumer<Integer> durationConsumer) {
        this.durationConsumer = durationConsumer;
    }
}

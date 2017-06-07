package com.sap.sailing.gwt.autoplay.client.places.screens.afterliveraceloop.boats;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayPresenterConfigured;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.shared.SixtyInchLeaderBoard;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings.RaceColumnSelectionStrategies;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardEntryPoint;
import com.sap.sse.common.Duration;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;

public class RaceEndWithBoatsPresenterImpl extends AutoPlayPresenterConfigured<AbstractRaceEndWithImagesTop3Place>
        implements RaceEndWithBoatsView.NextRaceWithBoatsPresenter {
    protected static final int SWITCH_COMPETITOR_DELAY = 2000;
    private RaceEndWithBoatsView view;
    private SixtyInchLeaderBoard leaderboardPanel;
    private CompetitorSelectionModel competitorSelectionProvider;

    public RaceEndWithBoatsPresenterImpl(AbstractRaceEndWithImagesTop3Place place, AutoPlayClientFactory clientFactory,
            RaceEndWithBoatsView slide1ViewImpl) {
        super(place, clientFactory);
        this.view = slide1ViewImpl;
    }

    @Override
    public void startConfigured(AcceptsOneWidget panel) {

        SailingServiceAsync sailingService = getClientFactory().getSailingService();
        ErrorReporter errorReporter = getClientFactory().getErrorReporter();
        view.startingWith(this, panel);

        RegattaAndRaceIdentifier liveRace = getPlace().getLastRace();
        if (liveRace == null) {
            panel.setWidget(new Label("No raceIdentifier specified"));
            return;
        }
        getEventBus().fireEvent(new AutoPlayHeaderEvent(getSlideCtx().getContextDefinition().getLeaderboardName(),
                getPlace().getLastRace().getRaceName()));

        final LeaderboardSettings leaderboardSettings = new LeaderboardSettings(null, null, null, null, null, null,
                null, false, null, liveRace.getRaceName(), /* ascending */ true, /* updateUponPlayStateChange */ true,
                RaceColumnSelectionStrategies.EXPLICIT, /* showAddedScores */ false,
                /* showOverallRacesCompleted */ false, true, false, true, true);

        competitorSelectionProvider = new CompetitorSelectionModel(/* hasMultiSelection */ false);

        com.sap.sse.gwt.client.player.Timer timer = new com.sap.sse.gwt.client.player.Timer(PlayModes.Live,
                PlayStates.Paused,
                /* delayBetweenAutoAdvancesInMilliseconds */ LeaderboardEntryPoint.DEFAULT_REFRESH_INTERVAL_MILLIS);
        leaderboardPanel = new SixtyInchLeaderBoard(sailingService, new AsyncActionsExecutor(), leaderboardSettings,
                true, liveRace, competitorSelectionProvider, timer, null,
                getSlideCtx().getContextDefinition().getLeaderboardName(), errorReporter, StringMessages.INSTANCE, null,
                false, null, false, null, false, true, false, false, false);

        
        int competitorCount = getPlace().getStatistic().getCompetitors();
        Distance distance  = getPlace().getStatistic().getDistance();
        Duration duration = getPlace().getStatistic().getDuration();
        
        view.setStatistic(competitorCount,distance,duration);
        
        view.setLeaderBoard(leaderboardPanel);
        leaderboardPanel.addLeaderboardUpdateListener(new LeaderboardUpdateListener() {
            
            @Override
            public void updatedLeaderboard(LeaderboardDTO leaderboard) {
                determinePlacement(liveRace);                
            }
            
            @Override
            public void currentRaceSelected(RaceIdentifier raceIdentifier, RaceColumnDTO raceColumn) {
                
            }
        });
    }

    private void determinePlacement(RegattaAndRaceIdentifier lifeRace) {

        RaceColumnDTO preselectedRaceColumn = leaderboardPanel.getLeaderboard().getRaceList().get(0);
        List<CompetitorDTO> sortedCompetitors = leaderboardPanel.getLeaderboard()
                .getCompetitorsFromBestToWorst(preselectedRaceColumn);

        if (sortedCompetitors.size() >= 3) {
            GWT.log("First is " + sortedCompetitors.get(0));
            view.setFirst(sortedCompetitors.get(0));
            view.setSecond(sortedCompetitors.get(1));
            view.setThird(sortedCompetitors.get(2));
        }
    }

    @Override
    public void onStop() {
        view.onStop();
    }
}

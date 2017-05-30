package com.sap.sailing.gwt.autoplay.client.places.screens.liveraceloop.racemapwithleaderboard;

import java.util.ArrayList;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayPresenterConfigured;
import com.sap.sailing.gwt.autoplay.client.shared.SixtyInchLeaderBoard;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings.RaceColumnSelectionStrategies;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardEntryPoint;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;

public class LiveRaceWithRacemapAndLeaderBoardPresenterImpl extends AutoPlayPresenterConfigured<LiveRaceWithRacemapAndLeaderBoardPlace> implements LiveRaceWithRacemapAndLeaderBoardView.Slide7Presenter {
    protected static final int SWITCH_COMPETITOR_DELAY = 2000;
    private LiveRaceWithRacemapAndLeaderBoardView view;
    private Timer selectionTimer;
    private SixtyInchLeaderBoard leaderboardPanel;
    private int selected = -1;
    ArrayList<CompetitorDTO> compList = new ArrayList<>();

    public LiveRaceWithRacemapAndLeaderBoardPresenterImpl(LiveRaceWithRacemapAndLeaderBoardPlace place,
            AutoPlayClientFactory clientFactory,
            LiveRaceWithRacemapAndLeaderBoardView LifeRaceWithRacemapViewImpl) {
        super(place, clientFactory);
        this.view = LifeRaceWithRacemapViewImpl;
        selectionTimer = new Timer() {
            @Override
            public void run() {
                 selectNext();
            }
        };
    }

    protected void selectNext() {
        try {
            compList.clear();
            // sync with Leaderboard sorting
            for (LeaderboardRowDTO item : leaderboardPanel.getLeaderboardTable().getVisibleItems()) {
                compList.add(item.competitor);
                getPlace().getRaceMapSelectionProvider().setSelected(item.competitor, false);
            }
            // wait for data in leaderboard, if empty no need to proceed
            if (compList.isEmpty()) {
                selectionTimer.schedule(SWITCH_COMPETITOR_DELAY);
                return;
            }

            selected++;
            // overflow, restart
            if (selected > compList.size() - 1) {
                selected = 0;
            }

            CompetitorDTO marked = compList.get(selected);
            getPlace().getRaceMapSelectionProvider().setSelected(marked, true);
            onSelect(marked);
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    if (selected == 0) {
                        view.scrollLeaderBoardToTop();
                    } else {
                        leaderboardPanel.scrollRowIntoView(selected);
                    }
                }
            });
        } catch (Exception e) {
            // ensure that the loop keeps running, no matter if errors occur
            e.printStackTrace();
            selected = 0;
        }
        selectionTimer.schedule(SWITCH_COMPETITOR_DELAY);
    }

    private void onSelect(CompetitorDTO marked) {
        view.onCompetitorSelect(marked);
    }

    @Override
    public void startConfigured(AcceptsOneWidget panel) {
        if (getPlace().getError() != null) {
            view.showErrorNoLive(this, panel, getPlace().getError());
            return;
        }

        SailingServiceAsync sailingService = getClientFactory().getSailingService();
        ErrorReporter errorReporter = getClientFactory().getErrorReporter();

        RegattaAndRaceIdentifier lifeRace = getSlideCtx().getLifeRace();
        ArrayList<String> racesToShow = null;
        if (lifeRace != null) {
            racesToShow = new ArrayList<>();
            racesToShow.add(lifeRace.getRaceName());
        } else {
            view.showErrorNoLive(this, panel, new IllegalStateException("Na race is life"));
            return;
        }

        final LeaderboardSettings leaderboardSettings = new LeaderboardSettings(null, null, null, null,
        null, racesToShow, null, false, null, lifeRace.getRaceName(),
        /* ascending */ true, /* updateUponPlayStateChange */ true, RaceColumnSelectionStrategies.EXPLICIT,
        /* showAddedScores */ false, /* showOverallRacesCompleted */ false, true,
        false, true, true);


        com.sap.sse.gwt.client.player.Timer timer = new com.sap.sse.gwt.client.player.Timer(
                // perform the first request as "live" but don't by default auto-play
                PlayModes.Live, PlayStates.Playing,
                /* delayBetweenAutoAdvancesInMilliseconds */ LeaderboardEntryPoint.DEFAULT_REFRESH_INTERVAL_MILLIS);
        leaderboardPanel = new SixtyInchLeaderBoard(sailingService, new AsyncActionsExecutor(), leaderboardSettings,
                true, lifeRace, getPlace().getRaceMapSelectionProvider(), timer, null,
                getSlideCtx().getContextDefinition().getLeaderboardName(), errorReporter,
                StringMessages.INSTANCE, null, false, null, false, null, false, true, false, false, false);
        
        view.startingWith(this, panel, getPlace().getRaceMap(), leaderboardPanel);
        selectionTimer.schedule(SWITCH_COMPETITOR_DELAY);
    }

    @Override
    public void onStop() {
        selectionTimer.cancel();
        view.onStop();
    }

}

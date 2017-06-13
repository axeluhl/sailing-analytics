package com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.leaderboard;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.gwt.autoplay.client.app.AnimationPanel;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayPresenterConfigured;
import com.sap.sailing.gwt.autoplay.client.shared.SixtyInchLeaderBoard;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings.RaceColumnSelectionStrategies;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardEntryPoint;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;

public class PreLeaderBoardWithImagePresenterImpl
        extends AutoPlayPresenterConfigured<AbstractPreRaceLeaderBoardWithImagePlace>
        implements PreLeaderboardWithImageView.Slide1Presenter {
    protected static final int SWITCH_COMPETITOR_DELAY = 2000;
    private int selected = -1;
    private PreLeaderboardWithImageView view;
    private SixtyInchLeaderBoard leaderboardPanel;
    private Timer selectionTimer;
    private CompetitorSelectionModel competitorSelectionProvider;
    ArrayList<CompetitorDTO> compList = new ArrayList<>();

    public PreLeaderBoardWithImagePresenterImpl(AbstractPreRaceLeaderBoardWithImagePlace place,
            AutoPlayClientFactory clientFactory, PreLeaderboardWithImageView slide1ViewImpl) {
        super(place, clientFactory);
        this.view = slide1ViewImpl;
        selectionTimer = new Timer() {
            @Override
            public void run() {
                selectNext();
            }
        };
    }

    protected void selectNext() {
        compList.clear();
        // sync with Leaderboard sorting
        for (LeaderboardRowDTO item : leaderboardPanel.getLeaderboardTable().getVisibleItems()) {
            compList.add(item.competitor);
        }
        if (compList.isEmpty()) {
            // no data loaded yet
            return;
        }
        if (selected >= 0) {
            CompetitorDTO lastSelected = compList.get(selected);
            competitorSelectionProvider.setSelected(lastSelected, false);
        }
        selected++;
        // overflow, restart
        if (selected > compList.size() - 1) {
            selected = 0;
        }
        CompetitorDTO newSelected = compList.get(selected);
        competitorSelectionProvider.setSelected(newSelected, true);
        view.onCompetitorSelect(newSelected);
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                if (selected == 0) {
                    view.scrollLeaderBoardToTop();
                } else {
                    leaderboardPanel.scrollRowIntoView(selected);
                }
                selectionTimer.schedule(SWITCH_COMPETITOR_DELAY);
            }
        });
    }

    @Override
    public void startConfigured(AcceptsOneWidget panel) {
        SailingServiceAsync sailingService = getClientFactory().getSailingService();
        ErrorReporter errorReporter = getClientFactory().getErrorReporter();
        view.startingWith(this, panel);

        RegattaAndRaceIdentifier lifeRace = getSlideCtx().getLiveRace();
        ArrayList<String> racesToShow = null;
        if (lifeRace != null) {
            racesToShow = new ArrayList<>();
//            racesToShow.add(lifeRace.getRaceName());
        } else {
            return;
        }

        final LeaderboardSettings leaderboardSettings = new LeaderboardSettings(null, null, null, null, null,
                racesToShow, null, false, null, lifeRace.getRaceName(), /* ascending */ true,
                /* updateUponPlayStateChange */ true, RaceColumnSelectionStrategies.EXPLICIT,
                /* showAddedScores */ false, /* showOverallRacesCompleted */ false, true, false, false, true);

        GWT.log("event " + getSlideCtx().getEvent());
        competitorSelectionProvider = new CompetitorSelectionModel(/* hasMultiSelection */ false);

        com.sap.sse.gwt.client.player.Timer timer = new com.sap.sse.gwt.client.player.Timer(
                // perform the first request as "live" but don't by default auto-play
                PlayModes.Live, PlayStates.Playing,
                /* delayBetweenAutoAdvancesInMilliseconds */ LeaderboardEntryPoint.DEFAULT_REFRESH_INTERVAL_MILLIS);
        leaderboardPanel = new NoRaceColumnsSixtyInchLeaderboard(sailingService, new AsyncActionsExecutor(), leaderboardSettings,
                true, lifeRace, competitorSelectionProvider, timer, null,
                getSlideCtx().getContextDefinition().getLeaderboardName(), errorReporter, StringMessages.INSTANCE, null,
                false, null, false, null, false, true, false, false, false);
        view.setLeaderBoard(leaderboardPanel);
        selectionTimer.schedule(AnimationPanel.DELAY + AnimationPanel.ANIMATION_DURATION);
        

    }

    @Override
    public void onStop() {
        selectionTimer.cancel();
        view.onStop();
    }
}

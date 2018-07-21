package com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.leaderboardsixty;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.gwt.autoplay.client.app.AnimationPanel;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayPresenterConfigured;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.leaderboard.MultiRaceLeaderboardPanel;

public class IdleSixtyInchLeaderboardPresenterImpl extends AutoPlayPresenterConfigured<IdleSixtyInchLeaderboardPlace>
        implements IdleSixtyInchLeaderboardView.Slide7Presenter {
    private static final int SWITCH_COMPETITOR_DELAY = 2000;
    private static final int WAIT_FOR_POPULATION_DELAY = 10000;
    private static final Logger LOGGER = Logger.getLogger(IdleSixtyInchLeaderboardPresenterImpl.class.getName());
    private IdleSixtyInchLeaderboardView view;
    private int selected = -1;
    ArrayList<CompetitorDTO> compList = new ArrayList<>();
    private MultiRaceLeaderboardPanel leaderboardPanel;
    private Timer selectionTimer;
    private int emptyTries;

    public IdleSixtyInchLeaderboardPresenterImpl(IdleSixtyInchLeaderboardPlace place, AutoPlayClientFactory clientFactory,
            IdleSixtyInchLeaderboardView lifeRaceWithRacemapViewImpl) {
        super(place, clientFactory);
        this.view = lifeRaceWithRacemapViewImpl;
        selectionTimer = new Timer() {
            @Override
            public void run() {
                selectNext();
            }
        };
    }

    @Override
    public void startConfigured(AcceptsOneWidget panel) {
        emptyTries = 0;
        try {
            leaderboardPanel = getPlace().getLeaderboardPanel();
            view.startingWith(this, panel, getPlace().getLeaderboardPanel());
            selectionTimer.schedule(SWITCH_COMPETITOR_DELAY+AnimationPanel.ANIMATION_DURATION+AnimationPanel.DELAY);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "error strting idle leaderboard", e.getCause());
        }
    }

    @Override
    public void onStop() {
        view.onStop();
        selectionTimer.cancel();
    }
    
    protected void selectNext() {
        try {
            CompetitorSelectionProvider competitorSelectionProvider = getPlace().getCompetitorSelectionProvider();
            compList.clear();
            // sync with Leaderboard sorting
            for (LeaderboardRowDTO item : leaderboardPanel.getLeaderboardTable().getVisibleItems()) {
                compList.add(item.competitor);
            }
            
            // wait for data in leaderboard, if empty no need to proceed
            if (compList.isEmpty()) {
                //ensure that if for some reasons the leaderboard will not be populated, we are not trapped here
                if (emptyTries * SWITCH_COMPETITOR_DELAY > WAIT_FOR_POPULATION_DELAY) {
                    getPlace().getDurationConsumer().accept(0);
                    return;
                }
                emptyTries++;
                selectionTimer.schedule(SWITCH_COMPETITOR_DELAY);
                return;
            } else {
                emptyTries = 0;
            }
            selected++;
            // end reached, notify we are done
            if (selected >= compList.size()) {
                getPlace().getDurationConsumer().accept(2);
            } else {
                for (CompetitorDTO competitor : compList) {
                    competitorSelectionProvider.setSelected(competitor, false);
                }
                CompetitorDTO marked = compList.get(selected);
                competitorSelectionProvider.setSelected(marked, true);
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        if (selected == 0) {
                            view.scrollLeaderBoardToTop();
                        }
                        view.scrollIntoView(selected);
                    }
                });
                selectionTimer.schedule(SWITCH_COMPETITOR_DELAY);
            }
        } catch (Exception e) {
            // ensure that the loop keeps running, no matter if errors occur
            selected = 0;
            selectionTimer.schedule(SWITCH_COMPETITOR_DELAY);
        }
        
    }
    

}

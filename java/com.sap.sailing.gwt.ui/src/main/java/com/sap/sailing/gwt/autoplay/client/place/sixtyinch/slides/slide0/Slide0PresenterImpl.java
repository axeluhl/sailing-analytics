package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide0;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SixtyInchLeaderBoard;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.ConfiguredSlideBase;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettingsFactory;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;

public class Slide0PresenterImpl extends ConfiguredSlideBase<Slide0Place> implements Slide0View.Slide1Presenter {
    private int selected = -1;
    private Slide0View view;
    private SixtyInchLeaderBoard leaderboardPanel;
    private Timer selectionTimer;
    private CompetitorSelectionModel competitorSelectionProvider;

    public Slide0PresenterImpl(Slide0Place place, AutoPlayClientFactorySixtyInch clientFactory,
            Slide0View slide1ViewImpl) {
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
        Iterator<CompetitorDTO> iter = competitorSelectionProvider.getAllCompetitors().iterator();
        ArrayList<CompetitorDTO> compList = new ArrayList<>();
        while (iter.hasNext()) {
            compList.add(iter.next());
        }
        if (compList.isEmpty()) {
            // no data loaded yet
            return;
        }

        if(selected >= 0){
            CompetitorDTO lastSelected = compList.get(selected);
            competitorSelectionProvider.setSelected(lastSelected, false);
        }
        selected++;

        // overflow, restart
        if (selected > compList.size() - 1) {
            selected = 0;
        }

        GWT.log("Select " + selected);
        CompetitorDTO marked = compList.get(selected);
        competitorSelectionProvider.setSelected(marked, true);
    }

    @Override
    public void startConfigured(AcceptsOneWidget panel) {
        SailingServiceAsync sailingService = getClientFactory().getSailingService();
        ErrorReporter errorReporter = getClientFactory().getErrorReporter();
        view.startingWith(this, panel);

        RegattaAndRaceIdentifier lifeRace = getPlace().getLifeRace();
        ArrayList<String> racesToShow = null;
        if (lifeRace != null) {
            racesToShow = new ArrayList<>();
            racesToShow.add(lifeRace.getRaceName());
            GWT.log("Limiting leaderboard to liferace " + lifeRace);
        }

        final LeaderboardSettings leaderboardSettings = LeaderboardSettingsFactory.getInstance()
                .createNewDefaultSettings(null, racesToShow, null, /* autoExpandFirstRace */ false,
                        /* showRegattaRank */ true, /* showCompetitorSailIdColumn */ true,
                        /* showCompetitorFullNameColumn */ true);
        GWT.log("event " + getSlideCtx().getEvent());
        List<StrippedLeaderboardDTO> leaderboards = getSlideCtx().getEvent().getLeaderboardGroups().get(0)
                .getLeaderboards();
        GWT.log("leaderboard result " + leaderboards);
        StrippedLeaderboardDTO leaderboard = leaderboards.get(0);
        competitorSelectionProvider = new CompetitorSelectionModel(/* hasMultiSelection */ false);
        leaderboardPanel = new SixtyInchLeaderBoard(sailingService, new AsyncActionsExecutor(), leaderboardSettings,
                false, lifeRace, competitorSelectionProvider, null, leaderboard.name, errorReporter,
                StringMessages.INSTANCE, null, /* showRaceDetails */false);
        view.setLeaderBoard(leaderboardPanel);
        selectNext();
        selectionTimer.scheduleRepeating(1000);

    }

    @Override
    public void onStop() {
        selectionTimer.cancel();
    }
}

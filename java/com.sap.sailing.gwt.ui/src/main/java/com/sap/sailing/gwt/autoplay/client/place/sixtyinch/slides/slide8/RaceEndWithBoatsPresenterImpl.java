package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide8;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SixtyInchLeaderBoard;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.ConfiguredSlideBase;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.MiniLeaderboardItemDTO;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardEntryPoint;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettingsFactory;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;

public class RaceEndWithBoatsPresenterImpl extends ConfiguredSlideBase<RaceEndWithBoatsPlace>
        implements RaceEndWithBoatsView.NextRaceWithBoatsPresenter {
    protected static final int SWITCH_COMPETITOR_DELAY = 2000;
    private RaceEndWithBoatsView view;
    private SixtyInchLeaderBoard leaderboardPanel;
    private CompetitorSelectionModel competitorSelectionProvider;

    public RaceEndWithBoatsPresenterImpl(RaceEndWithBoatsPlace place, AutoPlayClientFactorySixtyInch clientFactory,
            RaceEndWithBoatsView slide1ViewImpl) {
        super(place, clientFactory);
        this.view = slide1ViewImpl;
    }

    @Override
    public void startConfigured(AcceptsOneWidget panel) {
        SailingServiceAsync sailingService = getClientFactory().getSailingService();
        ErrorReporter errorReporter = getClientFactory().getErrorReporter();
        view.startingWith(this, panel);

        RegattaAndRaceIdentifier lifeRace = getPlace().getLastRace();

        final LeaderboardSettings leaderboardSettings = LeaderboardSettingsFactory.getInstance()
                .createNewDefaultSettings(null, null, null, /* autoExpandFirstRace */ false, /* showRegattaRank */ true,
                        /* showCompetitorSailIdColumn */ true, /* showCompetitorFullNameColumn */ true);
        competitorSelectionProvider = new CompetitorSelectionModel(/* hasMultiSelection */ false);

        com.sap.sse.gwt.client.player.Timer timer = new com.sap.sse.gwt.client.player.Timer(PlayModes.Live,
                PlayStates.Paused,
                /* delayBetweenAutoAdvancesInMilliseconds */ LeaderboardEntryPoint.DEFAULT_REFRESH_INTERVAL_MILLIS);
        leaderboardPanel = new SixtyInchLeaderBoard(sailingService, new AsyncActionsExecutor(), leaderboardSettings,
                false, lifeRace, competitorSelectionProvider, timer, null,
                getSlideCtx().getSettings().getLeaderBoardName(), errorReporter, StringMessages.INSTANCE, null, false,
                null, false, null, false, true, false, false, false);
        view.setLeaderBoard(leaderboardPanel);

        GetMiniLeaderboardDTO miniLeaderBoard = getPlace().getLeaderBoardDTO();
        ArrayList<MiniLeaderboardItemDTO> items = new ArrayList<MiniLeaderboardItemDTO>(miniLeaderBoard.getItems());
        Collections.sort(items, new Comparator<MiniLeaderboardItemDTO>() {
            @Override
            public int compare(MiniLeaderboardItemDTO o1, MiniLeaderboardItemDTO o2) {
                return Integer.compare(o1.getRank(), o2.getRank());
            }
        });

        if (items.size() >= 3) {
            MiniLeaderboardItemDTO first = items.get(0);
            MiniLeaderboardItemDTO second = items.get(0);
            MiniLeaderboardItemDTO third = items.get(0);

            for (CompetitorDTO c : leaderboardPanel.getCompetitors(lifeRace)) {
                if (first.getCompetitor().getSailID().equals(c.getSailID())) {
                    view.setFirst(c);
                }
                if (second.getCompetitor().getSailID().equals(c.getSailID())) {
                    view.setSecond(c);
                }
                if (third.getCompetitor().getSailID().equals(c.getSailID())) {
                    view.setThird(c);
                }
            }

        }

    }

    @Override
    public void onStop() {
        view.onStop();
    }
}

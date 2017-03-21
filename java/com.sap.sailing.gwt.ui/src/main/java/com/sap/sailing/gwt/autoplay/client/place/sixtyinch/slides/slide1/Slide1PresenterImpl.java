package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide1;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.events.MiniLeaderboardUpdatedEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SixtyInchLeaderBoard;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SlideHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.ConfiguredSlideBase;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettingsFactory;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;

public class Slide1PresenterImpl extends ConfiguredSlideBase<Slide1Place> implements Slide1View.Slide1Presenter {

    private Slide1View view;

    public Slide1PresenterImpl(Slide1Place place, AutoPlayClientFactorySixtyInch clientFactory,
            Slide1View slide1ViewImpl) {
        super(place, clientFactory);
        this.view = slide1ViewImpl;
    }

    @Override
    public void startConfigured(AcceptsOneWidget panel) {

        getEventBus()
                .fireEvent(new SlideHeaderEvent("i18n 5 Races Rank", getSlideCtx().getSettings().getLeaderBoardName()));

        SailingServiceAsync sailingService = getClientFactory().getSailingService();
        ErrorReporter errorReporter = getClientFactory().getErrorReporter();
        view.startingWith(this, panel);
        final CompetitorSelectionModel competitorSelectionProvider = new CompetitorSelectionModel(
                /* hasMultiSelection */ true);
        final LeaderboardSettings leaderboardSettings = LeaderboardSettingsFactory.getInstance()
                .createNewDefaultSettings(null, null, null, /* autoExpandFirstRace */ false, /* showRegattaRank */ true,
                        /* showCompetitorSailIdColumn */ true, /* showCompetitorFullNameColumn */ true);
        GWT.log("event " + getSlideCtx().getEvent());
        List<StrippedLeaderboardDTO> leaderboards = getSlideCtx().getEvent().getLeaderboardGroups().get(0)
                .getLeaderboards();
        GWT.log("leaderboard result " + leaderboards);
        StrippedLeaderboardDTO leaderboard = leaderboards.get(0);
        SixtyInchLeaderBoard leaderboardPanel = new SixtyInchLeaderBoard(sailingService, new AsyncActionsExecutor(),
                leaderboardSettings, false, /* preSelectedRace */null, competitorSelectionProvider, null,
                leaderboard.name, errorReporter, StringMessages.INSTANCE, null, /* showRaceDetails */false);
        panel.setWidget(leaderboardPanel);

        getEventBus().addHandler(MiniLeaderboardUpdatedEvent.TYPE, new MiniLeaderboardUpdatedEvent.Handler() {

            @Override
            public void handleNoOpEvent(MiniLeaderboardUpdatedEvent e) {
                view.setLeaderBoardDTO(getPlace().getLeaderBoardDTO());
            }

        });
        view.setLeaderBoardDTO(getPlace().getLeaderBoardDTO());
    }
}

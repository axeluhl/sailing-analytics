package com.sap.sailing.gwt.autoplay.client.places.screens.liferaceloop.raceboard;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.autoplay.client.app.ConfiguredPresenter;
import com.sap.sailing.gwt.autoplay.client.app.sixtyinch.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings.RaceColumnSelectionStrategies;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardEntryPoint;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;

public class LifeRaceWithRaceboardPresenterImpl extends ConfiguredPresenter<LifeRaceWithRaceboardPlace> implements LifeRaceWithRaceboardView.Slide7Presenter {
    protected static final int SWITCH_COMPETITOR_DELAY = 2000;
    private LifeRaceWithRaceboardView view;
    ArrayList<CompetitorDTO> compList = new ArrayList<>();

    public LifeRaceWithRaceboardPresenterImpl(LifeRaceWithRaceboardPlace place, AutoPlayClientFactorySixtyInch clientFactory,
            LifeRaceWithRaceboardView LifeRaceWithRacemapViewImpl) {
        super(place, clientFactory);
        this.view = LifeRaceWithRacemapViewImpl;
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
        null, racesToShow, null, false, null, null,
        /* ascending */ true, /* updateUponPlayStateChange */ true, RaceColumnSelectionStrategies.EXPLICIT,
        /* showAddedScores */ false, /* showOverallRacesCompleted */ false, false,
        true);

        List<StrippedLeaderboardDTO> leaderboards = getSlideCtx().getEvent().getLeaderboardGroups().get(0)
                .getLeaderboards();
        StrippedLeaderboardDTO leaderboard = leaderboards.get(0);

        com.sap.sse.gwt.client.player.Timer timer = new com.sap.sse.gwt.client.player.Timer(
                // perform the first request as "live" but don't by default auto-play
                PlayModes.Live, PlayStates.Playing,
                /* delayBetweenAutoAdvancesInMilliseconds */ LeaderboardEntryPoint.DEFAULT_REFRESH_INTERVAL_MILLIS);

        view.startingWith(this, panel, getPlace().getRaceMap());
    }

    @Override
    public void onStop() {
        view.onStop();
    }

}

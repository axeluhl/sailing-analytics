package com.sap.sailing.gwt.autoplay.client.nodes;

import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.app.AutoplayPerspectiveLifecycle;
import com.sap.sailing.gwt.autoplay.client.app.AutoplayPerspectiveOwnSettings;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.nodes.base.FiresPlaceNode;
import com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.leaderboard.LeaderboardPlace;
import com.sap.sailing.gwt.autoplay.client.shared.leaderboard.LeaderboardWithHeaderPerspective;
import com.sap.sailing.gwt.autoplay.client.shared.leaderboard.LeaderboardWithHeaderPerspectiveSettings;
import com.sap.sailing.gwt.autoplay.client.utils.AutoplayHelper;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardEntryPoint;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;

public class IdleRaceLeaderboard extends FiresPlaceNode {
    private static final int REFRESH_INTERVAL_IN_MILLIS_LEADERBOARD = 10000;
    private final AutoPlayClientFactory cf;
    private final Timer leaderboardTimer;

    public IdleRaceLeaderboard(AutoPlayClientFactory cf) {
        super(IdleRaceLeaderboard.class.getName());
        this.cf = cf;
        PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings> settings = cf.getAutoPlayCtx()
                .getAutoplaySettings();
        leaderboardTimer = new Timer(PlayModes.Live,
                /* delayBetweenAutoAdvancesInMilliseconds */ LeaderboardEntryPoint.DEFAULT_REFRESH_INTERVAL_MILLIS);
        leaderboardTimer
                .setLivePlayDelayInMillis(settings.getPerspectiveOwnSettings().getTimeToSwitchBeforeRaceStart());
        leaderboardTimer.setRefreshInterval(REFRESH_INTERVAL_IN_MILLIS_LEADERBOARD);
    }

    public void onStart() {
        leaderboardTimer.play();
        PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings> settings = cf.getAutoPlayCtx()
                .getAutoplaySettings();
        AutoplayPerspectiveLifecycle autoplayLifecycle = cf.getAutoPlayCtx().getAutoplayLifecycle();
        boolean withFullscreenButton = settings.getPerspectiveOwnSettings().isFullscreen();
        PerspectiveCompositeSettings<LeaderboardWithHeaderPerspectiveSettings> leaderboardSettings = settings
                .findSettingsByComponentId(autoplayLifecycle.getLeaderboardLifecycle().getComponentId());

        StringMessages stringMessages = StringMessages.INSTANCE;

        LeaderboardWithHeaderPerspective leaderboardPerspective = new LeaderboardWithHeaderPerspective(null, null,
                autoplayLifecycle.getLeaderboardLifecycle(), leaderboardSettings, cf.getSailingService(),
                cf.getUserService(), AutoplayHelper.asyncActionsExecutor,
                new CompetitorSelectionModel(/* hasMultiSelection */ true), leaderboardTimer,
                cf.getAutoPlayCtx().getContextDefinition().getLeaderboardName(), cf.getErrorReporter(), stringMessages,
                withFullscreenButton);
        setPlaceToGo(new LeaderboardPlace(leaderboardPerspective));
        StrippedLeaderboardDTO leaderboard = AutoplayHelper.getSelectedLeaderboard(cf.getAutoPlayCtx().getEvent(),
                cf.getAutoPlayCtx().getContextDefinition().getLeaderboardName());
        String title = stringMessages.leaderboard() + ": "
                + (leaderboard.getDisplayName() == null ? leaderboard.name : leaderboard.getDisplayName());
        getBus().fireEvent(new AutoPlayHeaderEvent(title, ""));
        firePlaceChangeAndStartTimer();
    };

    @Override
    public void onStop() {
        leaderboardTimer.pause();
        leaderboardTimer.reset();
    }
}

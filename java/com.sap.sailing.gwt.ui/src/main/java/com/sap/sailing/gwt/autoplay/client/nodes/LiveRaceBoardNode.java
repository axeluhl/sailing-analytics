package com.sap.sailing.gwt.autoplay.client.nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.app.AutoplayPerspectiveLifecycle;
import com.sap.sailing.gwt.autoplay.client.app.AutoplayPerspectiveOwnSettings;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.nodes.base.FiresPlaceNode;
import com.sap.sailing.gwt.autoplay.client.places.screens.liveraceloop.raceboard.LiveRaceWithRaceboardPlace;
import com.sap.sailing.gwt.autoplay.client.utils.AutoplayHelper;
import com.sap.sailing.gwt.settings.client.raceboard.RaceBoardPerspectiveOwnSettings;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPanel;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.RaceboardDataDTO;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.security.ui.client.UserService;

public class LiveRaceBoardNode extends FiresPlaceNode implements RaceTimesInfoProviderListener {
    private static final long RACETIME_UPDATE_INTERVAL = 3000l;
    private static final long TIMER_ADVANCE_STEPSIZE = /* delayBetweenAutoAdvancesInMilliseconds */1000l;
    private static final long REFRESH_INTERVAL_IN_MILLIS_RACEBOARD = 1000;
    private final AutoPlayClientFactory cf;
    private final Timer raceboardTimer;
    private final RaceTimesInfoProvider raceTimesInfoProvider;


    public LiveRaceBoardNode(AutoPlayClientFactory cf) {
        super(LiveRaceBoardNode.class.getName());
        this.cf = cf;
        SailingServiceAsync sailingService = cf.getSailingService();

        raceboardTimer = new Timer(PlayModes.Live, TIMER_ADVANCE_STEPSIZE);
        raceboardTimer.setRefreshInterval(REFRESH_INTERVAL_IN_MILLIS_RACEBOARD);
        raceboardTimer.play();
        raceTimesInfoProvider = new RaceTimesInfoProvider(sailingService, AutoplayHelper.asyncActionsExecutor,
                cf.getErrorReporter(),
                new ArrayList<RegattaAndRaceIdentifier>(), RACETIME_UPDATE_INTERVAL);
        raceTimesInfoProvider.addRaceTimesInfoProviderListener(this);

    }

    public void onStart() {
        raceboardTimer.setTime(MillisecondsTimePoint.now().asMillis());
        raceboardTimer.play();
        raceTimesInfoProvider.addRaceIdentifier(cf.getAutoPlayCtxSignalError().getLifeOrPreLiveRace(), true);

        PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings> settings = cf.getAutoPlayCtxSignalError().getAutoplaySettings();
        AutoplayPerspectiveLifecycle autoplayLifecycle = cf.getAutoPlayCtxSignalError().getAutoplayLifecycle();
        UserService userService = cf.getUserService();
        SailingServiceAsync sailingService = cf.getSailingService();
        MediaServiceAsync mediaService = cf.getMediaService();

        AsyncCallback<RaceboardDataDTO> raceBoardDataCallback = new AsyncCallback<RaceboardDataDTO>() {
            @Override
            public void onSuccess(RaceboardDataDTO result) {

                PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> raceboardSettings = settings
                        .findSettingsByComponentId(autoplayLifecycle.getRaceboardLifecycle().getComponentId());
                RaceBoardPanel raceboardPerspective = new RaceBoardPanel(null, null,
                        autoplayLifecycle.getRaceboardLifecycle(), raceboardSettings, sailingService, mediaService,
                        userService, AutoplayHelper.asyncActionsExecutor, result.getCompetitorAndTheirBoats(),
                        raceboardTimer,
                        cf.getAutoPlayCtxSignalError().getLifeOrPreLiveRace(), cf.getAutoPlayCtxSignalError().getContextDefinition().getLeaderboardName(),
                        /** leaderboardGroupName */
                        null, /** eventId */
                        null, cf.getErrorReporter(), StringMessages.INSTANCE, null, raceTimesInfoProvider, true, false, Arrays.asList(DetailType.values()));
                setPlaceToGo(new LiveRaceWithRaceboardPlace(raceboardPerspective));
                firePlaceChangeAndStartTimer();

                getBus().fireEvent(new AutoPlayHeaderEvent(cf.getAutoPlayCtxSignalError().getLifeOrPreLiveRace().getRegattaName(),
                        cf.getAutoPlayCtxSignalError().getLifeOrPreLiveRace().getRaceName()));
            }

            @Override
            public void onFailure(Throwable caught) {
                cf.getErrorReporter().reportError("Error while loading data for raceboard: " + caught.getMessage());
                getBus().fireEvent(new AutoPlayHeaderEvent("", ""));
            }
        };
        sailingService.getRaceboardData(cf.getAutoPlayCtxSignalError().getLifeOrPreLiveRace().getRegattaName(),
                cf.getAutoPlayCtxSignalError().getLifeOrPreLiveRace().getRaceName(), cf.getAutoPlayCtxSignalError().getContextDefinition().getLeaderboardName(), null,
                null, raceBoardDataCallback);
    };

    @Override
    public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo,
            long clientTimeWhenRequestWasSent, Date serverTimeDuringRequest, long clientTimeWhenResponseWasReceived) {
        raceboardTimer.adjustClientServerOffset(clientTimeWhenRequestWasSent, serverTimeDuringRequest,
                clientTimeWhenResponseWasReceived);
    }

    @Override
    public void onStop() {
        raceboardTimer.pause();
        raceboardTimer.reset();
    }
}

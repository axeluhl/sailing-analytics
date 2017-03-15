package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide7;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.events.RaceTimeInfoProviderUpdatedEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SlideHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.ConfiguredSlideBase;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPanel;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveLifecycle;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveSettings;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.TimeListener;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycleWithAllSettings;
import com.sap.sse.security.ui.client.UserService;

public class Slide7PresenterImpl extends ConfiguredSlideBase<Slide7Place> implements Slide7View.Slide7Presenter {

    private Slide7View view;
    private Timer raceboardTimer;
    private static RaceBoardPanel raceboardPerspective;
    private static RegattaAndRaceIdentifier lastLiveRace;

    public Slide7PresenterImpl(Slide7Place place, AutoPlayClientFactorySixtyInch clientFactory,
            Slide7View slide7ViewImpl) {
        super(place, clientFactory);
        this.view = slide7ViewImpl;

    }

    @Override
    public void startConfigured(AcceptsOneWidget panel) {
        if (getSlideCtx().getCurrentLiveRace() == null) {
            getEventBus().fireEvent(
                    new SlideHeaderEvent("Currently Live", getSlideCtx().getCurrentLiveRace().getRaceName()));
        } else {
            getEventBus().fireEvent(new SlideHeaderEvent("Currently Live", ""));
        }
        view.startingWith(this, panel);

        raceboardTimer = new Timer(PlayModes.Live, /* delayBetweenAutoAdvancesInMilliseconds */1000l);
        raceboardTimer.setLivePlayDelayInMillis(1000);
        raceboardTimer.setRefreshInterval(10000);
        raceboardTimer.play();

        raceboardTimer.addTimeListener(new TimeListener() {
            @Override
            public void timeChanged(Date newTime, Date oldTime) {
                GWT.log("Time change " + newTime);
            }
        });

        getEventBus().addHandler(RaceTimeInfoProviderUpdatedEvent.TYPE, new RaceTimeInfoProviderUpdatedEvent.Handler() {
            @Override
            public void handleNoOpEvent(RaceTimeInfoProviderUpdatedEvent e) {
                raceTimeInfosReceived();
            }
        });

        if (raceboardPerspective != null) {
            view.setRaceMap(raceboardPerspective);
        } else {
            view.showErrorNoLive();
        }
    }

    protected void raceTimeInfosReceived() {
        GWT.log("raceTimesInfosReceived");
        long clientTimeWhenRequestWasSent = getSlideCtx().getClientTimeWhenRequestWasSent();
        Date serverTimeDuringRequest = getSlideCtx().getServerTimeDuringRequest();
        long clientTimeWhenResponseWasReceived = getSlideCtx().getClientTimeWhenResponseWasReceived();
        raceboardTimer.adjustClientServerOffset(clientTimeWhenRequestWasSent, serverTimeDuringRequest,
                clientTimeWhenResponseWasReceived);

        RegattaAndRaceIdentifier currentLiveRace = getSlideCtx().getCurrentLiveRace();
        if (currentLiveRace == null) {
            view.showErrorNoLive();
        } else {
            // life race changed, switch to it!
            if ((lastLiveRace == null && !(currentLiveRace == null))
                    || (lastLiveRace != null && !lastLiveRace.equals(currentLiveRace))) {
                if (getSlideCtx().getRaceboardResult() != null) {
                    createRaceMapIfNotExist(currentLiveRace);
                }
            }
            lastLiveRace = currentLiveRace;
        }

    }

    private void createRaceMapIfNotExist(RegattaAndRaceIdentifier currentLiveRace) {
        String leaderBoardName = getSlideCtx().getSettings().getLeaderBoardName();

        SailingServiceAsync sailingService = getClientFactory().getSailingService();
        MediaServiceAsync mediaService = getClientFactory().getMediaService();
        UserService userService = getClientFactory().getUserService();
        AsyncActionsExecutor asyncActionsExecutor = new AsyncActionsExecutor();
        ErrorReporter errorReporter = getClientFactory().getErrorReporter();

        EventDTO event = getClientFactory().getSlideCtx().getEvent();
        StrippedLeaderboardDTO selectedLeaderboard = getSelectedLeaderboard(event, leaderBoardName);

        RaceBoardPerspectiveLifecycle raceboardPerspectiveLifecycle = new RaceBoardPerspectiveLifecycle(
                selectedLeaderboard, StringMessages.INSTANCE);
        PerspectiveLifecycleWithAllSettings<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveSettings> raceboardPerspectiveLifecyclesAndSettings = new PerspectiveLifecycleWithAllSettings<>(
                raceboardPerspectiveLifecycle, raceboardPerspectiveLifecycle.createDefaultSettings());

        raceboardPerspective = new RaceBoardPanel(raceboardPerspectiveLifecyclesAndSettings,
                sailingService, mediaService, userService, asyncActionsExecutor,
                getSlideCtx().getRaceboardResult().getCompetitorAndTheirBoats(),
                raceboardTimer, currentLiveRace, leaderBoardName,
                /** leaderboardGroupName */
                null, /** eventId */
                null, errorReporter, StringMessages.INSTANCE, null, getSlideCtx().getRaceTimesInfoProvider(), true);
        raceboardTimer.setPlayMode(PlayModes.Live);
        view.setRaceMap(raceboardPerspective);

    }

    private StrippedLeaderboardDTO getSelectedLeaderboard(EventDTO event, String leaderBoardName) {
        for (LeaderboardGroupDTO leaderboardGroup : event.getLeaderboardGroups()) {
            for (StrippedLeaderboardDTO leaderboard : leaderboardGroup.getLeaderboards()) {
                if (leaderboard.name.equals(leaderBoardName)) {
                    return leaderboard;
                }
            }
        }
        return null;
    }
}

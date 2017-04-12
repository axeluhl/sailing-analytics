package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.events.AutoplayFailureEvent;
import com.sap.sailing.gwt.autoplay.client.events.UpcomingLiferaceDetectedEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.HelperSixty;

public class UpcomingRacesMonitor {
    private final AutoPlayClientFactorySixtyInch cf;
    private String leaderBoardName;
    private int errorCount = 0;;
    private Timer checkTimer = new Timer() {
        @Override
        public void run() {
            doCheck();
        }
    };

    public UpcomingRacesMonitor(AutoPlayClientFactorySixtyInch cf) {
        this.cf = cf;
    }

    public void startMonitoring() {
        doCheck();
    }

    private void doCheck() {
        if (cf.getSlideCtx() == null || //
                cf.getSlideCtx().getSettings() == null || //
                cf.getSlideCtx().getEvent() == null //
        ) {
            // data not loaded yet
            checkTimer.schedule(5000);
            return;
        }

        this.leaderBoardName = cf.getSlideCtx().getSettings().getLeaderBoardName();
        HelperSixty.getLifeRace(cf.getSailingService(), cf.getErrorReporter(), cf.getSlideCtx().getEvent(),
                leaderBoardName, cf.getDispatch(), new AsyncCallback<RegattaAndRaceIdentifier>() {
                    @Override
                    public void onSuccess(RegattaAndRaceIdentifier lifeRace) {
                        errorCount = 0;
                        if (lifeRace == null) {
                            checkTimer.schedule(5000);
                        } else {
                            cf.getEventBus().fireEvent(new UpcomingLiferaceDetectedEvent(lifeRace));
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorCount++;
                        if (errorCount > 5) {
                            cf.getEventBus().fireEvent(new AutoplayFailureEvent(caught));
                        }
                    }
                });
    }
}

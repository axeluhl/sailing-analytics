package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayNodeTransitionRequestEvent;
import com.sap.sailing.gwt.autoplay.client.events.AutoplayFailureEvent;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.AutoPlayNodeController;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.impl.AutoPlayLoopNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.HelperSixty;
import com.sap.sse.common.Util.Pair;

public class LifeRaceStateController implements AutoPlayNodeController {
    private final AutoPlayClientFactorySixtyInch cf;
    private String leaderBoardName;
    private int errorCount = 0;;
    private RegattaAndRaceIdentifier currentPreLifeRace;
    private RegattaAndRaceIdentifier currentLifeRace;
    private Timer checkTimer = new Timer() {
        @Override
        public void run() {
            doCheck();
        }
    };

    private AutoPlayNodeController idleLoop;
    private AutoPlayNodeController preLifeRaceLoop;
    private AutoPlayNodeController lifeRaceLoop;
    private AutoPlayNodeController afterLifeRaceLoop;

    public LifeRaceStateController(AutoPlayClientFactorySixtyInch cf, AutoPlayLoopNode idleLoop,
            AutoPlayLoopNode preLifeRaceLoop, AutoPlayLoopNode lifeRaceLoop, AutoPlayLoopNode afterLifeRaceLoop) {

        this.cf = cf;
        this.idleLoop = idleLoop;
        this.preLifeRaceLoop = preLifeRaceLoop;
        this.lifeRaceLoop = lifeRaceLoop;
        this.afterLifeRaceLoop = afterLifeRaceLoop;

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
                leaderBoardName, cf.getDispatch(), new AsyncCallback<Pair<Long, RegattaAndRaceIdentifier>>() {
                    @Override
                    public void onSuccess(Pair<Long, RegattaAndRaceIdentifier> result) {
                        errorCount = 0;
                        final Long timeToRaceStartInMs = result.getA();
                        final RegattaAndRaceIdentifier loadedLifeRace = result.getB();

                        if (loadedLifeRace == null) {
                            boolean comingFromLiferace = currentLifeRace != null || currentPreLifeRace != null;
                            currentLifeRace = null;
                            currentPreLifeRace = null;
                            GWT.log("FallbackToIdleLoopEvent: isComingFromLiferace: " + true);
                            cf.getEventBus().fireEvent(new AutoPlayNodeTransitionRequestEvent(
                                    comingFromLiferace ? afterLifeRaceLoop : idleLoop));
                        } else if (/* is pre liferace */ timeToRaceStartInMs > 10000) {
                            if (/* is new pre life race */!loadedLifeRace.equals(currentPreLifeRace)) {
                                currentPreLifeRace = loadedLifeRace;
                                currentLifeRace = null;
                                GWT.log("UpcomingLiferaceDetectedEvent: " + loadedLifeRace.toString());
                                cf.getEventBus().fireEvent(new AutoPlayNodeTransitionRequestEvent(afterLifeRaceLoop));
                            }
                        } else /* is life race */ {
                            currentPreLifeRace = null;
                            if (/* is new life race */!loadedLifeRace.equals(currentLifeRace)) {
                                currentLifeRace = loadedLifeRace;
                                cf.getEventBus().fireEvent(new AutoPlayNodeTransitionRequestEvent(lifeRaceLoop));

                            }
                        }
                        checkTimer.schedule(5000);
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

    @Override
    public void start(EventBus eventBus) {
        // TODO Auto-generated method stub
    }

    @Override
    public void doSuspend() {
        // TODO Auto-generated method stub
    }

    @Override
    public void doContinue() {
        // TODO Auto-generated method stub
    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub
    }
}

package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.events.AutoplayFailureEvent;
import com.sap.sailing.gwt.autoplay.client.events.DataLoadFailureEvent;
import com.sap.sailing.gwt.autoplay.client.events.FailureEvent;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.AutoPlayNode;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.impl.BaseCompositeNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.HelperSixty;
import com.sap.sse.common.Util.Pair;

public class SixtyInchRootNode extends BaseCompositeNode {
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
    private AutoPlayNode idleLoop;
    private AutoPlayNode preLifeRaceLoop;
    private AutoPlayNode lifeRaceLoop;
    private AutoPlayNode afterLifeRaceLoop;

    public SixtyInchRootNode(AutoPlayClientFactorySixtyInch cf, AutoPlayNode idleLoop, AutoPlayNode preLifeRaceLoop,
            AutoPlayNode lifeRaceLoop, AutoPlayNode afterLifeRaceLoop) {
        this.cf = cf;
        this.idleLoop = idleLoop;
        this.preLifeRaceLoop = preLifeRaceLoop;
        this.lifeRaceLoop = lifeRaceLoop;
        this.afterLifeRaceLoop = afterLifeRaceLoop;
    }

    private void doCheck() {
        this.leaderBoardName = cf.getSlideCtx().getSettings().getLeaderBoardName();
        HelperSixty.getLifeRace(cf.getSailingService(), cf.getErrorReporter(), cf.getSlideCtx().getEvent(),
                leaderBoardName, cf.getDispatch(), new AsyncCallback<Pair<Long, RegattaAndRaceIdentifier>>() {
                    @Override
                    public void onSuccess(Pair<Long, RegattaAndRaceIdentifier> result) {
                        errorCount = 0;
                        if (result == null) {
                            boolean comingFromLiferace = currentLifeRace != null || currentPreLifeRace != null;
                            currentLifeRace = null;
                            currentPreLifeRace = null;
                            GWT.log("FallbackToIdleLoopEvent: isComingFromLiferace: " + true);
                            transitionTo(comingFromLiferace ? afterLifeRaceLoop : idleLoop);
                        } else {
                            final Long timeToRaceStartInMs = result.getA();
                            final RegattaAndRaceIdentifier loadedLifeRace = result.getB();
                            if (loadedLifeRace == null) {
                                boolean comingFromLiferace = currentLifeRace != null || currentPreLifeRace != null;
                                currentLifeRace = null;
                                currentPreLifeRace = null;
                                GWT.log("FallbackToIdleLoopEvent: isComingFromLiferace: " + true);
                                transitionTo(comingFromLiferace ? afterLifeRaceLoop : idleLoop);
                            } else if (/* is pre liferace */ timeToRaceStartInMs > 10000) {
                                if (/* is new pre life race */!loadedLifeRace.equals(currentPreLifeRace)) {
                                    currentPreLifeRace = loadedLifeRace;
                                    currentLifeRace = null;
                                    GWT.log("UpcomingLiferaceDetectedEvent: " + loadedLifeRace.toString());
                                    transitionTo(preLifeRaceLoop);
                                }
                            } else /* is life race */ {
                                currentPreLifeRace = null;
                                if (/* is new life race */!loadedLifeRace.equals(currentLifeRace)) {
                                    currentLifeRace = loadedLifeRace;
                                    transitionTo(lifeRaceLoop);
                                }
                            }
                        }
                        checkTimer.schedule(5000);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorCount++;
                        if (errorCount > 5) {
                            transitionTo(idleLoop);
                            cf.getEventBus().fireEvent(new AutoplayFailureEvent(caught));
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        if (cf.getSlideCtx() == null || //
                cf.getSlideCtx().getSettings() == null || //
                cf.getSlideCtx().getEvent() == null //
        ) {
            // data not loaded yet
            throw new RuntimeException("No event loaded");
        }
        getBus().addHandler(AutoplayFailureEvent.TYPE, new AutoplayFailureEvent.Handler() {
            @Override
            public void onFailure(AutoplayFailureEvent e) {
                processFailure(e);
            }
        });
        getBus().addHandler(DataLoadFailureEvent.TYPE, new DataLoadFailureEvent.Handler() {
            @Override
            public void onLoadFailure(DataLoadFailureEvent e) {
                processFailure(e);
            }
        });
        doCheck();
        transitionTo(idleLoop);
    }

    @Override
    public void onStop() {
        checkTimer.cancel();
    }

    private void processFailure(FailureEvent event) {
        GWT.log("Captured failure event: " + event);
        if (event.getCaught() != null) {
            event.getCaught().printStackTrace();
        }
        transitionTo(idleLoop);
    }
}

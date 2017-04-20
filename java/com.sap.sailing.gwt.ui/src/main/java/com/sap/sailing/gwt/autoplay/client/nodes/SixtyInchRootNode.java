package com.sap.sailing.gwt.autoplay.client.nodes;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.autoplay.client.app.sixtyinch.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayFailureEvent;
import com.sap.sailing.gwt.autoplay.client.events.FailureEvent;
import com.sap.sailing.gwt.autoplay.client.nodes.base.AutoPlayLoopNode;
import com.sap.sailing.gwt.autoplay.client.nodes.base.AutoPlayNode;
import com.sap.sailing.gwt.autoplay.client.nodes.base.BaseCompositeNode;
import com.sap.sailing.gwt.autoplay.client.utils.HelperSixty;
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
    private AutoPlayNode preLiveRaceLoop;
    private AutoPlayNode liveRaceLoop;
    private AutoPlayNode afterLiveRaceLoop;

    public SixtyInchRootNode(AutoPlayClientFactorySixtyInch cf, AutoPlayNode idleLoop, AutoPlayNode preLiveRaceLoop,
            AutoPlayNode liveRaceLoop, AutoPlayLoopNode afterLiveRaceLoop) {
        this.cf = cf;
        this.idleLoop = idleLoop;
        this.preLiveRaceLoop = preLiveRaceLoop;
        this.liveRaceLoop = liveRaceLoop;
        this.afterLiveRaceLoop = afterLiveRaceLoop;

        afterLiveRaceLoop.setOnLoopEnd(new Command() {
            @Override
            public void execute() {
                transitionTo(idleLoop);
            }
        });
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
                            transitionTo(comingFromLiferace ? afterLiveRaceLoop : idleLoop);
                        } else {
                            final Long timeToRaceStartInMs = result.getA();
                            final RegattaAndRaceIdentifier loadedLiveRace = result.getB();
                            if (loadedLiveRace == null) {
                                boolean comingFromLiferace = currentLifeRace != null || currentPreLifeRace != null;
                                currentLifeRace = null;
                                currentPreLifeRace = null;
                                GWT.log("FallbackToIdleLoopEvent: isComingFromLiferace: " + true);
                                transitionTo(comingFromLiferace ? afterLiveRaceLoop : idleLoop);
                            } else if (/* is pre liverace */ timeToRaceStartInMs > 10000) {
                                if (/* is new pre live race */!loadedLiveRace.equals(currentPreLifeRace)) {
                                    currentPreLifeRace = loadedLiveRace;
                                    currentLifeRace = null;
                                    GWT.log("UpcomingLiferaceDetectedEvent: " + loadedLiveRace.toString());
                                    transitionTo(preLiveRaceLoop);
                                }
                            } else /* is live race */ {
                                currentPreLifeRace = null;
                                if (/* is new live race */!loadedLiveRace.equals(currentLifeRace)) {
                                    currentLifeRace = loadedLiveRace;
                                    transitionTo(liveRaceLoop);
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
                            cf.getEventBus().fireEvent(new AutoPlayFailureEvent(caught));
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
        getBus().addHandler(AutoPlayFailureEvent.TYPE, new AutoPlayFailureEvent.Handler() {
            @Override
            public void onFailure(AutoPlayFailureEvent e) {
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

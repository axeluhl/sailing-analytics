package com.sap.sailing.gwt.autoplay.client.nodes;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayFailureEvent;
import com.sap.sailing.gwt.autoplay.client.events.FailureEvent;
import com.sap.sailing.gwt.autoplay.client.nodes.base.AutoPlayNode;
import com.sap.sailing.gwt.autoplay.client.nodes.base.BaseCompositeNode;
import com.sap.sailing.gwt.autoplay.client.utils.AutoplayHelper;
import com.sap.sse.common.Util.Pair;

public class ClassicRootNode extends BaseCompositeNode {
    protected static final long LIVE_SWITCH_DELAY = 1000;
    private final AutoPlayClientFactory cf;
    private String leaderBoardName;
    private int errorCount = 0;;
    private Timer checkTimer = new Timer() {
        @Override
        public void run() {
            doCheck();
        }
    };
    private AutoPlayNode idle;
    private AutoPlayNode live;

    public ClassicRootNode(AutoPlayClientFactory cf, LiveRaceLeaderboard idle, LiveRaceBoardNode live) {
        this.cf = cf;
        this.idle = idle;
        this.live = live;
    }

    private void doCheck() {
        this.leaderBoardName = cf.getSlideCtx().getSettings().getLeaderboardName();
        AutoplayHelper.getLifeRace(cf.getSailingService(), cf.getErrorReporter(), cf.getSlideCtx().getEvent(),
                leaderBoardName, cf.getDispatch(), new AsyncCallback<Pair<Long, RegattaAndRaceIdentifier>>() {
                    @Override
                    public void onSuccess(Pair<Long, RegattaAndRaceIdentifier> result) {
                        errorCount = 0;
                        if (result == null) {
                            cf.getSlideCtx().setCurrenLifeRace(null);
                            transitionTo(idle);
                        } else {
                            cf.getSlideCtx().setCurrenLifeRace(result.getB());
                            final Long timeToRaceStartInMs = result.getA();
                            final RegattaAndRaceIdentifier loadedLiveRace = result.getB();
                            if (loadedLiveRace == null || timeToRaceStartInMs > LIVE_SWITCH_DELAY) {
                                cf.getSlideCtx().setCurrenLifeRace(null);
                                transitionTo(idle);
                            } else /* is live race */ {
                                transitionTo(live);
                            }
                        }
                        checkTimer.schedule(5000);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorCount++;
                        if (errorCount > 5) {
                            transitionTo(idle);
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
        transitionTo(idle);
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
        transitionTo(idle);
    }
}

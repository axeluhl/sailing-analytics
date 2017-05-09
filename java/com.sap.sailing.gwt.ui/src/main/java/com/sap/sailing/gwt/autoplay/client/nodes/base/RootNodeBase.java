package com.sap.sailing.gwt.autoplay.client.nodes.base;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayFailureEvent;
import com.sap.sailing.gwt.autoplay.client.events.FailureEvent;
import com.sap.sailing.gwt.autoplay.client.utils.AutoplayHelper;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.common.Util.Pair;

public abstract class RootNodeBase extends BaseCompositeNode {
    protected static final long PRE_RACE_DELAY = 180000;
    protected static final long LIVE_SWITCH_DELAY = 1000;

    private final AutoPlayClientFactory cf;
    private String leaderBoardName;
    private int errorCount = 0;;
    private RootNodeState currentState;
    private RegattaAndRaceIdentifier currentPreLifeRace;
    private RegattaAndRaceIdentifier currentLifeRace;
    private Timer checkTimer = new Timer() {
        @Override
        public void run() {
            doCheck();
        }
    };
    protected RootNodeBase(AutoPlayClientFactory cf) {
        this.cf = cf;
    }

    @Override
    public final void onStart() {
        if (cf.getSlideCtx() == null || //
                cf.getSlideCtx().getSettings() == null//
        ) {
            backToConfig();
            return;
        }
        getBus().addHandler(AutoPlayFailureEvent.TYPE, new AutoPlayFailureEvent.Handler() {
            @Override
            public void onFailure(AutoPlayFailureEvent e) {
                processFailure(e);
            }
        });
        doCheck();
    }

    @Override
    public final void onStop() {
        checkTimer.cancel();
    }

    private void doCheck() {
        if (currentState == RootNodeState.AFTER_LIVE) {
            checkTimer.schedule(5000);
            GWT.log("do change state while in afterrace");
            return;
        }
        final UUID eventUUID = cf.getSlideCtx().getSettings().getEventId();
        cf.getSailingService().getEventById(eventUUID, true, new AsyncCallback<EventDTO>() {
            @Override
            public void onSuccess(final EventDTO event) {
                cf.getSlideCtx().updateEvent(event);
                _doCheck();
            }

            @Override
            public void onFailure(Throwable caught) {
                getBus().fireEvent(new AutoPlayFailureEvent(caught, "Error loading Event with id " + eventUUID));
            }
        });
    }

    private void _doCheck() {
        this.leaderBoardName = cf.getSlideCtx().getSettings().getLeaderboardName();
        AutoplayHelper.getLifeRace(cf.getSailingService(), cf.getErrorReporter(), cf.getSlideCtx().getEvent(),
                leaderBoardName, cf.getDispatch(), new AsyncCallback<Pair<Long, RegattaAndRaceIdentifier>>() {
                    @Override
                    public void onSuccess(Pair<Long, RegattaAndRaceIdentifier> result) {
                        errorCount = 0;
                        if (result == null) {
                            cf.getSlideCtx().setCurrenLifeRace(null);
                            boolean comingFromLiferace = currentLifeRace != null || currentPreLifeRace != null;
                            setCurrentState(comingFromLiferace ? RootNodeState.IDLE : RootNodeState.AFTER_LIVE,
                                    currentState);
                            currentLifeRace = null;
                            currentPreLifeRace = null;
                        } else {
                            cf.getSlideCtx().setCurrenLifeRace(result.getB());
                            final Long timeToRaceStartInMs = result.getA();
                            final RegattaAndRaceIdentifier loadedLiveRace = result.getB();
                            if (loadedLiveRace == null || timeToRaceStartInMs > PRE_RACE_DELAY) {
                                boolean comingFromLiferace = currentLifeRace != null || currentPreLifeRace != null;
                                GWT.log("FallbackToIdleLoopEvent: isComingFromLiferace: " + comingFromLiferace);
                                setCurrentState(comingFromLiferace ? RootNodeState.IDLE : RootNodeState.AFTER_LIVE,
                                        currentState);
                                currentLifeRace = null;
                                currentPreLifeRace = null;
                            } else if (/* is pre liverace */ timeToRaceStartInMs < PRE_RACE_DELAY
                                    && timeToRaceStartInMs > LIVE_SWITCH_DELAY) {
                                if (/* is new pre live race */!loadedLiveRace.equals(currentPreLifeRace)) {
                                    currentPreLifeRace = loadedLiveRace;
                                    currentLifeRace = null;
                                    GWT.log("UpcomingLiferaceDetectedEvent: " + loadedLiveRace.toString());
                                    setCurrentState(RootNodeState.PRE_RACE, currentState);
                                }
                            } else /* is live race */ {
                                currentPreLifeRace = null;
                                if (/* is new live race */!loadedLiveRace.equals(currentLifeRace)) {
                                    currentLifeRace = loadedLiveRace;
                                    setCurrentState(RootNodeState.LIVE, currentState);
                                }
                            }
                        }
                        checkTimer.schedule(5000);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorCount++;
                        if (errorCount > 5) {
                            cf.getEventBus().fireEvent(new AutoPlayFailureEvent(caught));
                        }
                    }
                });
    }

    private final void setCurrentState(RootNodeState goingTo, RootNodeState comingFrom) {
        if (goingTo != comingFrom) {
            this.currentState = goingTo;
            processStateTransition(goingTo, comingFrom);
        } else {
            GWT.log("Transition to same autoplay state, skipping");
        }
    }

    public AutoPlayClientFactory getClientFactory() {
        return cf;
    }

    protected void backToConfig() {
        cf.getPlaceController().goTo(cf.getDefaultPlace());
    }

    protected abstract void processStateTransition(RootNodeState goingTo, RootNodeState comingFrom);

    protected abstract void processFailure(FailureEvent event);
}

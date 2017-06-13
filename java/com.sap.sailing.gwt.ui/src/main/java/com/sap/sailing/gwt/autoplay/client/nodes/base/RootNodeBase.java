package com.sap.sailing.gwt.autoplay.client.nodes.base;

import java.util.UUID;

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
    private int UPDATE_STATE_TIMER = 5000;
    private final AutoPlayClientFactory cf;
    private String leaderBoardName;
    private int errorCount = 0;;
    private RootNodeState currentState;
    private Timer checkTimer = new Timer() {
        @Override
        public void run() {
            doCheck();
        }
    };

    protected RootNodeBase(String name, AutoPlayClientFactory cf) {
        super(name);
        this.cf = cf;
    }

    @Override
    public final void onStart() {
        if (cf.getAutoPlayCtx() == null || //
                cf.getAutoPlayCtx().getContextDefinition() == null//
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
        // start next update, to ensure it is done no matter any error cases
        checkTimer.schedule(UPDATE_STATE_TIMER);
        final UUID eventUUID = cf.getAutoPlayCtx().getContextDefinition().getEventId();
        cf.getSailingService().getEventById(eventUUID, true, new AsyncCallback<EventDTO>() {
            @Override
            public void onSuccess(final EventDTO event) {
                cf.getAutoPlayCtx().updateEvent(event);
                _doCheck();
            }

            @Override
            public void onFailure(Throwable caught) {
                getBus().fireEvent(new AutoPlayFailureEvent(caught, "Error loading Event with id " + eventUUID));
            }
        });
    }

    private void _doCheck() {

        final RegattaAndRaceIdentifier currentPreLiveRace = cf.getAutoPlayCtx().getPreLiveRace();
        final RegattaAndRaceIdentifier currentLiveRace = cf.getAutoPlayCtx().getLiveRace();

        this.leaderBoardName = cf.getAutoPlayCtx().getContextDefinition().getLeaderboardName();
        AutoplayHelper.getLiveRace(cf.getSailingService(), cf.getErrorReporter(), cf.getAutoPlayCtx().getEvent(),
                leaderBoardName, cf.getDispatch(), new AsyncCallback<Pair<Long, RegattaAndRaceIdentifier>>() {
                    @Override
                    public void onSuccess(Pair<Long, RegattaAndRaceIdentifier> result) {
                        errorCount = 0;
                        // we have no race, or we have one, and had a different one in the past
                        if (result == null || (currentLiveRace != null && !result.getB().equals(currentLiveRace))) {
                            boolean comingFromLiveRace = currentLiveRace != null || currentPreLiveRace != null
                                    || (result != null && !result.getB().equals(currentLiveRace));
                            setCurrentState(null, null,
                                    comingFromLiveRace ? RootNodeState.AFTER_LIVE : RootNodeState.IDLE,
                                    currentState);

                        } else {
                            final Long timeToRaceStartInMs = result.getA();
                            final RegattaAndRaceIdentifier loadedLiveRace = result.getB();
                            if (loadedLiveRace == null || timeToRaceStartInMs > PRE_RACE_DELAY) {
                                boolean comingFromLiveRace = currentLiveRace != null || currentPreLiveRace != null;
                                if (loadedLiveRace == null) {
                                    log("No live race, isComingFromLiveRace: " + comingFromLiveRace);
                                } else {
                                    log("Live race is too far away, isComingFromLiveRace: " + comingFromLiveRace);
                                }
                                setCurrentState(null, null,
                                        comingFromLiveRace ? RootNodeState.AFTER_LIVE : RootNodeState.IDLE,
                                        currentState);
                            } else if (/* is pre liverace */ timeToRaceStartInMs < PRE_RACE_DELAY
                                    && timeToRaceStartInMs > LIVE_SWITCH_DELAY) {
                                if (/* is new pre live race */!loadedLiveRace.equals(currentPreLiveRace)) {
                                    log("New pre live race: " + loadedLiveRace.getRaceName());
                                    boolean veto = setCurrentState(loadedLiveRace, null, RootNodeState.PRE_RACE,
                                            currentState);
                                    if (!veto) {
                                        log("Switched to pre live race: " + currentPreLiveRace.getRaceName());
                                    } else {
                                        log("Veto, not switching to pre live race: "
                                                + currentPreLiveRace.getRaceName());
                                    }
                                }
                            } else /* is live race */ {
                                if (/* is new live race */!loadedLiveRace.equals(currentLiveRace)) {
                                    boolean veto = setCurrentState(null, loadedLiveRace, RootNodeState.LIVE,
                                            currentState);
                                    if (!veto) {
                                        log("New live race: " + loadedLiveRace.getRaceName());
                                    }
                                }
                            }
                        }
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

    private final boolean setCurrentState(RegattaAndRaceIdentifier candidatePreLiveRace,
            RegattaAndRaceIdentifier candidateLiveRace, RootNodeState goingTo, RootNodeState comingFrom) {
        if (goingTo != comingFrom) {
            log("RootNodeBase transition " + comingFrom + " -> " + goingTo);
            boolean veto = processStateTransition(candidatePreLiveRace, candidateLiveRace, goingTo, comingFrom);
            if (veto) {
                log("Vetoed switching to state " + goingTo + " coming from " + comingFrom);
            } else {
                // only update the state if it was not vetoed, to ensure that a futurs change comingFrom is clean
                log("Switching to state " + goingTo + " coming from " + comingFrom);
                this.currentState = goingTo;
            }
            return veto;
        } else {
            log("Transition to same autoplay state, skipping");
            return true;
        }
    }

    public AutoPlayClientFactory getClientFactory() {
        return cf;
    }

    protected void backToConfig() {
        cf.getPlaceController().goTo(cf.getDefaultPlace());
    }

    protected abstract boolean processStateTransition(RegattaAndRaceIdentifier currentLiveRace,
            RegattaAndRaceIdentifier currentPreLiveRace, RootNodeState goingTo, RootNodeState comingFrom);

    protected abstract void processFailure(FailureEvent event);
}

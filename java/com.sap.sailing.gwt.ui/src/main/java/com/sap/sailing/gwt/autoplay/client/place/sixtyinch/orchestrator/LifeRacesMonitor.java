package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayNodeTransitionRequestEvent;
import com.sap.sailing.gwt.autoplay.client.events.AutoplayFailureEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.HelperSixty;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.IdleUpNextNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.LifeRaceWithRacemapNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.PreRaceWithRacemapNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.RaceEndWithBoatsNode;
import com.sap.sse.common.Util.Pair;

public class LifeRacesMonitor {
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
    private IdleUpNextNode idleLoopStartNode;
    private PreRaceWithRacemapNode preRaceStartNode;
    private LifeRaceWithRacemapNode lifeRaceStartNode;
    private RaceEndWithBoatsNode afterRaceStartNode;

    public LifeRacesMonitor(AutoPlayClientFactorySixtyInch cf) {
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
                                    comingFromLiferace ? afterRaceStartNode : idleLoopStartNode));
                        } else if (/* is pre liferace */ timeToRaceStartInMs > 10000) {
                            if (/* is new pre life race */!loadedLifeRace.equals(currentPreLifeRace)) {
                                currentPreLifeRace = loadedLifeRace;
                                currentLifeRace = null;
                                GWT.log("UpcomingLiferaceDetectedEvent: " + loadedLifeRace.toString());
                                cf.getEventBus().fireEvent(new AutoPlayNodeTransitionRequestEvent(afterRaceStartNode));
                            }
                        } else /* is life race */ {
                            currentPreLifeRace = null;
                            if (/* is new life race */!loadedLifeRace.equals(currentLifeRace)) {
                                currentLifeRace = loadedLifeRace;
                                cf.getEventBus().fireEvent(new AutoPlayNodeTransitionRequestEvent(lifeRaceStartNode));

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

    public void setIdleStartNodeRef(IdleUpNextNode idleLoopStartNode) {
        this.idleLoopStartNode = idleLoopStartNode;
    }

    public void setPreLifeRaceNodeRef(PreRaceWithRacemapNode preRaceStartNode) {
        this.preRaceStartNode = preRaceStartNode;
    }

    public void setLifeRaceNodeRef(LifeRaceWithRacemapNode lifeRaceStartNode) {
        this.lifeRaceStartNode = lifeRaceStartNode;
    }

    public void setAfterRaceNodeRef(RaceEndWithBoatsNode afterRaceStartNode) {
        this.afterRaceStartNode = afterRaceStartNode;
    }

}

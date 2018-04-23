package com.sap.sailing.gwt.autoplay.client.nodes;

import com.google.gwt.user.client.Command;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.events.FailureEvent;
import com.sap.sailing.gwt.autoplay.client.nodes.base.AutoPlayLoopNode;
import com.sap.sailing.gwt.autoplay.client.nodes.base.AutoPlaySequenceNode;
import com.sap.sailing.gwt.autoplay.client.nodes.base.RootNodeBase;
import com.sap.sailing.gwt.autoplay.client.nodes.base.RootNodeState;

public class RootNodeSixtyInch extends RootNodeBase {

    private final AutoPlayLoopNode idleLoop;
    private final AutoPlayLoopNode preLiveRaceLoop;
    private final AutoPlayLoopNode liveRaceLoop;
    private final AutoPlaySequenceNode afterLiveRaceLoop;
    protected boolean afterRaceFinished = true;
    private AutoPlayLoopNode preEvent;

    public RootNodeSixtyInch(AutoPlayClientFactory cf) {
        super(RaceEndWithCompetitorsFlagsNode.class.getName(), cf);
        this.idleLoop = new AutoPlayLoopNode("IdleLoop", 30, new IdleUpNextNode(cf), new IdleOverallLeaderBoardNode(cf),
                new VideoNode(cf));
        this.preEvent = new AutoPlayLoopNode("PreEvent", 30, new IdlePreEventNode(cf), new VideoNode(cf));
        this.preLiveRaceLoop = new AutoPlayLoopNode("PreLiveRaceLoop", 90,
                new PreLiveRaceLeaderBoardWithCompetitorsNode(cf), new PreLiveRaceWithRacemapNode(cf));
        this.liveRaceLoop = new AutoPlayLoopNode("LiveRaceLoop", 30, new LiveRaceWithRacemapNode(cf));
        this.afterLiveRaceLoop = new AutoPlaySequenceNode("AfterLiveRaceLoop", 30,
                new RaceEndWithCompetitorsBoatsNode(cf), new RaceEndWithCompetitorsFlagsNode(cf));

        afterLiveRaceLoop.setOnSequenceEnd(new Command() {
            @Override
            public void execute() {
                afterRaceFinished = true;
            }
        });
    }

    protected boolean processStateTransition(RegattaAndRaceIdentifier currentPreLiveRace,
            RegattaAndRaceIdentifier currentLiveRace, RootNodeState goingTo, RootNodeState comingFrom) {
        // block transitions, until the afterLiveRaceLoop is finished
        if (comingFrom == RootNodeState.AFTER_LIVE) {
            if (afterRaceFinished) {
                afterRaceFinished = false;
            } else {
                return true;
            }
        }
        getClientFactory().getAutoPlayCtx().updateLiveRace(currentPreLiveRace, currentLiveRace);
        switch (goingTo) {
        case PRE_EVENT:
            transitionTo(preEvent);
            break;
        case IDLE:
            transitionTo(idleLoop);
            break;
        case PRE_RACE:
            transitionTo(preLiveRaceLoop);
            break;
        case LIVE:
            transitionTo(liveRaceLoop);
            break;
        case AFTER_LIVE:
            transitionTo(afterLiveRaceLoop);
            break;
        }
        return false;
    }

    protected void processFailure(FailureEvent event) {
        AutoPlayClientFactory cf = getClientFactory();
        if (cf.getAutoPlayCtx() == null || //
                cf.getAutoPlayCtx().getContextDefinition() == null || //
                cf.getAutoPlayCtx().getEvent() == null //
        ) {
            backToConfig();
            return;
        }
        if (event.getCaught() != null) {
            event.getCaught().printStackTrace();
        }
        transitionTo(idleLoop);
    }

    @Override
    protected long getSwitchBeforeRaceStartInMillis() {
        return 3*60*1000;
    }

    @Override
    protected long getWaitTimeAfterRaceEndInMillis() {
        return 60*1000;
    }
}

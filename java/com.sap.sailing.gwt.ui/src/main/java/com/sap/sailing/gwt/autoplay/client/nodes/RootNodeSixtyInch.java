package com.sap.sailing.gwt.autoplay.client.nodes;

import com.google.gwt.user.client.Command;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.events.FailureEvent;
import com.sap.sailing.gwt.autoplay.client.nodes.base.AutoPlayLoopNode;
import com.sap.sailing.gwt.autoplay.client.nodes.base.RootNodeBase;
import com.sap.sailing.gwt.autoplay.client.nodes.base.RootNodeState;

public class RootNodeSixtyInch extends RootNodeBase {

    private final AutoPlayLoopNode idleLoop;
    private final AutoPlayLoopNode preLiveRaceLoop;
    private final AutoPlayLoopNode liveRaceLoop;
    private final AutoPlayLoopNode afterLiveRaceLoop;

    public RootNodeSixtyInch(AutoPlayClientFactory cf) {
        super(cf);

        this.idleLoop = new AutoPlayLoopNode(30, new IdleUpNextNode(cf),new IdleOverallLeaderBoardNode(cf));
        this.preLiveRaceLoop = new AutoPlayLoopNode(30, new PreRaceWithRacemapNode(cf));
        this.liveRaceLoop = new AutoPlayLoopNode(30, new LiveRaceWithRacemapNode(cf));
        this.afterLiveRaceLoop = new AutoPlayLoopNode(30, new RaceEndWithCompetitorsNode(cf), idleLoop);

        afterLiveRaceLoop.setOnLoopEnd(new Command() {
            @Override
            public void execute() {
                transitionTo(idleLoop);
            }
        });
    }

    protected boolean processStateTransition(RootNodeState goingTo, RootNodeState comingFrom) {
        // block transitions, until the afterLiveRaceLoop returns to the idleLoop, to ensure scoreboard is shown at
        // least 30 seconds
        if (comingFrom == RootNodeState.AFTER_LIVE && afterLiveRaceLoop.getCurrentNode() != idleLoop) {
            return true;
        }
        switch (goingTo) {
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
}

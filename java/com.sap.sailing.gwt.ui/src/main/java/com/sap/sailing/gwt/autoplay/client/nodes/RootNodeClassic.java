package com.sap.sailing.gwt.autoplay.client.nodes;

import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.events.FailureEvent;
import com.sap.sailing.gwt.autoplay.client.nodes.base.AutoPlayNode;
import com.sap.sailing.gwt.autoplay.client.nodes.base.RootNodeBase;
import com.sap.sailing.gwt.autoplay.client.nodes.base.RootNodeState;

public class RootNodeClassic extends RootNodeBase {
    private final AutoPlayNode idle;
    private final AutoPlayNode live;

    public RootNodeClassic(AutoPlayClientFactory cf) {
        super(cf);
        this.idle = new IdleRaceLeaderboard(cf);
        this.live = new LiveRaceBoardNode(cf);
    }

    protected boolean processStateTransition(RootNodeState goingTo, RootNodeState comingFrom) {
        switch (goingTo) {
        case IDLE:
        case AFTER_LIVE:
            transitionTo(idle);
            break;
        case PRE_RACE:
        case LIVE:
            transitionTo(live);
            break;
        }
        return false;
    }

    protected void processFailure(FailureEvent event) {
        AutoPlayClientFactory cf = getClientFactory();
        if (cf.getSlideCtx() == null || //
                cf.getSlideCtx().getSettings() == null || //
                cf.getSlideCtx().getEvent() == null //
        ) {
            backToConfig();
            return;
        }
        if (event.getCaught() != null) {
            event.getCaught().printStackTrace();
        }
        transitionTo(idle);
    }


}

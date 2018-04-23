package com.sap.sailing.gwt.autoplay.client.nodes;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.app.AutoplayPerspectiveOwnSettings;
import com.sap.sailing.gwt.autoplay.client.events.FailureEvent;
import com.sap.sailing.gwt.autoplay.client.nodes.base.AutoPlayNode;
import com.sap.sailing.gwt.autoplay.client.nodes.base.RootNodeBase;
import com.sap.sailing.gwt.autoplay.client.nodes.base.RootNodeState;

public class RootNodeClassic extends RootNodeBase {
    private final AutoPlayNode idle;
    private final AutoPlayNode live;
    private final IdlePreEventNode preEvent;

    public RootNodeClassic(AutoPlayClientFactory cf) {
        super(RaceEndWithCompetitorsFlagsNode.class.getName(), cf);
        this.idle = new IdleRaceLeaderboard(cf);
        this.live = new LiveRaceBoardNode(cf);
        this.preEvent = new IdlePreEventNode(cf);
    }

    protected boolean processStateTransition(RegattaAndRaceIdentifier currentPreLiveRace,
            RegattaAndRaceIdentifier currentLiveRace, RootNodeState goingTo, RootNodeState comingFrom) {
        getClientFactory().getAutoPlayCtx().updateLiveRace(currentPreLiveRace, currentLiveRace);
        switch (goingTo) {
        case PRE_EVENT:
            transitionTo(preEvent);
            break;
        case AFTER_LIVE:
        case IDLE:
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
        transitionTo(idle);
    }

    @Override
    protected long getSwitchBeforeRaceStartInMillis() {
        AutoplayPerspectiveOwnSettings ownSettings = getClientFactory().getAutoPlayCtx().getAutoplaySettings().getPerspectiveOwnSettings();
        return ownSettings.getTimeToSwitchBeforeRaceStart() * 1000;
    }

    @Override
    protected long getWaitTimeAfterRaceEndInMillis() {
        AutoplayPerspectiveOwnSettings ownSettings = getClientFactory().getAutoPlayCtx().getAutoplaySettings().getPerspectiveOwnSettings();
        return ownSettings.getWaitTimeAfterRaceEndInMillis() * 1000;
    }

}

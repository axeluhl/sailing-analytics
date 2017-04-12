package com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.impl;

import com.sap.sailing.gwt.autoplay.client.events.AutoPlayNodeTransitionRequestEvent;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.AutoPlayNode;

public abstract class AutoPlaySingleNextSlideNodeBase<NODE extends AutoPlayNode> extends AutoPlayNodeBase<NODE> {

    private AutoPlayNode nodeTroTransitionTo;

    public void setNextNode(AutoPlayNode nodeTroTransitionTo) {
        this.nodeTroTransitionTo = nodeTroTransitionTo;
    }

    protected AutoPlayNode getNodeTroTransitionTo() {
        return nodeTroTransitionTo;
    }

    protected void fireTransition() {
        getBus().fireEvent(new AutoPlayNodeTransitionRequestEvent(nodeTroTransitionTo));
    }

}
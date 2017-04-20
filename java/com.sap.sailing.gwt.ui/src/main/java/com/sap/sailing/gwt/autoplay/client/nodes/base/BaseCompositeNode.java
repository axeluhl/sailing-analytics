package com.sap.sailing.gwt.autoplay.client.nodes.base;

public abstract class BaseCompositeNode
        extends AutoPlayNodeBase {

    private AutoPlayNode currentNode;

    public AutoPlayNode getCurrentNode() {
        return currentNode;
    }

    protected void transitionTo(AutoPlayNode nextNode) {
        if (isStopped()) {
            return;
        }
        if (currentNode != null) {
            currentNode.stop();
        }
        currentNode = nextNode;
        nextNode.start(getBus());
    }


}
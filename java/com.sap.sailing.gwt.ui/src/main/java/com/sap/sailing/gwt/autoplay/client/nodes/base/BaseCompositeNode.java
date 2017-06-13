package com.sap.sailing.gwt.autoplay.client.nodes.base;

public abstract class BaseCompositeNode
        extends AutoPlayNodeBase {

    public BaseCompositeNode(String name) {
        super(name);
    }

    private AutoPlayNode currentNode;


    public AutoPlayNode getCurrentNode() {
        return currentNode;
    }

    protected void transitionTo(AutoPlayNode nextNode) {
        if (nextNode == currentNode) {
            log("Same node, not transitioning to " + nextNode);
            return;
        }
        if (isStopped()) {
            log("This node is stopped, not transitioning ");
            return;
        }
        if (currentNode != null) {
            log("Stopping current node " + currentNode);
            currentNode.stop();
        }
        log("Transitioning to " + nextNode);
        currentNode = nextNode;
        nextNode.start(getBus());
    }

    @Override
    public void onStop() {
        super.onStop();
        currentNode = null;
    }


}
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
            return;
        }
        if (isStopped()) {
            return;
        }
        if (currentNode != null) {
            currentNode.stop();
        }
        currentNode = nextNode;
        nextNode.start(getBus());
    }

    @Override
    public void onStop() {
        super.onStop();
        currentNode = null;
    }


}
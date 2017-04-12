package com.sap.sailing.gwt.autoplay.client.orchestrator.nodes;

public abstract class AutoPlaySingleNextSlideNodeBase extends AutoPlayNodeBase {

    private AutoPlayNode nextSlide;

    public void setNextNode(AutoPlayNode nextSlide) {
        this.nextSlide = nextSlide;
    }

    protected AutoPlayNode getNextSlide() {
        return nextSlide;
    }

    protected void fireTransition() {
        getOrchestrator().transitionToNode(nextSlide);
    }

}
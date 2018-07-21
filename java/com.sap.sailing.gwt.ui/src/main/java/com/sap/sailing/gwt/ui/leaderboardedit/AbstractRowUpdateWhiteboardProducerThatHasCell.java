package com.sap.sailing.gwt.ui.leaderboardedit;



public abstract class AbstractRowUpdateWhiteboardProducerThatHasCell<T, C> extends AbstractRowUpdateWhiteboardProducer<T>
        implements RowUpdateWhiteboardProducerThatAlsoHasCell<T, C> {
    private T currentlyRendering;

    @Override
    public void setCurrentlyRendering(T currentlyRendering) {
        this.currentlyRendering = currentlyRendering;
    }
    
    protected T getCurrentlyRendering() {
        return currentlyRendering;
    }
}

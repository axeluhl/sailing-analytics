package com.sap.sailing.gwt.ui.client;

public class AbstractRowUpdateWhiteboardProducer<T> implements RowUpdateWhiteboardProducer<T> {

    private RowUpdateWhiteboardOwner<T> rowUpdateWhiteboardOwner;

    @Override
    public void setWhiteboardOwner(RowUpdateWhiteboardOwner<T> toReceiveWhiteboardsProduced) {
        rowUpdateWhiteboardOwner = toReceiveWhiteboardsProduced;
    }
    
    protected RowUpdateWhiteboardOwner<T> getWhiteboardOwner() {
        return rowUpdateWhiteboardOwner;
    }

}

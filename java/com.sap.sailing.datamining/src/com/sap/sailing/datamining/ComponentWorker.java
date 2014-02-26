package com.sap.sailing.datamining;


public interface ComponentWorker<ResultType> extends Runnable {

    public void setReceiver(WorkReceiver<ResultType> receiver);

    public boolean isDone();

}

package com.sap.sse.datamining.workers;

public interface ComponentWorker<ResultType> extends Runnable {

    public void setReceiver(WorkReceiver<ResultType> receiver);

    public boolean isDone();

}

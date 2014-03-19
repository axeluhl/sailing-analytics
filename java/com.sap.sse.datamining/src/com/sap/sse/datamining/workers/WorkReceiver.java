package com.sap.sse.datamining.workers;

public interface WorkReceiver<ResultType> {
    
    public void receiveWork(ResultType result);

}

package com.sap.sailing.datamining;

public interface WorkReceiver<ResultType> {
    
    public void receiveWork(ResultType result);

}

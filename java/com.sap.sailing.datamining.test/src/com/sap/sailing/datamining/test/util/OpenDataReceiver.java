package com.sap.sailing.datamining.test.util;

import com.sap.sailing.datamining.WorkReceiver;

public class OpenDataReceiver<ResultType> implements WorkReceiver<ResultType> {
    
    public ResultType result;

    @Override
    public void receiveWork(ResultType result) {
        this.result = result;
    }
    
}
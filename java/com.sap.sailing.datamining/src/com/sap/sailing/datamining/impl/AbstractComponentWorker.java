package com.sap.sailing.datamining.impl;

import com.sap.sse.datamining.workers.ComponentWorker;
import com.sap.sse.datamining.workers.WorkReceiver;

public abstract class AbstractComponentWorker<ResultType> implements ComponentWorker<ResultType> {

    private WorkReceiver<ResultType> receiver;
    private boolean done;

    @Override
    public void run() {
        receiver.receiveWork(doWork());
        done = true;
    }

    protected abstract ResultType doWork();

    @Override
    public void setReceiver(WorkReceiver<ResultType> receiver) {
        this.receiver = receiver;
    }

    @Override
    public boolean isDone() {
        return done;
    }

    protected WorkReceiver<ResultType> getReceiver() {
        return receiver;
    }

}

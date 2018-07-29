package com.sap.sse.datamining.test.util.components;

import com.sap.sse.datamining.components.ProcessorInstructionHandler;
import com.sap.sse.datamining.impl.components.AbstractProcessorInstruction;

public class StatefulBlockingInstruction extends AbstractProcessorInstruction<Object> {
    
    private final int duration;
    
    private boolean runWasCalled;
    private boolean computeResultWasCalled;
    private boolean computeResultWasFinished;

    public StatefulBlockingInstruction(ProcessorInstructionHandler<Object> handler, int duration) {
        super(handler);
        this.duration = duration;
    }
    
    @Override
    public void run() {
        runWasCalled = true;
        super.run();
    }

    @Override
    protected Object computeResult() throws Exception {
        computeResultWasCalled = true;
        if (duration > 0) {
            Thread.sleep(duration);
        }
        computeResultWasFinished = true;
        return null;
    }

    public boolean runWasCalled() {
        return runWasCalled;
    }

    public boolean computeResultWasCalled() {
        return computeResultWasCalled;
    }

    public boolean computeResultWasFinished() {
        return computeResultWasFinished;
    }
    
}
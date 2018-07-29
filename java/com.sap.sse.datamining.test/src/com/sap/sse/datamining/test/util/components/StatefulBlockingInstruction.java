package com.sap.sse.datamining.test.util.components;

import com.sap.sse.datamining.components.ProcessorInstructionHandler;
import com.sap.sse.datamining.impl.components.AbstractProcessorInstruction;
import com.sap.sse.datamining.impl.components.ProcessorInstructionPriority;

public class StatefulBlockingInstruction<ResultType> extends AbstractProcessorInstruction<ResultType> {
    
    protected final long blockDuration;
    protected final ResultType result;
    
    private boolean runWasCalled;
    private boolean computeResultWasCalled;
    private boolean computeResultWasFinished;

    public StatefulBlockingInstruction(ProcessorInstructionHandler<ResultType> handler, long blockDuration) {
        this(handler, 0, blockDuration, null);
    }
    
    public StatefulBlockingInstruction(ProcessorInstructionHandler<ResultType> handler, ProcessorInstructionPriority priority, long blockDuration, ResultType result) {
        this(handler, priority.asIntValue(), blockDuration, result);
    }
    
    public StatefulBlockingInstruction(ProcessorInstructionHandler<ResultType> handler, int priority, long blockDuration, ResultType result) {
        super(handler, priority);
        this.blockDuration = blockDuration;
        this.result = result;
    }
    
    @Override
    public void run() {
        runWasCalled = true;
        super.run();
    }

    @Override
    protected ResultType computeResult() throws Exception {
        computeResultWasCalled = true;
        actionBeforeBlock();
        if (blockDuration > 0) {
            Thread.sleep(blockDuration);
        }
        actionAfterBlock();
        computeResultWasFinished = true;
        return result;
    }
    
    protected void actionBeforeBlock() { }
    protected void actionAfterBlock() { }
    
    public long getBlockDuration() {
        return blockDuration;
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
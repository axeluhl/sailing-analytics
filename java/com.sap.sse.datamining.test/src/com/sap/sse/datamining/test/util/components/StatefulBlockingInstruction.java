package com.sap.sse.datamining.test.util.components;

import com.sap.sse.datamining.components.ProcessorInstructionHandler;
import com.sap.sse.datamining.impl.components.ProcessorInstructionPriority;

public class StatefulBlockingInstruction<ResultType> extends StatefulProcessorInstruction<ResultType> {
    
    protected final long blockDuration;
    protected final ResultType result;

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
    protected ResultType internalComputeResult() throws Exception {
        actionBeforeBlock();
        if (blockDuration > 0) {
            Thread.sleep(blockDuration);
        }
        actionAfterBlock();
        return result;
    }
    
    protected void actionBeforeBlock() { }
    protected void actionAfterBlock() { }
    
    public long getBlockDuration() {
        return blockDuration;
    }
    
}
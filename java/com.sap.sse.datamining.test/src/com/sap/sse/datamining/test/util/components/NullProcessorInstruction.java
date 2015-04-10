package com.sap.sse.datamining.test.util.components;

import com.sap.sse.datamining.impl.components.AbstractPartitioningParallelProcessor;
import com.sap.sse.datamining.impl.components.AbstractProcessorInstruction;
import com.sap.sse.datamining.impl.components.ProcessorInstructionPriority;

public class NullProcessorInstruction<ResultType> extends AbstractProcessorInstruction<ResultType> {

    public NullProcessorInstruction(AbstractPartitioningParallelProcessor<?, ?, ResultType> processor,
            ProcessorInstructionPriority priority) {
        super(processor, priority);
    }

    @Override
    protected ResultType computeResult() throws Exception {
        return null;
    }
    
    public int getPriority() {
        return super.getPriority();
    }

}

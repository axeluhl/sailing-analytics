package com.sap.sse.datamining.test.util.components;

import com.sap.sse.datamining.impl.components.AbstractParallelProcessor;
import com.sap.sse.datamining.impl.components.AbstractProcessorInstruction;
import com.sap.sse.datamining.impl.components.ProcessorInstructionPriority;

public class NullProcessorInstruction<ResultType> extends AbstractProcessorInstruction<ResultType> {

    public NullProcessorInstruction(AbstractParallelProcessor<?, ResultType> processor,
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

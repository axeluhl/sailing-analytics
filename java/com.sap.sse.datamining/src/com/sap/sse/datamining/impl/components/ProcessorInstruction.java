package com.sap.sse.datamining.impl.components;

public abstract class ProcessorInstruction<ResultType> implements Runnable {
    
    private final AbstractPartitioningParallelProcessor<?, ?, ResultType> processor;

    public ProcessorInstruction(AbstractPartitioningParallelProcessor<?, ?, ResultType> processor) {
        this.processor = processor;
    }

    @Override
    public void run() {
        try {
            ResultType result = computeResult();
            if (processor.isResultValid(result) && !processor.isAborted()) {
                processor.forwardResultToReceivers(result);
            }
        } catch (Exception e) {
            if (!processor.isAborted() || !(e instanceof InterruptedException)) {
                processor.onFailure(e);
            }
        } finally {
            processor.getUnfinishedInstructionsCounter().getAndDecrement();
        }
    }

    protected abstract ResultType computeResult() throws Exception;

}

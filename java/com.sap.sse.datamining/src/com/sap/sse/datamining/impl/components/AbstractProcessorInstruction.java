package com.sap.sse.datamining.impl.components;

public abstract class AbstractProcessorInstruction<ResultType> implements Runnable, Comparable<AbstractProcessorInstruction<?>> {
    
    private final AbstractPartitioningParallelProcessor<?, ?, ResultType> processor;
    private final int priority;

    /**
     * Creates an instruction with a priority of 0, which is the highest priority.<br />
     * Use only, if <b>no processor</b> in the chain creates instruction <b>with</b> priorities other than 0 or
     * if the chain doesn't use priorities at all.
     * If this isn't the case use
     * {@link #ProcessorInstruction(AbstractPartitioningParallelProcessor, ProcessorInstructionPriority)}
     * instead.
     * 
     * @param processor The processor, that processes this instruction
     */
    public AbstractProcessorInstruction(AbstractPartitioningParallelProcessor<?, ?, ResultType> processor) {
        this(processor, 0);
    }
    
    /**
     * Creates an instruction with the given {@linkplain ProcessorInstructionPriority priority}.
     * 
     * @param processor The processor, that processes this instruction
     * @param priority The priority of this instruction
     */
    public AbstractProcessorInstruction(AbstractPartitioningParallelProcessor<?, ?, ResultType> processor,
                                ProcessorInstructionPriority priority) {
        this(processor, priority.asIntValue());
    }

    /**
     * Creates an instruction with the given priority as <code>int</code>.<br />
     * <b>Use only, if you need instructions with custom priorities and if you know what you're doing!</b>
     * If this isn't the case use
     * {@link #ProcessorInstruction(AbstractPartitioningParallelProcessor, ProcessorInstructionPriority)}
     * instead.
     * 
     * @param processor The processor, that processes this instruction
     * @param priority The priority of this instruction. <code>0</code> is the highest priority.
     */
    public AbstractProcessorInstruction(AbstractPartitioningParallelProcessor<?, ?, ResultType> processor,
                                int priority) {
        this.processor = processor;
        this.priority = priority;
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
    
    @Override
    public int compareTo(AbstractProcessorInstruction<?> instruction) {
        return Integer.compare(priority, instruction.priority);
    }

}

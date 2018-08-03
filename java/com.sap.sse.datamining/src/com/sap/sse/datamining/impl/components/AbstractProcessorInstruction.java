package com.sap.sse.datamining.impl.components;

import com.sap.sse.datamining.components.ProcessorInstruction;
import com.sap.sse.datamining.components.ProcessorInstructionHandler;

public abstract class AbstractProcessorInstruction<ResultType> implements ProcessorInstruction<ResultType> {
    
    private final ProcessorInstructionHandler<ResultType> handler;
    private final int priority;

    /**
     * Creates an instruction with a priority of 0, which is the highest priority.<br />
     * Use only, if <b>no processor</b> in the chain creates instructions <b>with</b> priorities other than 0 or
     * if the chain doesn't use priorities at all.
     * If this isn't the case use
     * {@link #ProcessorInstruction(AbstractParallelProcessor, ProcessorInstructionPriority)} instead.
     * 
     * @param handler The handler, that handles the result of the instruction.
     *                In most cases the processor, that processes the instruction.
     */
    public AbstractProcessorInstruction(ProcessorInstructionHandler<ResultType> handler) {
        this(handler, 0);
    }
    
    /**
     * Creates an instruction with the given {@linkplain ProcessorInstructionPriority priority}.
     * 
     * @param handler The processor, that processes this instruction
     * @param priority The priority of this instruction
     */
    public AbstractProcessorInstruction(ProcessorInstructionHandler<ResultType> handler,
                                ProcessorInstructionPriority priority) {
        this(handler, priority.asIntValue());
    }

    /**
     * Creates an instruction with the given priority as <code>int</code>.<br />
     * <b>Use only, if you need instructions with custom priorities and if you know what you're doing!</b>
     * If this isn't the case use
     * {@link #ProcessorInstruction(AbstractParallelProcessor, ProcessorInstructionPriority)}
     * instead.
     * 
     * @param processor The processor, that processes this instruction
     * @param priority The priority of this instruction. <code>0</code> is the highest priority.
     */
    public AbstractProcessorInstruction(ProcessorInstructionHandler<ResultType> processor,
                                int priority) {
        this.handler = processor;
        this.priority = priority;
    }

    @Override
    public void run() {
        try {
            if (!isAborted()) {
                ResultType result = computeResult();
                handler.instructionSucceeded(result);
            }
        } catch (Exception e) {
            handler.instructionFailed(e);
        } finally {
            handler.afterInstructionFinished(this);
        }
    }
    
    protected boolean isAborted() {
        return handler.isAborted();
    }

    protected abstract ResultType computeResult() throws Exception;
    
    public ProcessorInstructionHandler<ResultType> getHandler() {
        return handler;
    }
    
    @Override
    public int getPriority() {
        return priority;
    }
    
    @Override
    public int compareTo(ProcessorInstruction<?> instruction) {
        return Integer.compare(priority, instruction.getPriority());
    }

}

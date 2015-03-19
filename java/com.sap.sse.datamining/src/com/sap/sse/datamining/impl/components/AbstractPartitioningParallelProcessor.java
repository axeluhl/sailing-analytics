package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;

public abstract class AbstractPartitioningParallelProcessor<InputType, WorkingType, ResultType>
                      extends AbstractProcessor<InputType, ResultType> {

    private static final Logger LOGGER = Logger.getLogger(AbstractPartitioningParallelProcessor.class.getName());
    private static final int SLEEP_TIME_DURING_FINISHING = 100;

    private final Set<Processor<ResultType, ?>> resultReceivers;
    private final ExecutorService executor;
    private final AtomicInteger unfinishedInstructionsCounter;
    
    private boolean isFinished = false;
    private boolean isAborted = false;

    public AbstractPartitioningParallelProcessor(Class<InputType> inputType, Class<ResultType> resultType, ExecutorService executor, Collection<Processor<ResultType, ?>> resultReceivers) {
        super(inputType, resultType);
        this.executor = executor;
        this.resultReceivers = new HashSet<Processor<ResultType, ?>>(resultReceivers);
        unfinishedInstructionsCounter = new AtomicInteger();
    }

    @Override
    public void processElement(InputType element) {
        if (!isFinished && !isAborted) {
            for (WorkingType partialElement : partitionElement(element)) {
                final ProcessorInstruction<ResultType> instruction = createInstruction(partialElement);
                if (isInstructionValid(instruction)) {
                    unfinishedInstructionsCounter.getAndIncrement();
                    try {
                        executor.execute(instruction);
                    } catch (RejectedExecutionException exc){
                        LOGGER.log(Level.WARNING, "A " + RejectedExecutionException.class.getSimpleName() +
                                                  " appeared during the processing.");
                        instruction.run();
                    }
                }
            }
        }
    }

    private boolean isInstructionValid(ProcessorInstruction<ResultType> instruction) {
        return instruction != null;
    }

    boolean isResultValid(ResultType result) {
        return result != null;
    }
    
    AtomicInteger getUnfinishedInstructionsCounter() {
        return unfinishedInstructionsCounter;
    }
    
    /**
     * @return An invalid result, that won't be forwarded to the result receivers.
     */
    protected ResultType createInvalidResult() {
        return null;
    }
    
    protected void forwardResultToReceivers(ResultType result) {
        for (Processor<ResultType, ?> resultReceiver : resultReceivers) {
            resultReceiver.processElement(result);
        }
    }

    protected abstract ProcessorInstruction<ResultType> createInstruction(final WorkingType partialElement);

    protected abstract Iterable<WorkingType> partitionElement(InputType element);

    @Override
    public void onFailure(Throwable failure) {
        for (Processor<ResultType, ?> resultReceiver : resultReceivers) {
            resultReceiver.onFailure(failure);
        }
    }

    @Override
    public void finish() throws InterruptedException {
        sleepUntilAllInstructionsFinished();
        if (!isAborted) {
            isFinished = true;
            tellResultReceiversToFinish();
        }
    }

    protected void sleepUntilAllInstructionsFinished() throws InterruptedException {
        while (areUnfinishedInstructionsLeft() && !isAborted) {
            try {
                Thread.sleep(SLEEP_TIME_DURING_FINISHING);
            } catch (InterruptedException e) {
                if (!isAborted) {
                    onFailure(e);
                }
            }
        }
    }

    private boolean areUnfinishedInstructionsLeft() {
        return unfinishedInstructionsCounter.get() > 0;
    }

    protected void tellResultReceiversToFinish() {
        for (Processor<ResultType, ?> resultReceiver : resultReceivers) {
            try {
                resultReceiver.finish();
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, resultReceiver.toString() + " was interrupted", e);
            }
        }
    }

    boolean isAborted() {
        return isAborted;
    }
    
    @Override
    public void abort() {
        isAborted = true;
        tellResultReceiversToAbort();
        LOGGER.log(Level.INFO, "The processing got aborted.");
    }

    private void tellResultReceiversToAbort() {
        for (Processor<ResultType, ?> resultReceiver : resultReceivers) {
            resultReceiver.abort();
        }
    }
    
    @Override
    public AdditionalResultDataBuilder getAdditionalResultData(AdditionalResultDataBuilder additionalDataBuilder) {
        setAdditionalData(additionalDataBuilder);
        for (Processor<ResultType, ?> resultReceiver : resultReceivers) {
            additionalDataBuilder = resultReceiver.getAdditionalResultData(additionalDataBuilder);
        }
        return additionalDataBuilder;
    }
    
    protected abstract void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder);

}

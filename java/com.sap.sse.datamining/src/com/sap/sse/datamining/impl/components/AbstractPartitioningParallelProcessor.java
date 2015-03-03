package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
    private final UnfinishedInstructionsCounter unfinishedInstructionsCounter;
    
    private boolean isFinished = false;
    private boolean gotAborted = false;

    public AbstractPartitioningParallelProcessor(Class<InputType> inputType, Class<ResultType> resultType, ExecutorService executor, Collection<Processor<ResultType, ?>> resultReceivers) {
        super(inputType, resultType);
        this.executor = executor;
        this.resultReceivers = new HashSet<Processor<ResultType, ?>>(resultReceivers);
        unfinishedInstructionsCounter = new UnfinishedInstructionsCounter();
    }

    @Override
    public void processElement(InputType element) {
        if (!isFinished && !gotAborted) {
            for (WorkingType partialElement : partitionElement(element)) {
                final Callable<ResultType> instruction = createInstruction(partialElement);
                if (isInstructionValid(instruction)) {
                    Runnable instructionWrapper = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ResultType result = instruction.call();
                                if (isResultValid(result)) {
                                    forwardResultToReceivers(result);
                                }
                            } catch (Exception e) {
                                if (!gotAborted || !(e instanceof InterruptedException)) {
                                    onFailure(e);
                                }
                            } finally {
                                AbstractPartitioningParallelProcessor.this.unfinishedInstructionsCounter.decrement();
                            }
                        }
                    };
                    unfinishedInstructionsCounter.increment();
                    /**
                     * Run in executer if queue is not full. Otherwise run in current thread.
                     * 
                     * TODO: Maybe shortcut by asking executor if queue is full
                     */
                    try {
                        executor.execute(instructionWrapper);
                    } catch (RejectedExecutionException exc){
                        instructionWrapper.run();
                    }
                }
            }
        }
    }

    protected boolean isInstructionValid(Callable<ResultType> instruction) {
        return instruction != null;
    }

    protected boolean isResultValid(ResultType result) {
        return result != null;
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

    protected abstract Callable<ResultType> createInstruction(final WorkingType partialElement);

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
        if (!gotAborted) {
            isFinished = true;
            tellResultReceiversToFinish();
        }
    }

    protected void sleepUntilAllInstructionsFinished() throws InterruptedException {
        while (areUnfinishedInstructionsLeft()) {
            Thread.sleep(SLEEP_TIME_DURING_FINISHING);
        }
    }

    private boolean areUnfinishedInstructionsLeft() {
        return unfinishedInstructionsCounter.getUnfinishedInstructionsAmount() > 0;
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
    
    @Override
    public void abort() {
        gotAborted = true;
        executor.shutdownNow();
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

    /**
     * Thread safe class to manage, if there are unfinished instructions. 
     */
    private class UnfinishedInstructionsCounter {
        
        private final Lock instructionsAmountLock;
        private int unfinishedInstructionsAmount;
        
        public UnfinishedInstructionsCounter() {
            instructionsAmountLock = new ReentrantLock();
        }
        
        public void increment() {
            instructionsAmountLock.lock();
            try {
                unfinishedInstructionsAmount++;
            } finally {
                instructionsAmountLock.unlock();
            }
        }
        
        public void decrement() {
            instructionsAmountLock.lock();
            try {
                unfinishedInstructionsAmount--;
                unfinishedInstructionsAmount = Math.max(0, unfinishedInstructionsAmount);
            } finally {
                instructionsAmountLock.unlock();
            }
        }
        
        public int getUnfinishedInstructionsAmount() {
            return unfinishedInstructionsAmount;
        }
        
    }

}

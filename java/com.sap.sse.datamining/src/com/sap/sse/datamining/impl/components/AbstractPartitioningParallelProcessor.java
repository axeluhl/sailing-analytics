package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.datamining.components.Processor;

public abstract class AbstractPartitioningParallelProcessor<InputType, WorkingType, ResultType>
                      implements Processor<InputType> {

    private static final Logger LOGGER = Logger.getLogger(AbstractPartitioningParallelProcessor.class.getName());
    private static final int SLEEP_TIME_DURING_FINISHING = 100;

    private final Set<Processor<ResultType>> resultReceivers;
    private final ExecutorService executor;
    private final UnfinishedInstructionsCounter unfinishedInstructionsCounter;
    
    private boolean gotAborted = false;

    public AbstractPartitioningParallelProcessor(ExecutorService executor, Collection<Processor<ResultType>> resultReceivers) {
        this.executor = executor;
        this.resultReceivers = new HashSet<Processor<ResultType>>(resultReceivers);
        unfinishedInstructionsCounter = new UnfinishedInstructionsCounter();
    }

    @Override
    public void onElement(InputType element) {
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
                                LOGGER.log(Level.FINEST, "An error occured during the processing of an instruction: ", e);
                            }
                        } finally {
                            AbstractPartitioningParallelProcessor.this.unfinishedInstructionsCounter.decrement();
                        }
                    }
                };
                unfinishedInstructionsCounter.increment();
                executor.execute(instructionWrapper);
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
        for (Processor<ResultType> resultReceiver : resultReceivers) {
            resultReceiver.onElement(result);
        }
    }

    protected Set<Processor<ResultType>> getResultReceivers() {
        return resultReceivers;
    }

    protected abstract Callable<ResultType> createInstruction(final WorkingType partialElement);

    protected abstract Iterable<WorkingType> partitionElement(InputType element);

    @Override
    public void finish() throws InterruptedException {
        sleepUntilAllInstructionsFinished();
        tellResultReceiversToFinish();
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
        for (Processor<ResultType> resultReceiver : getResultReceivers()) {
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
        for (Processor<ResultType> resultReceiver : getResultReceivers()) {
            resultReceiver.abort();
        }
    }
    
    /**
     * Thread safe class to manage, if there are unfinished instructions. 
     */
    private class UnfinishedInstructionsCounter {
        
        private final ReentrantReadWriteLock instructionsAmountLock;
        private int unfinishedInstructionsAmount;
        
        public UnfinishedInstructionsCounter() {
            instructionsAmountLock = new ReentrantReadWriteLock();
        }
        
        public void increment() {
            instructionsAmountLock.writeLock().lock();
            try {
                unfinishedInstructionsAmount++;
            } finally {
                instructionsAmountLock.writeLock().unlock();
            }
        }
        
        public void decrement() {
            instructionsAmountLock.writeLock().lock();
            try {
                unfinishedInstructionsAmount--;
                unfinishedInstructionsAmount = Math.max(0, unfinishedInstructionsAmount);
            } finally {
                instructionsAmountLock.writeLock().unlock();
            }
        }
        
        public int getUnfinishedInstructionsAmount() {
            return unfinishedInstructionsAmount;
        }
        
    }

}

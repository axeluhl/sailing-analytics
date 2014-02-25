package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.datamining.components.Processor;

public abstract class AbstractPartitioningParallelProcessor<InputType, WorkingType, ResultType>
                      implements Processor<InputType> {

    private static final Logger LOGGER = Logger.getLogger(AbstractPartitioningParallelProcessor.class.getName());
    private static final int SLEEP_TIME_DURING_FINISHING = 100;

    private final Set<Processor<ResultType>> resultReceivers;
    private final Executor executor;
    private final UnfinishedInstructionsCounter unfinishedInstructionsCounter;

    public AbstractPartitioningParallelProcessor(Executor executor, Collection<Processor<ResultType>> resultReceivers) {
        this.executor = executor;
        this.resultReceivers = new HashSet<Processor<ResultType>>(resultReceivers);
        unfinishedInstructionsCounter = new UnfinishedInstructionsCounter();
    }

    @Override
    public void onElement(InputType element) {
        for (WorkingType partialElement : partitionElement(element)) {
            final RunnableFuture<ResultType> instruction = new FutureTask<>(createInstruction(partialElement));
            if (isInstructionValid(instruction)) {
                Runnable instructionWrapper = new Runnable() {
                    @Override
                    public void run() {
                        instruction.run();
                        try {
                            ResultType result = instruction.get();
                            if (isResultValid(result)) {
                                forwardResultToReceivers(result);
                            }
                        } catch (InterruptedException | ExecutionException e) {
                            LOGGER.log(Level.FINEST, "Error getting the result from the instruction: ", e);
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

    protected boolean isInstructionValid(Runnable instruction) {
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
    
    private void forwardResultToReceivers(ResultType result) {
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
        while (areUnfinishedInstructionsLeft()) {
            Thread.sleep(SLEEP_TIME_DURING_FINISHING);
        }
        notifyResultReceiversToFinish();
    }

    private boolean areUnfinishedInstructionsLeft() {
        return unfinishedInstructionsCounter.getUnfinishedInstructionsAmount() > 0;
    }

    private void notifyResultReceiversToFinish() {
        for (Processor<ResultType> resultReceiver : getResultReceivers()) {
            try {
                resultReceiver.finish();
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, resultReceiver.toString() + " was interrupted", e);
            }
        }
    }
    
    /**
     * Thread safe class to manage, if there are unfinished instructions. 
     */
    private class UnfinishedInstructionsCounter {
        
        private int unfinishedInstructionsAmount;
        
        public synchronized void increment() {
            unfinishedInstructionsAmount++;
        }
        
        public synchronized void decrement() {
            unfinishedInstructionsAmount--;
            unfinishedInstructionsAmount = Math.max(0, unfinishedInstructionsAmount);
        }
        
        public int getUnfinishedInstructionsAmount() {
            return unfinishedInstructionsAmount;
        }
        
    }

}

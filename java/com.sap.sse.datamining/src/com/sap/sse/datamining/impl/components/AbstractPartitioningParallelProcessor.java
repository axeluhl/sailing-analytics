package com.sap.sse.datamining.impl.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
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
            final Runnable instruction = createInstruction(partialElement);
            if (instructionIsValid(instruction)) {
                Runnable instructionWrapper = new Runnable() {
                    @Override
                    public void run() {
                        instruction.run();
                        AbstractPartitioningParallelProcessor.this.unfinishedInstructionsCounter.decrement();
                    }
                };
                unfinishedInstructionsCounter.increment();
                executor.execute(instructionWrapper);
            }
        }
    }

    protected abstract Runnable createInstruction(WorkingType partialElement);

    protected abstract Iterable<WorkingType> partitionElement(InputType element);

    private boolean instructionIsValid(Runnable instruction) {
        return instruction != null;
    }

    protected Set<Processor<ResultType>> getResultReceivers() {
        return resultReceivers;
    }

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

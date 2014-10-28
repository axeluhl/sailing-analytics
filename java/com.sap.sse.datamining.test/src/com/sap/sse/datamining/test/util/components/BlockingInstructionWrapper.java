package com.sap.sse.datamining.test.util.components;

import com.sap.sse.datamining.test.util.ConcurrencyTestsUtil;

public class BlockingInstructionWrapper implements Runnable {
    
    private Runnable innerInstruction;

    private boolean workIsInProgress;

    public BlockingInstructionWrapper(Runnable innerInstruction) {
        this.innerInstruction = innerInstruction;
        workIsInProgress = true;
    }

    @Override
    public void run() {
        while (workIsInProgress) {
            ConcurrencyTestsUtil.sleepFor(100);
        }
        innerInstruction.run();
    }
    
    /**
     * Tells the instruction, that the work is now done. It won't block it's inner instruction anymore and calls the run method of it.
     */
    public void workIsNowFinished() {
        workIsInProgress = false;
    }

}

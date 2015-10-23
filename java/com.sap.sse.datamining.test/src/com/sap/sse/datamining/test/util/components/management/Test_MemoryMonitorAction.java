package com.sap.sse.datamining.test.util.components.management;

import com.sap.sse.datamining.impl.components.management.AbstractMemoryMonitorAction;

public class Test_MemoryMonitorAction extends AbstractMemoryMonitorAction {

    private boolean actionHasBeenPerformed;

    public Test_MemoryMonitorAction(double freeMemoryInPercentThreshold) {
        super(freeMemoryInPercentThreshold);
        actionHasBeenPerformed = false;
    }
    
    @Override
    public void performAction() {
        actionHasBeenPerformed = true;
    }

    public boolean actionHasBeenPerformed() {
        return actionHasBeenPerformed;
    }
    
    public void setActionHasBeenPerformed(boolean actionHasBeenPerformed) {
        this.actionHasBeenPerformed = actionHasBeenPerformed;
    }
    
}
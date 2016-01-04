package com.sap.sailing.gwt.ui.datamining.execution;

import com.sap.sailing.gwt.ui.datamining.ManagedDataMiningQueriesCounter;

public class SimpleManagedDataMiningQueriesCounter implements ManagedDataMiningQueriesCounter {
    
    private int sentQueriesCounter;

    @Override
    public int get() {
        return sentQueriesCounter;
    }

    @Override
    public void decrease() {
        if (sentQueriesCounter > 0) {
            sentQueriesCounter--;
        }
    }
    
    @Override
    public void increase() {
        sentQueriesCounter++;
    }

}

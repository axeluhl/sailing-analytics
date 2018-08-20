package com.sap.sse.datamining.ui.client.execution;

import com.sap.sse.datamining.ui.client.ManagedDataMiningQueriesCounter;

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

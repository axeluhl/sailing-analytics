package com.sap.sse.datamining.ui.client.execution;

import com.sap.sse.datamining.ui.client.ManagedDataMiningQueriesCounter;

/**
 * Counts running queries in order to suppress results as long as other queries are running.
 * 
 * @see ManagedDataMiningQueryCallback
 */
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

package com.sap.sailing.gwt.ui.datamining.execution;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.datamining.ManagedDataMiningQueriesCounter;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.data.QueryResultState;

public abstract class ManagedDataMiningQueryCallback<AggregatedType> implements AsyncCallback<QueryResult<AggregatedType>> {

    private final ManagedDataMiningQueriesCounter counter;
    
    public ManagedDataMiningQueryCallback(ManagedDataMiningQueriesCounter counter) {
        this.counter = counter;
    }

    @Override
    public void onFailure(Throwable caught) {
        counter.decrease();
        if (counter.get() == 0) {
            handleFailure(caught);
        }
    }

    protected abstract void handleFailure(Throwable caught);

    @Override
    public void onSuccess(QueryResult<AggregatedType> result) {
        counter.decrease();
        if (result.getState() == QueryResultState.NORMAL ||
            counter.get() == 0) {
            handleSuccess(result);
        }
    }

    protected abstract void handleSuccess(QueryResult<AggregatedType> result);

}

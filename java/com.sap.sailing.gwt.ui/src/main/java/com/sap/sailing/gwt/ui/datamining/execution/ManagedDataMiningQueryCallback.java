package com.sap.sailing.gwt.ui.datamining.execution;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.datamining.ManagedDataMiningQueriesCounter;
import com.sap.sse.datamining.shared.data.QueryResultState;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;

public abstract class ManagedDataMiningQueryCallback<AggregatedType extends Serializable>
        implements AsyncCallback<QueryResultDTO<AggregatedType>> {

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
    public void onSuccess(QueryResultDTO<AggregatedType> result) {
        counter.decrease();
        if (result.getState() == QueryResultState.NORMAL ||
            counter.get() == 0) {
            handleSuccess(result);
        }
    }

    protected abstract void handleSuccess(QueryResultDTO<AggregatedType> result);

}

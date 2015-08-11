package com.sap.sailing.gwt.ui.datamining.presentation.dataproviders;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;

public abstract class AbstractResultDataProvider<ResultType> {

    private final Class<ResultType> resultType;

    public AbstractResultDataProvider(Class<ResultType> resultType) {
        this.resultType = resultType;
    }

    public Class<ResultType> getResultType() {
        return resultType;
    }
    
    /**
     * 
     * @return Collection of the keys for the provided data.
     *         The method <code>toString()</code> can be used to display the keys.
     */
    public abstract Collection<? extends Object> getDataKeys();
    
    public Map<GroupKey, Number> getData(QueryResultDTO<ResultType> result, Object dataKey) {
        Map<GroupKey, Number> data = new HashMap<>();
        Map<GroupKey, ResultType> results = result.getResults();
        for (GroupKey groupKey : results.keySet()) {
            data.put(groupKey, getData(results.get(groupKey), dataKey));
        }
        return data;
    }

    protected abstract Number getData(ResultType result, Object dataKey);
    
}

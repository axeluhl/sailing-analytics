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

    public abstract Collection<String> getDataKeys();
    
    public Map<GroupKey, Number> getData(QueryResultDTO<?> result, String dataKey) {
        if (!acceptsResultsOfType(result.getResultType())) {
            throw new IllegalArgumentException("This data provider doesn't work for results of the type '" + result.getResultType() + "'");
        }
        
        Map<GroupKey, Number> data = new HashMap<>();
        Map<GroupKey, ?> results = result.getResults();
        for (GroupKey groupKey : results.keySet()) {
            @SuppressWarnings("unchecked")
            ResultType value = (ResultType) results.get(groupKey);
            data.put(groupKey, getData(value, dataKey));
        }
        return data;
    }

    public abstract boolean acceptsResultsOfType(String type);

    protected abstract Number getData(ResultType result, String dataKey);

    public abstract String getDefaultDataKeyFor(QueryResultDTO<?> result);
    
}

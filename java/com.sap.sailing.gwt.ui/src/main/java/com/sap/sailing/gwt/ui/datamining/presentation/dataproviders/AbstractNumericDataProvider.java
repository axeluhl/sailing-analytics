package com.sap.sailing.gwt.ui.datamining.presentation.dataproviders;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;

/**
 * Maps a value of type {@code T} to a numeric value for display in a chart. The mapping
 * is parameterized by a "data key" which can, e.g., be used to select different units in
 * which to display the value.<p>
 * 
 * Subclasses should construct the mapping in their constructor and pass it on to this class's
 * constructor.
 * 
 * @author Axel Uhl (d043530)
 *
 * @param <T>
 */
public abstract class AbstractNumericDataProvider<T> {

    private final Class<T> resultType;
    private final LinkedHashMap<String, Function<T, Number>> mappings;

    protected AbstractNumericDataProvider(Class<T> resultType, LinkedHashMap<String, Function<T, Number>> mappings) {
        this.resultType = resultType;
        this.mappings = mappings;
    }

    public Class<T> getResultType() {
        return resultType;
    }

    public Collection<String> getDataKeys() {
        return mappings.keySet();
    }

    public boolean isValidDataKey(String dataKey) {
        return mappings.containsKey(dataKey);
    }
    
    public Map<GroupKey, Number> getData(QueryResultDTO<?> result, String dataKey) {
        if (!acceptsResultsOfType(result.getResultType())) {
            throw new IllegalArgumentException("This data provider doesn't work for results of the type '" + result.getResultType() + "'");
        }

        Function<T, Number> mapping = mappings.get(dataKey);
        if (mapping == null) {
            throw new IllegalArgumentException("The given data key '" + dataKey + "' isn't valid");
        }
        
        Map<GroupKey, Number> data = new HashMap<>();
        Map<GroupKey, ?> results = result.getResults();
        for (GroupKey groupKey : results.keySet()) {
            @SuppressWarnings("unchecked")
            T value = (T) results.get(groupKey);
            data.put(groupKey, mapping.apply(value));
        }
        return data;
    }

    public boolean acceptsResultsOfType(String type) {
        return getResultType().getName().equals(type);
    }

    public String getDefaultDataKeyFor(QueryResultDTO<?> result) {
        return mappings.keySet().iterator().next();
    }
    
    public abstract String getLocalizedNameForDataKey(StringMessages stringMessages, String dataKey);
    
}

package com.sap.sailing.gwt.ui.datamining.presentation.dataproviders;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util.Pair;
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
 * @author Lennart Hensler
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
    
    /**
     * Provides the actual result data to be visualized primarily. The {@code result} entries are mapped to
     * {@link Number}s, based on the {@code dataKey} which is used to select the mapping out of those provided to this
     * object's constructor.
     */
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

    /**
     * Optionally, a {@link QueryResultDTO query result} may provide {@link QueryResultDTO#getErrorMargins() error
     * margin data} which can, if available, be visualized, e.g., by error bars. This method collects this error
     * margin data and maps it through the {@link #mappings mapping} selected by the {@code dataKey}.
     * 
     * @param result
     *            the result from which to extract error margins
     * @param dataKey
     *            selects one of the mappings provided to this object's constructor
     * @return {@code null} if no error margin information is available, or pairs of lower and upper error margin
     *         bounds, keyed by the group key. The map may not be "complete," meaning that not for all keys returned in
     *         the {@link #getData(QueryResultDTO, String)} result the map returned by this method needs to have an
     *         equal key.
     */
    public Map<GroupKey, Pair<Number, Number>> getErrorData(QueryResultDTO<?> result, String dataKey) {
        if (!acceptsResultsOfType(result.getResultType())) {
            throw new IllegalArgumentException("This data provider doesn't work for results of the type '" + result.getResultType() + "'");
        }
        Function<T, Number> mapping = mappings.get(dataKey);
        if (mapping == null) {
            throw new IllegalArgumentException("The given data key '" + dataKey + "' isn't valid");
        }
        final Map<GroupKey, Pair<Number, Number>> data = new HashMap<>();
        final Map<GroupKey, ?> errorMargins = result.getErrorMargins();
        for (GroupKey groupKey : errorMargins.keySet()) {
            if (errorMargins != null) {
                @SuppressWarnings("unchecked")
                Pair<T, T> errorMarginsForGroupKey = (Pair<T, T>) errorMargins.get(groupKey);
                if (errorMarginsForGroupKey != null) {
                    data.put(groupKey, new Pair<>(mapping.apply(errorMarginsForGroupKey.getA()),
                                                  mapping.apply(errorMarginsForGroupKey.getB())));
                }
            }
        }
        return data.isEmpty() ? null : data;
    }

    public boolean acceptsResultsOfType(String type) {
        return getResultType().getName().equals(type);
    }

    public String getDefaultDataKeyFor(QueryResultDTO<?> result) {
        return mappings.keySet().iterator().next();
    }
    
    public abstract String getLocalizedNameForDataKey(StringMessages stringMessages, String dataKey);
    
}

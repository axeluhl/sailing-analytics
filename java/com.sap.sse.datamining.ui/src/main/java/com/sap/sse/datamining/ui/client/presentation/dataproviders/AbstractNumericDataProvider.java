package com.sap.sse.datamining.ui.client.presentation.dataproviders;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.sap.sse.common.Util.Triple;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.datamining.ui.client.StringMessages;

/**
 * Maps a value of type {@code T} to a numeric value for display in a chart. The mapping is parameterized by a "data
 * key" which can, e.g., be used to select different units in which to display the value.
 * <p>
 * 
 * Subclasses should construct the mapping in their constructor and pass it on to this class's constructor.
 * 
 * @author Lennart Hensler
 * @author Axel Uhl (d043530)
 *
 * @param <T>
 */
public abstract class AbstractNumericDataProvider<T extends Serializable> {

    private final Class<T> resultType;

    protected AbstractNumericDataProvider(Class<T> resultType) {
        this.resultType = resultType;
    }

    public Class<T> getResultType() {
        return resultType;
    }

    public abstract Collection<String> getDataKeys(QueryResultDTO<?> result);

    public abstract boolean isValidDataKey(QueryResultDTO<?> result, String dataKey);

    public abstract String getDefaultDataKeyFor(QueryResultDTO<?> result);

    public abstract String getLocalizedNameForDataKey(QueryResultDTO<?> result, StringMessages stringMessages,
            String dataKey);

    protected abstract Function<T, Number> getMapping(QueryResultDTO<?> result, String dataKey);

    /**
     * Provides the actual result data to be visualized primarily. The {@code result} entries are mapped to
     * {@link Number}s, based on the {@code dataKey} which is used to select the mapping out of those provided to this
     * object's constructor.
     */
    public Map<GroupKey, Number> getData(QueryResultDTO<?> result, String dataKey) {
        if (!acceptsResultsOfType(result.getResultType())) {
            throw new IllegalArgumentException(
                    "This data provider doesn't work for results of the type '" + result.getResultType() + "'");
        }
        Function<T, Number> mapping = getMapping(result, dataKey);
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
     * margin data} which can, if available, be visualized, e.g., by error bars. This method collects this error margin
     * data and maps it through the {@link #mappings mapping} selected by the {@code dataKey}. The implementation in
     * this class returns {@code null}.
     * 
     * @param result
     *            the result from which to extract error margins
     * @param dataKey
     *            selects one of the mappings provided to this object's constructor
     * @return {@code null} if no error margin information is available, or triples of lower and upper error margin
     *         bounds and the element count, keyed by the group key. The map may not be "complete," meaning that not for
     *         all keys returned in the {@link #getData(QueryResultDTO, String)} result the map returned by this method
     *         needs to have an equal key.
     */
    public Map<GroupKey, Triple<Number, Number, Long>> getErrorData(QueryResultDTO<?> result, String dataKey) {
        return null;
    }

    public boolean acceptsResultsOfType(String type) {
        return getResultType().getName().equals(type);
    }

}

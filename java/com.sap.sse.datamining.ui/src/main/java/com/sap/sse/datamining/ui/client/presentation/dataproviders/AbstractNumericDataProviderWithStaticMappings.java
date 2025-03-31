package com.sap.sse.datamining.ui.client.presentation.dataproviders;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.function.Function;

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
public abstract class AbstractNumericDataProviderWithStaticMappings<T extends Serializable> extends AbstractNumericDataProvider<T> {
    private final LinkedHashMap<String, Function<T, Number>> mappings;

    protected AbstractNumericDataProviderWithStaticMappings(Class<T> resultType, LinkedHashMap<String, Function<T, Number>> mappings) {
        super(resultType);
        this.mappings = mappings;
    }

    public Collection<String> getDataKeys(QueryResultDTO<?> result) {
        return mappings.keySet();
    }

    public boolean isValidDataKey(QueryResultDTO<?> result, String dataKey) {
        return mappings.containsKey(dataKey);
    }
    
    public String getDefaultDataKeyFor(QueryResultDTO<?> result) {
        return mappings.keySet().iterator().next();
    }

    @Override
    protected Function<T, Number> getMapping(QueryResultDTO<?> result, String dataKey) {
        return mappings.get(dataKey);
    }
}

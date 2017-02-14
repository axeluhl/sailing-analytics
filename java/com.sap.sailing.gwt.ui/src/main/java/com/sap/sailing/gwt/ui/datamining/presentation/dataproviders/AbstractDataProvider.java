package com.sap.sailing.gwt.ui.datamining.presentation.dataproviders;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.function.Function;

import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;

/**
 * Maps objects of type {@code T} to numerical values, using {@link String} keys describing the
 * type of mapping. This can, e.g., be used to project a scalar value to different units, such as
 * objects of type {@link Distance} to meters or inches.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <T>
 */
public class AbstractDataProvider<T> extends AbstractResultDataProvider<T> {
    private final LinkedHashMap<String, Function<T, Number>> mappings;

    /**
     * The first key in the mappings is used as the {@link #getDefaultDataKeyFor(QueryResultDTO) default} mapping.
     */
    public AbstractDataProvider(Class<T> tClass, LinkedHashMap<String, Function<T, Number>> mappings) {
        super(tClass);
        this.mappings = mappings;
    }

    @Override
    public Collection<String> getDataKeys() {
        return mappings.keySet();
    }
    
    @Override
    public boolean acceptsResultsOfType(String type) {
        return getResultType().getName().equals(type);
    }

    @Override
    protected Number getData(T value, String dataKey) {
        Function<T, Number> f = mappings.get(dataKey);
        if (f == null) {
            throw new IllegalArgumentException("The given data key '" + dataKey + "' isn't valid");
        } else {
            return f.apply(value);
        }
    }
    
    @Override
    public String getDefaultDataKeyFor(QueryResultDTO<?> result) {
        return mappings.keySet().iterator().next();
    }

}

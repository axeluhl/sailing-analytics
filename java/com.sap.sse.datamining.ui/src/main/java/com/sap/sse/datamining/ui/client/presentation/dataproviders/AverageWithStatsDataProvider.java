package com.sap.sse.datamining.ui.client.presentation.dataproviders;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import com.sap.sse.common.Util.Triple;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.data.AverageWithStats;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.datamining.ui.client.StringMessages;

/**
 * An {@link AverageWithStats} object can be mapped to the {@link AverageWithStats#getAverage() average} value.
 * Additionally, error bars based on {@link AverageWithStats#getMin()} and {@link AverageWithStats#getMax()} can be
 * rendered, the {@link AverageWithStats#getMedian() median}, the {@link AverageWithStats#getStandardDeviation()
 * standard deviation} as well as the {@link AverageWithStats#getCount() element count} may be put into the point's tool
 * tip.
 * <p>
 * 
 * Since {@link AverageWithStats} itself is a generic type, another {@link AbstractNumericDataProvider} is needed to map
 * the values to be displayed (other than count which is already guaranteed to be of numeric type) to a numeric type.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class AverageWithStatsDataProvider
        extends AbstractNumericDataProvider<AverageWithStats<? extends Serializable>> {
    final static Class<?> _c = AverageWithStats.class;
    @SuppressWarnings("unchecked")
    final static Class<AverageWithStats<? extends Serializable>> _cc = (Class<AverageWithStats<? extends Serializable>>) _c;
    private final DataProvidersPrecedenceList innerProviders;

    public AverageWithStatsDataProvider(final DataProvidersPrecedenceList innerProviders) {
        super(_cc);
        this.innerProviders = innerProviders;
    }

    /**
     * Optionally, a {@link QueryResultDTO query result} may provide {@link QueryResultDTO#getErrorMargins() error
     * margin data} which can, if available, be visualized, e.g., by error bars. This method collects this error margin
     * data and maps it through the {@link #mappings mapping} selected by the {@code dataKey}.
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
    public Map<GroupKey, Triple<Number, Number, Long>> getErrorData(QueryResultDTO<?> result, String dataKey) {
        if (!acceptsResultsOfType(result.getResultType())) {
            throw new IllegalArgumentException(
                    "This data provider doesn't work for results of the type '" + result.getResultType() + "'");
        }
        return getErrorDataInternal(result, dataKey);
    }

    private <T> Map<GroupKey, Triple<Number, Number, Long>> getErrorDataInternal(QueryResultDTO<?> result,
            String dataKey) {
        @SuppressWarnings("unchecked")
        Function<T, Number> mapping = (Function<T, Number>) getInnerMapping(result, dataKey);
        if (mapping == null) {
            throw new IllegalArgumentException("The given data key '" + dataKey + "' isn't valid");
        }
        final Map<GroupKey, Triple<Number, Number, Long>> data = new HashMap<>();
        for (Entry<GroupKey, ?> e : result.getResults().entrySet()) {
            @SuppressWarnings("unchecked")
            AverageWithStats<T> aws = (AverageWithStats<T>) e.getValue();
            if (aws.getMin() != null && aws.getMax() != null) {
                data.put(e.getKey(),
                        new Triple<>(mapping.apply(aws.getMin()), mapping.apply(aws.getMax()), aws.getCount()));
            }
        }
        return data.isEmpty() ? null : data;
    }

    @Override
    public String getDefaultDataKeyFor(QueryResultDTO<?> result) {
        final AbstractNumericDataProvider<? extends Serializable> provider = getProviderFromGenericResult(result);
        return provider == null ? "" : provider.getDefaultDataKeyFor(result);
    }

    private AbstractNumericDataProvider<? extends Serializable> getProviderFromGenericResult(QueryResultDTO<?> result) {
        @SuppressWarnings("unchecked")
        final QueryResultDTO<AverageWithStats<?>> typedResult = (QueryResultDTO<AverageWithStats<?>>) result;
        final AbstractNumericDataProvider<? extends Serializable> provider = getProvider(typedResult);
        return provider;
    }

    @Override
    public String getLocalizedNameForDataKey(QueryResultDTO<?> result, StringMessages stringMessages, String dataKey) {
        final AbstractNumericDataProvider<? extends Serializable> provider = getProviderFromGenericResult(result);
        return provider == null ? "" : provider.getLocalizedNameForDataKey(result, stringMessages, dataKey);
    }

    /**
     * Picks the first result object of type {@link AverageWithStats} that can be found in {@code result}'s
     * {@link QueryResultDTO#getResults()} and passes it to the {@link #innerProviders}'s
     * {@link DataProvidersPrecedenceList#selectCurrentDataProvider(QueryResultDTO)} method to determine the most
     * appropriate inner provider to use for the result.
     */
    private AbstractNumericDataProvider<? extends Serializable> getProvider(
            QueryResultDTO<AverageWithStats<?>> result) {
        AbstractNumericDataProvider<? extends Serializable> provider = null;
        final Map<GroupKey, AverageWithStats<?>> results = result.getResults();
        for (final Entry<GroupKey, AverageWithStats<?>> e : results.entrySet()) {
            if (e.getValue() != null) {
                provider = innerProviders.selectCurrentDataProvider(e.getValue().getResultType());
                break;
            }
        }
        return provider;
    }

    @Override
    public Collection<String> getDataKeys(QueryResultDTO<?> result) {
        final AbstractNumericDataProvider<? extends Serializable> provider = getProviderFromGenericResult(result);
        return provider == null ? Collections.emptySet() : provider.getDataKeys(result);
    }

    @Override
    public boolean isValidDataKey(QueryResultDTO<?> result, String dataKey) {
        final AbstractNumericDataProvider<? extends Serializable> provider = getProviderFromGenericResult(result);
        return provider == null ? false : provider.isValidDataKey(result, dataKey);
    }

    /**
     * Returns a mapping function that maps from an {@link AverageWithStats} object to the
     * {@link AverageWithStats#getAverage() average} value.
     */
    @Override
    protected Function<AverageWithStats<? extends Serializable>, Number> getMapping(QueryResultDTO<?> result,
            String dataKey) {
        final AbstractNumericDataProvider<? extends Serializable> provider = getProviderFromGenericResult(result);
        return new Function<AverageWithStats<? extends Serializable>, Number>() {
            @Override
            public Number apply(AverageWithStats<? extends Serializable> t) {
                final Function<? extends Serializable, Number> mapping = provider.getMapping(result, dataKey);
                return doApply(mapping, t.getAverage());
            }

            private <U extends Serializable, V extends Serializable> Number doApply(Function<U, Number> mapping,
                    V average) {
                @SuppressWarnings("unchecked")
                final U castAverage = (U) average;
                return mapping.apply(castAverage);
            }
        };
    }

    protected Function<?, Number> getInnerMapping(QueryResultDTO<?> result, String dataKey) {
        final AbstractNumericDataProvider<? extends Serializable> provider = getProviderFromGenericResult(result);
        return provider == null ? null : provider.getMapping(result, dataKey);
    }

}

package com.sap.sse.datamining.ui.client.presentation.dataproviders;

import java.io.Serializable;

/**
 * A sequence of {@link AbstractNumericDataProvider}s which are probed in sequence
 * for applicability.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class DataProvidersPrecedenceList {
    private final Iterable<AbstractNumericDataProvider<? extends Serializable>> providers;

    public DataProvidersPrecedenceList(Iterable<AbstractNumericDataProvider<? extends Serializable>> providers) {
        super();
        this.providers = providers;
    }
    
    public AbstractNumericDataProvider<? extends Serializable> selectCurrentDataProvider(String resultType) {
        for (final AbstractNumericDataProvider<? extends Serializable> provider : providers) {
            if (provider.acceptsResultsOfType(resultType)) {
                return provider;
            }
        }
        return null;
    }
}

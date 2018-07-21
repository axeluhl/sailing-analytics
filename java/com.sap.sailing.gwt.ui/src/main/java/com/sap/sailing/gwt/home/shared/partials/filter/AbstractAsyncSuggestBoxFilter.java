package com.sap.sailing.gwt.home.shared.partials.filter;

import java.util.Collection;

import com.sap.sse.common.filter.Filter;
import com.sap.sse.gwt.client.suggestion.AbstractSuggestOracle;

public abstract class AbstractAsyncSuggestBoxFilter<T, C> extends AbstractSuggestBoxFilter<T, C> {

    protected AbstractAsyncSuggestBoxFilter(AbstractSuggestOracle<C> suggestOracle, String placeholderText) {
        super(suggestOracle, placeholderText);
    }
    
    @Override
    public final void setSelectableValues(Collection<C> selectableValues) {
        // Nothing to do here, because of external filtering
    }
    
    @Override
    protected final Filter<T> getFilter(String searchString) {
        return new Filter<T>() { // Return an always matching filter, because of external filtering
            @Override public boolean matches(T object) { return true; }
            @Override public String getName() { return "alwaysMatchingFilter"; }
        };
    }

}

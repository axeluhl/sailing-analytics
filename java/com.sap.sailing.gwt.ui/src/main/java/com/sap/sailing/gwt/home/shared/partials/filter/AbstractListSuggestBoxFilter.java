package com.sap.sailing.gwt.home.shared.partials.filter;

import java.util.Collection;

import com.sap.sse.gwt.client.suggestion.AbstractListSuggestOracle;

public abstract class AbstractListSuggestBoxFilter<T, C> extends AbstractSuggestBoxFilter<T, C> {

    protected AbstractListSuggestBoxFilter(AbstractListSuggestOracle<C> suggestOracle, String placeholderText) {
        super(suggestOracle, placeholderText);
    }
    
    @Override
    public void setSelectableValues(Collection<C> selectableValues) {
        getSuggestOracle().setSelectableValues(selectableValues);
    }
    
    @Override
    protected AbstractListSuggestOracle<C> getSuggestOracle() {
        return (AbstractListSuggestOracle<C>) super.getSuggestOracle();
    }
    
}

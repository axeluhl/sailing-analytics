package com.sap.sailing.gwt.home.shared.partials.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.ui.SuggestOracle.Callback;
import com.google.gwt.user.client.ui.SuggestOracle.Request;
import com.sap.sse.common.filter.AbstractListFilter;

public abstract class AbstractListSuggestBoxFilter<T, C> extends AbstractSuggestBoxFilter<T, C> {

    private final List<C> suggestionObjectList = new ArrayList<>();
    protected final AbstractListFilter<C> suggestionMatchingFilter = new AbstractListFilter<C>() {
        @Override
        public Iterable<String> getStrings(C value) {
            return getMatchingStrings(value);
        }
    };
    
    protected AbstractListSuggestBoxFilter(String placeholderText) {
        super(placeholderText);
    }
    
    @Override
    protected void getSuggestions(Request request, Callback callback, Iterable<String> queryTokens) {
        Iterable<C> filteredList = suggestionMatchingFilter.applyFilter(queryTokens, suggestionObjectList);
        this.setSuggestions(request, callback, filteredList, queryTokens);
    }
    
    @Override
    public void setSelectableValues(Collection<C> selectableValues) {
        suggestionObjectList.clear();
        suggestionObjectList.addAll(selectableValues);
    }
    
}

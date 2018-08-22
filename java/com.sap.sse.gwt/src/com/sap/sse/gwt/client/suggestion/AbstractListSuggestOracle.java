package com.sap.sse.gwt.client.suggestion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sap.sse.common.filter.AbstractListFilter;

public abstract class AbstractListSuggestOracle<C> extends AbstractSuggestOracle<C> {
    
    private final List<C> suggestionObjectList = new ArrayList<>();
    private final AbstractListFilter<C> suggestionMatchingFilter = new AbstractListFilter<C>() {
        @Override
        public Iterable<String> getStrings(C value) {
            return getMatchingStrings(value);
        }
    };

    @Override
    protected final void getSuggestions(Request request, Callback callback, Iterable<String> queryTokens) {
        Iterable<String> keywords = getKeywordStrings(queryTokens);
        Iterable<C> filteredList = suggestionMatchingFilter.applyFilter(keywords, suggestionObjectList);
        this.setSuggestions(request, callback, filteredList, keywords);
    }
    
    protected Iterable<String> getKeywordStrings(Iterable<String> queryTokens) {
        return queryTokens;
    }
    
    public final AbstractListFilter<C> getSuggestionMatchingFilter() {
        return suggestionMatchingFilter;
    }
    
    public void setSelectableValues(Collection<? extends C> selectableValues) {
        suggestionObjectList.clear();
        suggestionObjectList.addAll(selectableValues);
    }

    protected abstract Iterable<String> getMatchingStrings(C value);

    protected abstract String createSuggestionKeyString(C value);

    protected abstract String createSuggestionAdditionalDisplayString(C value);

}

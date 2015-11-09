package com.sap.sailing.gwt.home.shared.partials.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.sap.sse.common.filter.AbstractListFilter;

public abstract class AbstractSuggestBoxFilter<T, C> extends AbstractTextInputFilter<T, C> {

    private final ContainsSuggestOracle suggestOracle = new ContainsSuggestOracle();
    private final SuggestBox suggestBox;
    private final List<C> suggestionObjectList = new ArrayList<>();
    protected final AbstractListFilter<C> suggestionMatchingFilter = new AbstractListFilter<C>() {
        @Override
        public Iterable<String> getStrings(C value) {
            return getMatchingStrings(value);
        }
    };
    
    protected AbstractSuggestBoxFilter(String placeholderText) {
        initWidgets(suggestBox = new SuggestBox(suggestOracle), placeholderText);
        suggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
            @Override
            public void onSelection(SelectionEvent<Suggestion> event) {
                AbstractSuggestBoxFilter.super.update();
            }
        });
    }
    
    @Override
    public void setSelectableValues(Collection<C> selectableValues) {
        suggestionObjectList.clear();
        suggestionObjectList.addAll(selectableValues);
    }
    
    protected abstract String createSuggestionDisplayString(C value);
    
    protected abstract String createSuggestionReplacementString(C value);
    
    protected abstract Iterable<String> getMatchingStrings(C value);
    
    private class ContainsSuggestOracle extends SuggestOracle {
        
        @Override
        public void requestDefaultSuggestions(Request request, Callback callback) {
            setSuggestions(request, callback, suggestionObjectList);
        }
        
        @Override
        public void requestSuggestions(Request request, Callback callback) {
            Iterable<String> queryTokens = Arrays.asList(request.getQuery().split("\\s"));
            setSuggestions(request, callback, suggestionMatchingFilter.applyFilter(queryTokens, suggestionObjectList));
        }
        
        private void setSuggestions(Request request, Callback callback, Iterable<C> suggestionObjects) {
            List<Suggestion> suggestions = new ArrayList<>();
            for (C match : suggestionObjects) {
                suggestions.add(new SimpleSuggestion(match));
            }
            Response response = new Response(suggestions);
            callback.onSuggestionsReady(request, response);
        }
        
    }
    
    private class SimpleSuggestion implements Suggestion {
        
        private final C suggestObject;
        
        private SimpleSuggestion(C suggestObject) {
            this.suggestObject = suggestObject;
        }

        @Override
        public String getDisplayString() {
            return createSuggestionDisplayString(suggestObject);
        }

        @Override
        public String getReplacementString() {
            return createSuggestionReplacementString(suggestObject);
        }
    }
}

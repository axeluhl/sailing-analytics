package com.sap.sailing.gwt.home.shared.partials.filter;

import java.util.ArrayList;
import java.util.Collection;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public abstract class AbstractSuggestBoxFilter<T, C> extends AbstractTextInputFilter<T, C> {

    private final MultiWordSuggestOracle suggestOracle;
    private final SuggestBox suggestBox;
    
    protected AbstractSuggestBoxFilter(String placeholderText, String whitespaceCharacters) {
        suggestOracle = new MultiWordSuggestOracle(whitespaceCharacters);
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
        suggestOracle.clear();
        ArrayList<String> suggestionList = new ArrayList<>();
        for (C value : selectableValues) {
            String suggestionString = createSuggestionString(value);
            suggestOracle.add(suggestionString);
            suggestionList.add(suggestionString);
        }
        suggestOracle.setDefaultSuggestionsFromText(suggestionList);
    }
    
    protected abstract String createSuggestionString(C value);
    
}

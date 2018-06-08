package com.sap.sailing.gwt.home.shared.partials.filter;

import com.sap.sse.gwt.client.suggestion.AbstractSuggestOracle;
import com.sap.sse.gwt.client.suggestion.CustomSuggestBox;
import com.sap.sse.gwt.client.suggestion.CustomSuggestBox.SuggestionSelectionHandler;

public abstract class AbstractSuggestBoxFilter<T, C> extends AbstractTextInputFilter<T, C> {
    
    private final AbstractSuggestOracle<C> suggestOracle;

    protected AbstractSuggestBoxFilter(AbstractSuggestOracle<C> suggestOracle, String placeholderText) {
        final CustomSuggestBox<C> suggestBox = new CustomSuggestBox<>(this.suggestOracle = suggestOracle);
        suggestBox.addSuggestionSelectionHandler(new SuggestionSelectionHandler<C>() {
            @Override
            public void onSuggestionSelected(C suggestObject) {
                AbstractSuggestBoxFilter.this.onSuggestionSelected(suggestObject);
                AbstractSuggestBoxFilter.super.update();
            }
        });
        initWidgets(suggestBox, placeholderText);
    }
    
    protected AbstractSuggestOracle<C> getSuggestOracle() {
        return suggestOracle;
    }
    
    protected abstract void onSuggestionSelected(C selectedItem);
}

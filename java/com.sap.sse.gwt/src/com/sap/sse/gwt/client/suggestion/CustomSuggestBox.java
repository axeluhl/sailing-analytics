package com.sap.sse.gwt.client.suggestion;

import java.util.Collection;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TextBox;

public class CustomSuggestBox<T> extends SuggestBox {
    
    public CustomSuggestBox(AbstractSuggestOracle<T> suggestOracle) {
        this(suggestOracle, new CustomSuggestionDisplay());
    }
    
    public CustomSuggestBox(AbstractSuggestOracle<T> suggestOracle, SuggestionDisplay suggestionDisplay) {
        super(suggestOracle, new TextBox(), suggestionDisplay);
    }
    
    public final HandlerRegistration addSuggestionSelectionHandler(final SuggestionSelectionHandler<T> handler) {
        return this.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
            @Override @SuppressWarnings("unchecked")
            public void onSelection(SelectionEvent<Suggestion> event) {
                handler.onSuggestionSelected(
                        ((AbstractSuggestOracle<T>.SimpleSuggestion) event.getSelectedItem()).getSuggestObject());
            }
        });
    }
    
    public interface SuggestionSelectionHandler<T> {
        void onSuggestionSelected(T suggestObject);
    }

    private static class CustomSuggestionDisplay extends DefaultSuggestionDisplay {
        @Override
        protected void showSuggestions(SuggestBox suggestBox, Collection<? extends Suggestion> suggestions,
                boolean isDisplayStringHTML, boolean isAutoSelectEnabled, SuggestionCallback callback) {
            super.showSuggestions(suggestBox, suggestions, isDisplayStringHTML, isAutoSelectEnabled, callback);
            getPopupPanel().getElement().getStyle().setProperty("maxWidth", suggestBox.getOffsetWidth(), Unit.PX);
        }
    }
    
}

package com.sap.sse.datamining.ui.client.selection.statistic;

import java.util.Collection;
import java.util.Objects;

import com.google.gwt.user.client.ui.MenuItem;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.suggestion.AbstractListSuggestOracle;
import com.sap.sse.gwt.client.suggestion.CustomSuggestBox;

public class ExtractionFunctionSuggestBox extends CustomSuggestBox<ExtractionFunctionWithContext> {
    
    @FunctionalInterface
    public interface ValueChangeHandler {
        void valueChanged(ExtractionFunctionWithContext oldValue, ExtractionFunctionWithContext newValue);
    }

    private final AbstractListSuggestOracle<ExtractionFunctionWithContext> suggestOracle;
    private final ExtractionFunctionSuggestBox.ScrollableSuggestionDisplay display;
    private ExtractionFunctionSuggestBox.ValueChangeHandler valueChangeHandler;
    
    private ExtractionFunctionWithContext extractionFunction;

    @SuppressWarnings("unchecked")
    public ExtractionFunctionSuggestBox() {
        super(new AbstractListSuggestOracle<ExtractionFunctionWithContext>() {
            @Override
            protected Iterable<String> getKeywordStrings(Iterable<String> queryTokens) {
                String filterText = Util.first(queryTokens);
                if (filterText == null) {
                    return queryTokens;
                }
                return Util.splitAlongWhitespaceRespectingDoubleQuotedPhrases(filterText);
            }

            @Override
            protected Iterable<String> getMatchingStrings(ExtractionFunctionWithContext value) {
                return value.getMatchingStrings();
            }

            @Override
            protected String createSuggestionKeyString(ExtractionFunctionWithContext value) {
                return value.getDisplayString();
            }

            @Override
            protected String createSuggestionAdditionalDisplayString(ExtractionFunctionWithContext value) {
                return value.getAdditionalDisplayString();
            }
        }, new ScrollableSuggestionDisplay());
        suggestOracle = (AbstractListSuggestOracle<ExtractionFunctionWithContext>) getSuggestOracle();
        display = (ExtractionFunctionSuggestBox.ScrollableSuggestionDisplay) getSuggestionDisplay();
        addSuggestionSelectionHandler(this::setExtractionFunction);
    }
    
    public void setValueChangeHandler(ExtractionFunctionSuggestBox.ValueChangeHandler valueChangeHandler) {
        this.valueChangeHandler = valueChangeHandler;
    }

    public void setSelectableValues(Collection<? extends ExtractionFunctionWithContext> selectableValues) {
        suggestOracle.setSelectableValues(selectableValues);
    }

    public void setExtractionFunction(ExtractionFunctionWithContext extractionFunction) {
        if (!Objects.equals(this.extractionFunction, extractionFunction)) {
            ExtractionFunctionWithContext oldValue = this.extractionFunction;
            this.extractionFunction = extractionFunction;
            setValue(extractionFunction == null ? null : extractionFunction.getDisplayString(), false);
            if (valueChangeHandler != null) {
                valueChangeHandler.valueChanged(oldValue, extractionFunction);
            }
        }
        setFocus(false);
    }

    public ExtractionFunctionWithContext getExtractionFunction() {
        return extractionFunction;
    }
    
    @Override
    public void hideSuggestionList() {
        display.hideSuggestions();
    }

    private static class ScrollableSuggestionDisplay extends DefaultSuggestionDisplay {

        public ScrollableSuggestionDisplay() {
            getPopupPanel().addStyleName("statisticSuggestBoxPopup");
        }
        
        @Override
        protected void moveSelectionUp() {
            super.moveSelectionUp();
            scrollSelectedItemIntoView();
        }

        @Override
        protected void moveSelectionDown() {
            super.moveSelectionDown();
            scrollSelectedItemIntoView();
        }

        private void scrollSelectedItemIntoView() {
            getSelectedMenuItem().getElement().scrollIntoView();
        }

        private native MenuItem getSelectedMenuItem() /*-{
                        var menu = this.@com.google.gwt.user.client.ui.SuggestBox.DefaultSuggestionDisplay::suggestionMenu;
                        return menu.@com.google.gwt.user.client.ui.MenuBar::selectedItem;
        }-*/;

    }

}
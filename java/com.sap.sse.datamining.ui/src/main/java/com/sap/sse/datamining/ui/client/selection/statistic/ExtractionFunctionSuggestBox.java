package com.sap.sse.datamining.ui.client.selection.statistic;

import java.util.Collection;
import java.util.Objects;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.HasAnimation;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.AnimationType;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.suggestion.AbstractListSuggestOracle;
import com.sap.sse.gwt.client.suggestion.CustomSuggestBox;

public class ExtractionFunctionSuggestBox extends CustomSuggestBox<ExtractionFunctionWithContext> {
    
    @FunctionalInterface
    public interface ValueChangeHandler {
        void valueChanged(ExtractionFunctionWithContext oldValue, ExtractionFunctionWithContext newValue);
    }

    private final AbstractListSuggestOracle<ExtractionFunctionWithContext> suggestOracle;
    private final GroupingSuggestionDisplay display;
    private ValueChangeHandler valueChangeHandler;
    
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
        }, new GroupingSuggestionDisplay());
        suggestOracle = (AbstractListSuggestOracle<ExtractionFunctionWithContext>) getSuggestOracle();
        display = (GroupingSuggestionDisplay) getSuggestionDisplay();
        addSuggestionSelectionHandler(this::setExtractionFunction);
    }
    
    public void setValueChangeHandler(ValueChangeHandler valueChangeHandler) {
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
    
    private static class GroupingSuggestionDisplay extends SuggestionDisplay implements HasAnimation {
        
        private final GroupingSuggestionMenu suggestionMenu;
        private final PopupPanel suggestionPopup;
        
        private Element autoHidePartner;
        
        public GroupingSuggestionDisplay() {
            suggestionMenu = new GroupingSuggestionMenu(/*vertical*/ true);
            suggestionPopup = new PopupPanel(/*autoHide*/ true, /*modal*/ false);
            suggestionPopup.setStyleName("statisticSuggestBoxPopup");
            suggestionPopup.addStyleName("dataMiningBorderLeft");
            suggestionPopup.addStyleName("dataMiningBorderBottom");
            suggestionPopup.addStyleName("dataMiningBorderRight");
            suggestionPopup.setPreviewingAllNativeEvents(true);
            suggestionPopup.setAnimationType(AnimationType.ROLL_DOWN);
            suggestionPopup.setWidget(suggestionMenu);
        }

        @Override
        protected Suggestion getCurrentSelection() {
            return suggestionMenu.getSelectedSuggestion();
        }

        @Override
        public void hideSuggestions() {
            suggestionPopup.hide();
        }
        
        @Override
        public boolean isSuggestionListShowing() {
            return suggestionPopup.isVisible();
        }

        @Override
        protected void moveSelectionDown() {
            if (isSuggestionListShowing()) {
                suggestionMenu.selectItem(suggestionMenu.getSelectedIndex() + 1);
            }
        }

        @Override
        protected void moveSelectionUp() {
            if (isSuggestionListShowing()) {
                int selectedIndex = suggestionMenu.getSelectedIndex();
                if (selectedIndex < 0) {
                    suggestionMenu.selectItem(suggestionMenu.size() - 1);
                } else {
                    suggestionMenu.selectItem(selectedIndex - 1);
                }
            }
        }

        @Override
        protected void showSuggestions(SuggestBox suggestBox, Collection<? extends Suggestion> suggestions,
                boolean isDisplayStringHTML, boolean isAutoSelectEnabled, SuggestionCallback callback) {
            if (suggestionPopup.isAttached()) {
                suggestionPopup.hide();
            }
            
            suggestionMenu.clearItems();
            for (Suggestion suggestion : suggestions) {
                SuggestionMenuItem menuItem = new SuggestionMenuItem(suggestion, isDisplayStringHTML,
                        () -> callback.onSuggestionSelected(suggestion));
                suggestionMenu.addItem(menuItem);
            }
            
            if (isAutoSelectEnabled && suggestionMenu.size() > 0) {
                suggestionMenu.selectItem(0);
            }
            
            updateAutoHidePartner(suggestBox.getElement());
            suggestionPopup.showRelativeTo(suggestBox);
        }

        private void updateAutoHidePartner(Element newAutoHidePartner) {
            if (autoHidePartner != newAutoHidePartner) {
                if (autoHidePartner != null) {
                    suggestionPopup.removeAutoHidePartner(autoHidePartner);
                }
                suggestionPopup.addAutoHidePartner(newAutoHidePartner);
                autoHidePartner = newAutoHidePartner;
            }
        }

        @Override
        public boolean isAnimationEnabled() {
            return suggestionPopup.isAnimationEnabled();
        }

        @Override
        public void setAnimationEnabled(boolean enable) {
            suggestionPopup.setAnimationEnabled(enable);
        }
        
    }
    
    private static class GroupingSuggestionMenu extends MenuBar {
        
        public GroupingSuggestionMenu(boolean vertical) {
            super(vertical);
            setStyleName("statisticSuggestBoxPopupContent");
            setFocusOnHoverEnabled(false);
        }
        
        public int size() {
            return getItems().size();
        }

        public int getSelectedIndex() {
            MenuItem selectedItem = getSelectedItem();
            return selectedItem == null ? -1 : getItems().indexOf(selectedItem);
        }
        
        public void selectItem(int index) {
            if (index >= 0 && index < size()) {
                MenuItem itemToSelect = getItems().get(index);
                selectItem(itemToSelect);
                itemToSelect.getElement().scrollIntoView();
            }
        }

        public Suggestion getSelectedSuggestion() {
            return ((SuggestionMenuItem) getSelectedItem()).getSuggestion();
        }
        
    }
    
    private static class SuggestionMenuItem extends MenuItem {

        private final Suggestion suggestion;

        public SuggestionMenuItem(Suggestion suggestion, boolean asHTML, ScheduledCommand command) {
            super(suggestion.getDisplayString(), asHTML, command);
            this.suggestion = suggestion;
            setStyleName("item");
        }

        public Suggestion getSuggestion() {
            return suggestion;
        }

    }

}
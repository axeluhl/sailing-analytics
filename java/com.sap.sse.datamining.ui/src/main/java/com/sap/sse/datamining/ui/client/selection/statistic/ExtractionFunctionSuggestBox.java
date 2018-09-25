package com.sap.sse.datamining.ui.client.selection.statistic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.user.client.ui.HasAnimation;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.AnimationType;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.sap.sse.common.Util;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.ui.client.StringMessages;
import com.sap.sse.gwt.client.suggestion.AbstractListSuggestOracle;
import com.sap.sse.gwt.client.suggestion.CustomSuggestBox;

public class ExtractionFunctionSuggestBox extends CustomSuggestBox<ExtractionFunctionWithContext> {
    
    @FunctionalInterface
    public interface ValueChangeHandler {
        void valueChanged(ExtractionFunctionWithContext oldValue, ExtractionFunctionWithContext newValue);
    }

    private final AbstractListSuggestOracle<ExtractionFunctionWithContext> suggestOracle;
    private final ExtractionFunctionSuggestionDisplay display;
    private ValueChangeHandler valueChangeHandler;
    
    private ExtractionFunctionWithContext extractionFunction;

    @SuppressWarnings("unchecked")
    public ExtractionFunctionSuggestBox(Predicate<ExtractionFunctionWithContext> extractionFunctionSupportedPredicate) {
        super(new ExtractionFunctionSuggestOracle(extractionFunctionSupportedPredicate), new ExtractionFunctionSuggestionDisplay());
        suggestOracle = (AbstractListSuggestOracle<ExtractionFunctionWithContext>) getSuggestOracle();
        display = (ExtractionFunctionSuggestionDisplay) getSuggestionDisplay();
        addSuggestionSelectionHandler(this::setExtractionFunction);
        getValueBox().addBlurHandler(this::onBlur);
    }
    
    public void setValueChangeHandler(ValueChangeHandler valueChangeHandler) {
        this.valueChangeHandler = valueChangeHandler;
    }
    
    public Collection<ExtractionFunctionWithContext> getSelectableValues() {
        return suggestOracle.getSelectableValues();
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
    
    private void onBlur(BlurEvent event) {
        setValue(extractionFunction == null ? null : extractionFunction.getDisplayString(), false);
    }
    
    @Override
    public void hideSuggestionList() {
        display.hideSuggestions();
    }
    
    public boolean isGroupingSuggestionsByRetrieverChain() {
        return display.isGroupingSuggestionsByRetrieverChain();
    }
    
    public void setGroupingSuggestionsByRetrieverChain(boolean groupingSuggestionsByRetrieverChain) {
        display.setGroupingSuggestionsByRetrieverChain(groupingSuggestionsByRetrieverChain);
    }
    
    private static class ExtractionFunctionSuggestOracle extends AbstractListSuggestOracle<ExtractionFunctionWithContext> {

        private final Predicate<ExtractionFunctionWithContext> extractionFunctionSupportedPredicate;

        public ExtractionFunctionSuggestOracle(Predicate<ExtractionFunctionWithContext> extractionFunctionSupportedPredicate) {
            this.extractionFunctionSupportedPredicate = extractionFunctionSupportedPredicate;
        }

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
            return null;
        }
        
        @Override
        protected SimpleSuggestion createSuggestion(ExtractionFunctionWithContext match, Iterable<String> queryTokens) {
            return new ExtractionFunctionSuggestion(match, queryTokens, extractionFunctionSupportedPredicate.test(match));
        }
        
        private class ExtractionFunctionSuggestion extends SimpleSuggestion implements Comparable<ExtractionFunctionSuggestion> {

            private final boolean supported;

            public ExtractionFunctionSuggestion(ExtractionFunctionWithContext suggestObject,
                    Iterable<String> queryTokens, boolean supported) {
                super(suggestObject, queryTokens);
                this.supported = supported;
            }
            
            public boolean isSupported() {
                return supported;
            }

            @Override
            public int compareTo(ExtractionFunctionSuggestion other) {
                if (other == null) {
                    return -1;
                }
                String displayName = getSuggestObject().getExtractionFunction().getDisplayName();
                String otherDisplayName = other.getSuggestObject().getExtractionFunction().getDisplayName();
                return displayName.compareToIgnoreCase(otherDisplayName);
            }
            
        }
        
    }
    
    private static class ExtractionFunctionSuggestionDisplay extends SuggestionDisplay implements HasAnimation {

        private final StringMessages stringMessages = StringMessages.INSTANCE;
        
        private final ExtractionFunctionSuggestionMenu suggestionMenu;
        private final PopupPanel suggestionPopup;
        
        private boolean groupingSuggestionsByRetrieverChain;
        private Element autoHidePartner;
        
        public ExtractionFunctionSuggestionDisplay() {
            suggestionMenu = new ExtractionFunctionSuggestionMenu(/*vertical*/ true);
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
        
        public boolean isGroupingSuggestionsByRetrieverChain() {
            return groupingSuggestionsByRetrieverChain;
        }
        
        public void setGroupingSuggestionsByRetrieverChain(boolean groupingSuggestionsByRetrieverChain) {
            this.groupingSuggestionsByRetrieverChain = groupingSuggestionsByRetrieverChain;
        }

        @Override
        protected void showSuggestions(SuggestBox suggestBox, Collection<? extends Suggestion> suggestions,
                boolean isDisplayStringHTML, boolean isAutoSelectEnabled, SuggestionCallback callback) {
            if (suggestionPopup.isAttached()) {
                suggestionPopup.hide();
            }

            Collection<ExtractionFunctionSuggestOracle.ExtractionFunctionSuggestion> supportedSuggestions = new ArrayList<>();
            Collection<ExtractionFunctionSuggestOracle.ExtractionFunctionSuggestion> unsupportedSuggestions = new ArrayList<>();
            for (Suggestion suggestion : suggestions) {
                ExtractionFunctionSuggestOracle.ExtractionFunctionSuggestion extractionFunctionSuggestion =
                        (ExtractionFunctionSuggestOracle.ExtractionFunctionSuggestion) suggestion;
                if (extractionFunctionSuggestion.isSupported()) {
                    supportedSuggestions.add(extractionFunctionSuggestion);
                } else {
                    unsupportedSuggestions.add(extractionFunctionSuggestion);
                }
            }

            suggestionMenu.clearItems();
            if (groupingSuggestionsByRetrieverChain) {
                showSuggestionsGroupedByRetrieverChain(supportedSuggestions, isDisplayStringHTML, callback);
            } else {
                addSuggestionMenuItems(supportedSuggestions, isDisplayStringHTML, callback);
            }
            if (!unsupportedSuggestions.isEmpty()) {
                suggestionMenu.addSeparator(new UnsupportedExtractionFunctionsSeparator(stringMessages.followingStatisticsAreNotSupportedByAggregatorWarning()));
                if (groupingSuggestionsByRetrieverChain) {
                    showSuggestionsGroupedByRetrieverChain(unsupportedSuggestions, isDisplayStringHTML, callback);
                } else {
                    addSuggestionMenuItems(unsupportedSuggestions, isDisplayStringHTML, callback);
                }
            }
            
            if (isAutoSelectEnabled && suggestionMenu.size() > 0) {
                suggestionMenu.selectItem(0);
            }
            
            updateAutoHidePartner(suggestBox.getElement());
            suggestionPopup.showRelativeTo(suggestBox);
        }

        private void showSuggestionsGroupedByRetrieverChain(Collection<? extends Suggestion> suggestions,
                boolean isDisplayStringHTML, SuggestionCallback callback) {
            Map<DataRetrieverChainDefinitionDTO, List<ExtractionFunctionSuggestOracle.ExtractionFunctionSuggestion>> groupedSuggestions =
                    groupSuggestionsByRetrieverChain(suggestions);
            List<DataRetrieverChainDefinitionDTO> orderedRetrieverChains = new ArrayList<>(groupedSuggestions.keySet());
            Collections.sort(orderedRetrieverChains);
            for (DataRetrieverChainDefinitionDTO retrieverChain : orderedRetrieverChains) {
                List<ExtractionFunctionSuggestOracle.ExtractionFunctionSuggestion> suggestionsOfRetrieverChain = groupedSuggestions.get(retrieverChain);
                suggestionsOfRetrieverChain.sort(null);
                
                ExtractionFunctionSeparator separator = new ExtractionFunctionSeparator(retrieverChain);
                suggestionMenu.addSeparator(separator);
                if (!suggestionsOfRetrieverChain.get(0).isSupported()) {
                    separator.addStyleName("separator-unsupported");
                }
                addSuggestionMenuItems(suggestionsOfRetrieverChain, isDisplayStringHTML, callback);
            }
        }

        private Map<DataRetrieverChainDefinitionDTO, List<ExtractionFunctionSuggestOracle.ExtractionFunctionSuggestion>> groupSuggestionsByRetrieverChain(
                Collection<? extends Suggestion> suggestions) {
            Map<DataRetrieverChainDefinitionDTO, List<ExtractionFunctionSuggestOracle.ExtractionFunctionSuggestion>> groupedSuggestions = new HashMap<>();
            for (Suggestion suggestion : suggestions) {
                ExtractionFunctionSuggestOracle.ExtractionFunctionSuggestion extractionFunctionSuggestion = 
                        (ExtractionFunctionSuggestOracle.ExtractionFunctionSuggestion) suggestion;
                ExtractionFunctionWithContext extractionFunction = extractionFunctionSuggestion.getSuggestObject();
                DataRetrieverChainDefinitionDTO retrieverChain = extractionFunction.getRetrieverChain();
                List<ExtractionFunctionSuggestOracle.ExtractionFunctionSuggestion> suggestionsOfRetrieverChain = groupedSuggestions
                        .get(retrieverChain);
                if (suggestionsOfRetrieverChain == null) {
                    suggestionsOfRetrieverChain = new ArrayList<>();
                    groupedSuggestions.put(retrieverChain, suggestionsOfRetrieverChain);
                }
                suggestionsOfRetrieverChain.add(extractionFunctionSuggestion);
            }
            return groupedSuggestions;
        }

        private void addSuggestionMenuItems(
                Collection<ExtractionFunctionSuggestOracle.ExtractionFunctionSuggestion> suggestions,
                boolean isDisplayStringHTML, SuggestionCallback callback) {
            int count = 0;
            for (ExtractionFunctionSuggestOracle.ExtractionFunctionSuggestion suggestion : suggestions) {
                boolean isEven = count % 2 == 0;
                SuggestionMenuItem menuItem = createSuggestionMenuItem(suggestion, isDisplayStringHTML, isEven, callback);
                suggestionMenu.addItem(menuItem);
                count++;
            }
        }

        private SuggestionMenuItem createSuggestionMenuItem(ExtractionFunctionSuggestOracle.ExtractionFunctionSuggestion suggestion,
                boolean isDisplayStringHTML, boolean isEven, SuggestionCallback callback) {
            SuggestionMenuItem menuItem = new SuggestionMenuItem(suggestion, isDisplayStringHTML,
                    () -> callback.onSuggestionSelected(suggestion));
            menuItem.addStyleName("item-" + (isEven ? "even" : "odd"));
            if (!suggestion.isSupported()) {
                menuItem.addStyleName("item-unsupported");
            }
            return menuItem;
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
    
    private static class ExtractionFunctionSuggestionMenu extends MenuBar {
        
        public ExtractionFunctionSuggestionMenu(boolean vertical) {
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
    
    private static class ExtractionFunctionSeparator extends MenuItemSeparator {
        
        public ExtractionFunctionSeparator(DataRetrieverChainDefinitionDTO retrieverChain) {
            setStyleName("separator");
            getElement().getFirstChildElement().setInnerText(retrieverChain.getName());
        }
        
    }
    
    private static class UnsupportedExtractionFunctionsSeparator extends MenuItemSeparator {
        
        public UnsupportedExtractionFunctionsSeparator(String text) {
            setStyleName("separator-message");
            getElement().getFirstChildElement().setInnerText(text);
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
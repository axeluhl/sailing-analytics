package com.sap.sailing.gwt.home.shared.partials.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestBox.DefaultSuggestionDisplay;
import com.google.gwt.user.client.ui.SuggestBox.SuggestionCallback;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.AbstractListFilter;

public abstract class AbstractSuggestBoxFilter<T, C> extends AbstractTextInputFilter<T, C> {

    private final ContainsSuggestOracle suggestOracle = new ContainsSuggestOracle();
    private final List<C> suggestionObjectList = new ArrayList<>();
    protected final AbstractListFilter<C> suggestionMatchingFilter = new AbstractListFilter<C>() {
        @Override
        public Iterable<String> getStrings(C value) {
            return getMatchingStrings(value);
        }
    };
    
    protected AbstractSuggestBoxFilter(String placeholderText) {
        final SuggestBox suggestBox = new SuggestBox(suggestOracle, new TextBox(), new CustomSuggestionDisplay());
        initWidgets(suggestBox, placeholderText);
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
    
    protected abstract String createSuggestionKeyString(C value);
    
    protected abstract String createSuggestionAdditionalDisplayString(C value);
    
    protected abstract Iterable<String> getMatchingStrings(C value);
    
    private class CustomSuggestionDisplay extends DefaultSuggestionDisplay {
        @Override
        protected void showSuggestions(SuggestBox suggestBox, Collection<? extends Suggestion> suggestions,
                boolean isDisplayStringHTML, boolean isAutoSelectEnabled, SuggestionCallback callback) {
            super.showSuggestions(suggestBox, suggestions, isDisplayStringHTML, isAutoSelectEnabled, callback);
            getPopupPanel().getElement().getStyle().setProperty("maxWidth", suggestBox.getOffsetWidth(), Unit.PX);
        }
    }
    
    private class ContainsSuggestOracle extends SuggestOracle {
        
        @Override
        public void requestDefaultSuggestions(Request request, Callback callback) {
            setSuggestions(request, callback, suggestionObjectList, Collections.<String>emptyList());
        }
        
        @Override
        public void requestSuggestions(Request request, Callback callback) {
            Iterable<String> queryTokens = Collections.singleton(request.getQuery().trim());
            setSuggestions(request, callback, suggestionMatchingFilter.applyFilter(queryTokens, suggestionObjectList), queryTokens);
        }
        
        private void setSuggestions(Request request, Callback callback, Iterable<C> suggestionObjects, Iterable<String> queryTokens) {
            List<Suggestion> suggestions = new ArrayList<>();
            List<String> normalizedQueryTokens = new ArrayList<>();
            for (String token : queryTokens) {
                normalizedQueryTokens.add(token.toLowerCase());
            }
            int count = 0;
            for (C match : suggestionObjects) {
                suggestions.add(new SimpleSuggestion(match, normalizedQueryTokens));
                if (++count >= request.getLimit()) {
                    break;
                }
            }
            Response response = new Response(suggestions);
            response.setMoreSuggestionsCount(Util.size(suggestionObjects) - count);
            callback.onSuggestionsReady(request, response);
        }
        
        @Override
        public boolean isDisplayStringHTML() {
          return true;
        }
    }
    
    private class SimpleSuggestion implements Suggestion {
        
        private static final String STRONG_TAG_OPEN = "<strong>", STRONG_TAG_CLOSE = "</strong>";
        private static final String ITALIC_TAG_OPEN = "<em>", ITALIC_TAG_CLOSE = "</em>";
        private final C suggestObject;
        private final Iterable<String> queryTokens;
        
        private SimpleSuggestion(C suggestObject, Iterable<String> queryTokens) {
            this.suggestObject = suggestObject;
            this.queryTokens = queryTokens;
        }

        @Override
        public String getDisplayString() {
            SafeHtmlBuilder builder = new SafeHtmlBuilder();
            appendHighlighted(builder, createSuggestionKeyString(suggestObject));
            builder.append(SafeHtmlUtils.fromTrustedString(ITALIC_TAG_OPEN));
            builder.append(SafeHtmlUtils.fromSafeConstant(" - "));
            appendHighlighted(builder, createSuggestionAdditionalDisplayString(suggestObject));
            builder.append(SafeHtmlUtils.fromTrustedString(ITALIC_TAG_CLOSE));
            return builder.toSafeHtml().asString();
        }
        
        private void appendHighlighted(SafeHtmlBuilder builder, String displayString) {
            String normalizedString = displayString.toLowerCase();
            int cursor = 0;
            while(true) {
                int index = displayString.length();
                String matchToken = null;
                for (String token : queryTokens) {
                    int matchIndex = normalizedString.indexOf(token, cursor);
                    if (matchIndex >= 0 && matchIndex < index) {
                        index = matchIndex;
                        matchToken = token;
                    }
                }
                if (matchToken != null) {
                    builder.appendEscaped(displayString.substring(cursor, index));
                    builder.append(SafeHtmlUtils.fromTrustedString(STRONG_TAG_OPEN));
                    builder.appendEscaped(displayString.substring(index, index + matchToken.length()));
                    builder.append(SafeHtmlUtils.fromTrustedString(STRONG_TAG_CLOSE));
                    cursor = index + matchToken.length();
                } else {
                    builder.appendEscaped(displayString.substring(cursor));
                    break;
                }
            }
        }

        @Override
        public String getReplacementString() {
            return createSuggestionKeyString(suggestObject);
        }
    }
}

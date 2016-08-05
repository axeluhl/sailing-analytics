package com.sap.sailing.gwt.home.shared.partials.filter;

import com.sap.sailing.gwt.common.client.suggestion.AbstractSuggestOracle;
import com.sap.sailing.gwt.common.client.suggestion.CustomSuggestBox;
import com.sap.sailing.gwt.common.client.suggestion.CustomSuggestBox.SuggestionSelectionHandler;

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
    
//    protected final void setSuggestions(Request request, Callback callback, Iterable<C> suggestionObjects,
//            Iterable<String> queryTokens) {
//        suggestOracle.setSuggestions(request, callback, suggestionObjects, queryTokens);
//    }
//    
//    protected abstract void getSuggestions(Request request, Callback callback, Iterable<String> queryTokens);
//    
//    protected abstract String createSuggestionKeyString(C value);
//    
//    protected abstract String createSuggestionAdditionalDisplayString(C value);
//    
//    protected abstract Iterable<String> getMatchingStrings(C value);

//    private final ContainsSuggestOracle suggestOracle = new ContainsSuggestOracle();
//    
//    protected AbstractSuggestBoxFilter(String placeholderText) {
//        final SuggestBox suggestBox = new SuggestBox(suggestOracle, new TextBox(), new CustomSuggestionDisplay());
//        initWidgets(suggestBox, placeholderText);
//        suggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
//            @Override
//            public void onSelection(SelectionEvent<Suggestion> event) {
//                @SuppressWarnings("unchecked")
//                C suggestObject = ((SimpleSuggestion) event.getSelectedItem()).suggestObject;
//                AbstractSuggestBoxFilter.this.onSuggestionSelected(suggestObject);
//                AbstractSuggestBoxFilter.super.update();
//            }
//        });
//    }
//    
//    protected final void setSuggestions(Request request, Callback callback, Iterable<C> suggestionObjects,
//            Iterable<String> queryTokens) {
//        List<Suggestion> suggestions = new ArrayList<>();
//        List<String> normalizedQueryTokens = new ArrayList<>();
//        for (String token : queryTokens) {
//            normalizedQueryTokens.add(token.toLowerCase());
//        }
//        int count = 0;
//        for (C match : suggestionObjects) {
//            suggestions.add(new SimpleSuggestion(match, normalizedQueryTokens));
//            if (++count >= request.getLimit()) {
//                break;
//            }
//        }
//        Response response = new Response(suggestions);
//        response.setMoreSuggestionsCount(Util.size(suggestionObjects) - count);
//        callback.onSuggestionsReady(request, response);
//    }
//    
//    protected abstract void getSuggestions(Request request, Callback callback, Iterable<String> queryTokens);
//    
//    protected abstract void onSuggestionSelected(C selectedItem);
//    
//    protected abstract String createSuggestionKeyString(C value);
//    
//    protected abstract String createSuggestionAdditionalDisplayString(C value);
//    
//    protected abstract Iterable<String> getMatchingStrings(C value);
//    
//    private class CustomSuggestionDisplay extends DefaultSuggestionDisplay {
//        @Override
//        protected void showSuggestions(SuggestBox suggestBox, Collection<? extends Suggestion> suggestions,
//                boolean isDisplayStringHTML, boolean isAutoSelectEnabled, SuggestionCallback callback) {
//            super.showSuggestions(suggestBox, suggestions, isDisplayStringHTML, isAutoSelectEnabled, callback);
//            getPopupPanel().getElement().getStyle().setProperty("maxWidth", suggestBox.getOffsetWidth(), Unit.PX);
//        }
//    }
//    
//    private class ContainsSuggestOracle extends SuggestOracle {
//        
//        @Override
//        public void requestDefaultSuggestions(Request request, Callback callback) {
//            getSuggestions(request, callback, Collections.<String>emptyList());
//        }
//        
//        @Override
//        public void requestSuggestions(Request request, Callback callback) {
//            String normalizedQuery = request.getQuery().trim();
//            if (normalizedQuery == null || normalizedQuery.isEmpty()) {
//                requestDefaultSuggestions(request, callback);
//            } else {
//                Iterable<String> queryTokens = Collections.singleton(normalizedQuery);
//                getSuggestions(request, callback, queryTokens);
//            }
//        }
//        
//        @Override
//        public boolean isDisplayStringHTML() {
//            return true;
//        }
//    }
//    
//    private class SimpleSuggestion implements Suggestion {
//        
//        private static final String STRONG_TAG_OPEN = "<strong>", STRONG_TAG_CLOSE = "</strong>";
//        private static final String ITALIC_TAG_OPEN = "<em>", ITALIC_TAG_CLOSE = "</em>";
//        private final C suggestObject;
//        private final Iterable<String> queryTokens;
//        
//        private SimpleSuggestion(C suggestObject, Iterable<String> queryTokens) {
//            this.suggestObject = suggestObject;
//            this.queryTokens = queryTokens;
//        }
//
//        @Override
//        public String getDisplayString() {
//            SafeHtmlBuilder builder = new SafeHtmlBuilder();
//            appendHighlighted(builder, createSuggestionKeyString(suggestObject));
//            String additionalString = createSuggestionAdditionalDisplayString(suggestObject);
//            if(additionalString != null && !additionalString.isEmpty()) {
//                builder.append(SafeHtmlUtils.fromTrustedString(ITALIC_TAG_OPEN));
//                builder.append(SafeHtmlUtils.fromSafeConstant(" - "));
//                appendHighlighted(builder, additionalString);
//                builder.append(SafeHtmlUtils.fromTrustedString(ITALIC_TAG_CLOSE));
//            }
//            return builder.toSafeHtml().asString();
//        }
//        
//        private void appendHighlighted(SafeHtmlBuilder builder, String displayString) {
//            String normalizedString = displayString.toLowerCase();
//            int cursor = 0;
//            while(true) {
//                int index = displayString.length();
//                String matchToken = null;
//                for (String token : queryTokens) {
//                    int matchIndex = normalizedString.indexOf(token, cursor);
//                    if (matchIndex >= 0 && matchIndex < index) {
//                        index = matchIndex;
//                        matchToken = token;
//                    }
//                }
//                if (matchToken != null) {
//                    builder.appendEscaped(displayString.substring(cursor, index));
//                    builder.append(SafeHtmlUtils.fromTrustedString(STRONG_TAG_OPEN));
//                    builder.appendEscaped(displayString.substring(index, index + matchToken.length()));
//                    builder.append(SafeHtmlUtils.fromTrustedString(STRONG_TAG_CLOSE));
//                    cursor = index + matchToken.length();
//                } else {
//                    builder.appendEscaped(displayString.substring(cursor));
//                    break;
//                }
//            }
//        }
//
//        @Override
//        public String getReplacementString() {
//            return createSuggestionKeyString(suggestObject);
//        }
//    }
}

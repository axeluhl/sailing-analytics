package com.sap.sailing.gwt.home.desktop.places.user.profile.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.view.client.ProvidesKey;

public class SuggestedMultiSelectionListDataProvider<T> extends AbstractSuggestedMultiSelectionDataProvider<T> {
    
    private final ProvidesKey<T> keyProvider;
    private final List<T> suggestionItemsList = new ArrayList<T>();
    
    public SuggestedMultiSelectionListDataProvider(ProvidesKey<T> keyProvider) {
        this.keyProvider = keyProvider;
    }

    @Override
    public Object getKey(T item) {
        return keyProvider == null ? item : keyProvider.getKey(item);
    }
    
    public void setSuggestionItems(Collection<T> suggestionItems) {
        suggestionItemsList.clear();
        suggestionItemsList.addAll(suggestionItems);
    }
    
    @Override
    protected void getSuggestions(String query, SuggestionItemsCallback<T> callback) {
        callback.setSuggestionItems(suggestionItemsList);
    }
    
}

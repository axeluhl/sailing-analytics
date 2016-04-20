package com.sap.sailing.gwt.home.desktop.places.user.profile.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.view.client.ProvidesKey;

public class SuggestedMultiSelectionListDataProvider<T> implements SuggestedMultiSelectionDataProvider<T> {
    
    private final Map<Object, T> selectedItemsMap = new HashMap<>();
    private final ProvidesKey<T> keyProvider;
    private final List<T> suggestionItemsList = new ArrayList<T>();
    
    public SuggestedMultiSelectionListDataProvider(ProvidesKey<T> keyProvider) {
        this.keyProvider = keyProvider;
    }

    @Override
    public Object getKey(T item) {
        return keyProvider == null ? item : keyProvider.getKey(item);
    }
    
    @Override
    public void addSelection(T item) {
        Object key = getKey(item);
        selectedItemsMap.put(key, item);
    }
    
    @Override
    public void removeSelection(T item) {
        Object key = getKey(item);
        selectedItemsMap.remove(key);
    }
    
    @Override
    public void clearSelection() {
        selectedItemsMap.clear();
    }
    
    public void setSuggestionItems(Collection<T> suggestionItems) {
        suggestionItemsList.clear();
        suggestionItemsList.addAll(suggestionItems);
    }
    
    @Override
    public void getSuggestionItems(String query, final SuggestionItemsCallback<T> callback) {
        List<T> filteredSuggestionItems = new ArrayList<>();
        for (T item : suggestionItemsList) {
            Object key = getKey(item);
            if (!selectedItemsMap.containsKey(key)) {
                filteredSuggestionItems.add(item);
            }
        }
        callback.setSuggestionItems(filteredSuggestionItems);
    }
    
}

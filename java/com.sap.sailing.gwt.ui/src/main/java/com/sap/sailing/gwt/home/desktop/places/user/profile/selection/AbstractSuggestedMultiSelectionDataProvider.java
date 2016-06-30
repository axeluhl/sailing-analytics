package com.sap.sailing.gwt.home.desktop.places.user.profile.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractSuggestedMultiSelectionDataProvider<T> 
        implements SuggestedMultiSelectionDataProvider<T> {
    
    private final Map<Object, T> selectedItemsMap = new HashMap<>();
    private boolean notificationsEnabled;
    
    @Override
    public final void addSelection(T item) {
        Object key = getKey(item);
        selectedItemsMap.put(key, item);
    }
    
    @Override
    public final void removeSelection(T item) {
        Object key = getKey(item);
        selectedItemsMap.remove(key);
    }
    
    @Override
    public final void clearSelection() {
        selectedItemsMap.clear();
    }
    
    @Override
    public final void setNotifications(boolean enabled) {
        notificationsEnabled = enabled;
    }
    
    @Override
    public final void getSuggestionItems(String query, final SuggestionItemsCallback<T> callback) {
        final SuggestionItemsCallback<T> internalCallback = new SuggestionItemsCallback<T>() {
            @Override
            public void setSuggestionItems(Collection<T> suggestionItems) {
                List<T> filteredSuggestionItems = new ArrayList<>();
                for (T item : suggestionItems) {
                    Object key = getKey(item);
                    if (!selectedItemsMap.containsKey(key)) {
                        filteredSuggestionItems.add(item);
                    }
                }
                callback.setSuggestionItems(filteredSuggestionItems);
            }
        };
        this.getSuggestions(query, internalCallback);
    }
    
    protected abstract void getSuggestions(String query, SuggestionItemsCallback<T> callback);
    
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }
    
    public Collection<T> getSelectedItems() {
        return new ArrayList<>(selectedItemsMap.values());
    }
    
}

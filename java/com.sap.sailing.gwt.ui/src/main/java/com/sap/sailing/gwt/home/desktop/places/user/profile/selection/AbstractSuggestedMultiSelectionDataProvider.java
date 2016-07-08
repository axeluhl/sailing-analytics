package com.sap.sailing.gwt.home.desktop.places.user.profile.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.view.client.ProvidesKey;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.SuggestedMultiSelectionDataProvider.Display;

public abstract class AbstractSuggestedMultiSelectionDataProvider<T, D extends Display<T>> 
        implements SuggestedMultiSelectionDataProvider<T, D> {
    
    private final ProvidesKey<T> keyProvider;
    private final Map<Object, T> selectedItemsMap = new HashMap<>();
    protected Set<D> displays = new HashSet<>();
    
    protected AbstractSuggestedMultiSelectionDataProvider(ProvidesKey<T> keyProvider) {
        this.keyProvider = keyProvider;
    }
    
    @Override
    public void addDisplay(D display) {
        this.displays.add(display);
    }
    
    @Override
    public final Object getKey(T item) {
        return keyProvider == null ? item : keyProvider.getKey(item);
    }
    
    @Override
    public final void addSelection(T item) {
        Object key = getKey(item);
        selectedItemsMap.put(key, item);
        persist();
    }
    
    @Override
    public final void removeSelection(T item) {
        Object key = getKey(item);
        selectedItemsMap.remove(key);
        persist();
    }
    
    @Override
    public final void clearSelection() {
        selectedItemsMap.clear();
        persist();
    }
    
    @Override
    public final void initSelectedItems(Collection<T> selectedItems) {
        for (T item : selectedItems) {
            selectedItemsMap.put(getKey(item), item);
        }
        for (D display : displays) {
            display.setSelectedItems(selectedItems);
        }
    }
    
    @Override
    public final void getSuggestionItems(Iterable<String> queryTokens, int limit,
            final SuggestionItemsCallback<T> callback) {
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
        this.getSuggestions(queryTokens, limit, internalCallback);
    }
    
    @Override
    public void persist() {
        this.persist(new ArrayList<>(selectedItemsMap.values()));
    }

    protected abstract void getSuggestions(Iterable<String> queryTokens, int limit,
            SuggestionItemsCallback<T> callback);
    
    protected abstract void persist(Collection<T> selectedItem);
    
}

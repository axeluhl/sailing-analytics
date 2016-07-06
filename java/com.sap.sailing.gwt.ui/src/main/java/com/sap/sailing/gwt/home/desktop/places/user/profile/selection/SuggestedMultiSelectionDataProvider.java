package com.sap.sailing.gwt.home.desktop.places.user.profile.selection;

import java.util.Collection;

import com.google.gwt.view.client.ProvidesKey;

public interface SuggestedMultiSelectionDataProvider<T> extends ProvidesKey<T> {
    
    void addSelection(T item);
    
    void removeSelection(T item);
    
    void clearSelection();
    
    void setNotifications(boolean enabled);
    
    void getSuggestionItems(Iterable<String> queryTokens, int limit, final SuggestionItemsCallback<T> callback);
    
    void persist(Collection<T> selectedItems);
    
    interface SuggestionItemsCallback<T> {
        void setSuggestionItems(Collection<T> suggestionItems);
    }

}

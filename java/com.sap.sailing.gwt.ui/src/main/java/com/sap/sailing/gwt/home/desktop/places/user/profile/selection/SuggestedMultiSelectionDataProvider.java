package com.sap.sailing.gwt.home.desktop.places.user.profile.selection;

import java.util.Collection;

import com.google.gwt.view.client.ProvidesKey;

public interface SuggestedMultiSelectionDataProvider<T> extends ProvidesKey<T> {
    
    void addSelection(T item);
    
    void removeSelection(T item);
    
    void clearSelection();
    
    void setNotifications(boolean enabled);
    
    void getSuggestionItems(String query, final SuggestionItemsCallback<T> callback);
    
    interface SuggestionItemsCallback<T> {
        void setSuggestionItems(Collection<T> suggestionItems);
    }

}

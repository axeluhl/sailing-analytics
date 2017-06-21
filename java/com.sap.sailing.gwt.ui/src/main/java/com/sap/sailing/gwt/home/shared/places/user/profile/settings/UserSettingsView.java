package com.sap.sailing.gwt.home.shared.places.user.profile.settings;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionDataProvider;

/**
 * Interface for the user preferences UI. To support desktop as well as mobile version, an
 * {@link #setEdgeToEdge(boolean) edge-to-edge} flag can be provided, in order to meet the respective layout
 * requirements.
 */
public interface UserSettingsView extends IsWidget {

    /**
     * Defines whether or not the {@link UserSettingsView} should be optimized to fill the whole display width.
     * 
     * @param edgeToEdge
     *            <code>true</code> if this view is used in an edge-to-edge layout (usually in mobile version),
     *            <code>false</code> otherwise
     */
    void setEdgeToEdge(boolean edgeToEdge);

    void setEntries(List<UserSettingsEntry> entries);

    /**
     * Presenter interface for the user preferences UI, providing methods to load preferences and to access the required
     * {@link SuggestedMultiSelectionDataProvider}s.
     */
    public interface Presenter {
        void loadData();

        void remove(UserSettingsEntry entry);
        
        void setView(UserSettingsView view);
    }

}

package com.sap.sailing.gwt.home.shared.places.user.profile.settings;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionDataProvider;
import com.sap.sse.common.filter.Filter;

/**
 * Interface for the user preferences UI. To support desktop as well as mobile version, an
 * {@link #setEdgeToEdge(boolean) edge-to-edge} flag can be provided, in order to meet the respective layout
 * requirements.
 */
public interface UserSettingsView extends IsWidget {

    Filter<UserSettingsEntry> getFilter();

    void setEntries(List<UserSettingsEntry> entries);

    /**
     * Presenter interface for the user preferences UI, providing methods to load preferences and to access the required
     * {@link SuggestedMultiSelectionDataProvider}s.
     */
    public interface Presenter {
        void loadData();

        void updateData();

        void remove(UserSettingsEntry entry);
        
        void setView(UserSettingsView view);
    }

}

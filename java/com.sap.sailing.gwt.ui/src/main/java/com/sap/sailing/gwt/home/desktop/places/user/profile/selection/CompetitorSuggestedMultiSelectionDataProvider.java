package com.sap.sailing.gwt.home.desktop.places.user.profile.selection;

import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.CompetitorSuggestedMultiSelectionDataProvider.Display;

public interface CompetitorSuggestedMultiSelectionDataProvider
        extends SuggestedMultiSelectionDataProvider<SimpleCompetitorWithIdDTO, Display> {
    
    boolean isNotifyAboutResults();
    
    void setNotifyAboutResults(boolean notifyAboutResults);
    
    void initNotifications(boolean notifyAboutResults);

    public static interface Display extends SuggestedMultiSelectionDataProvider.Display<SimpleCompetitorWithIdDTO> {
        
        void setNotifyAboutResults(boolean notifyAboutResults);
    }
}

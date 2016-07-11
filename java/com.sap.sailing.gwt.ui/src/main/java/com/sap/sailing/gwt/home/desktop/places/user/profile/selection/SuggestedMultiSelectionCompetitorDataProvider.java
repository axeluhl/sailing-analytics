package com.sap.sailing.gwt.home.desktop.places.user.profile.selection;

import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.SuggestedMultiSelectionCompetitorDataProvider.Display;

public interface SuggestedMultiSelectionCompetitorDataProvider
        extends SuggestedMultiSelectionDataProvider<SimpleCompetitorWithIdDTO, Display> {
    
    void setNotifyAboutResults(boolean notifyAboutResults);
    
    void initNotifications(boolean notifyAboutResults);

    public static interface Display extends SuggestedMultiSelectionDataProvider.Display<SimpleCompetitorWithIdDTO> {
        
        void setNotifyAboutResults(boolean notifyAboutResults);
    }
}

package com.sap.sailing.gwt.home.shared.partials.multiselection;

import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionCompetitorDataProvider.Display;

public interface SuggestedMultiSelectionCompetitorDataProvider
        extends SuggestedMultiSelectionDataProvider<SimpleCompetitorWithIdDTO, Display> {
    
    void setNotifyAboutResults(boolean notifyAboutResults);
    
    void initNotifications(boolean notifyAboutResults);

    public static interface Display extends SuggestedMultiSelectionDataProvider.Display<SimpleCompetitorWithIdDTO> {
        
        void setNotifyAboutResults(boolean notifyAboutResults);
    }
}

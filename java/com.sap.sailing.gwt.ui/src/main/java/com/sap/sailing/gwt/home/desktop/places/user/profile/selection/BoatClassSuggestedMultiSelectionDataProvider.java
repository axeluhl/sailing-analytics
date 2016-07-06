package com.sap.sailing.gwt.home.desktop.places.user.profile.selection;

import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.BoatClassSuggestedMultiSelectionDataProvider.Display;

public interface BoatClassSuggestedMultiSelectionDataProvider
        extends SuggestedMultiSelectionDataProvider<BoatClassMasterdata, Display> {
    
    boolean isNotifyAboutUpcomingRaces();
    
    void setNotifyAboutUpcomingRaces(boolean notifyAboutUpcomingRaces);
    
    boolean isNotifyAboutResults();
    
    void setNotifyAboutResults(boolean notifyAboutResults);
    
    void initNotifications(boolean notifyAboutUpcomingRaces, boolean notifyAboutResults);
    
    interface Display extends SuggestedMultiSelectionDataProvider.Display<BoatClassMasterdata> {
        
        void setNotifyAboutUpcomingRaces(boolean notifyAboutUpcomingRaces);
        
        void setNotifyAboutResults(boolean notifyAboutResults);
    }

}

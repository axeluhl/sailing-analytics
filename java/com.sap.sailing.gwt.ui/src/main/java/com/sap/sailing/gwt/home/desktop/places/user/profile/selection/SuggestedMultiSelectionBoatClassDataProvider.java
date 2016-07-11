package com.sap.sailing.gwt.home.desktop.places.user.profile.selection;

import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.SuggestedMultiSelectionBoatClassDataProvider.Display;

public interface SuggestedMultiSelectionBoatClassDataProvider
        extends SuggestedMultiSelectionDataProvider<BoatClassDTO, Display> {
    
    void setNotifyAboutUpcomingRaces(boolean notifyAboutUpcomingRaces);
    
    void setNotifyAboutResults(boolean notifyAboutResults);
    
    void initNotifications(boolean notifyAboutUpcomingRaces, boolean notifyAboutResults);
    
    interface Display extends SuggestedMultiSelectionDataProvider.Display<BoatClassDTO> {
        
        void setNotifyAboutUpcomingRaces(boolean notifyAboutUpcomingRaces);
        
        void setNotifyAboutResults(boolean notifyAboutResults);
    }

}

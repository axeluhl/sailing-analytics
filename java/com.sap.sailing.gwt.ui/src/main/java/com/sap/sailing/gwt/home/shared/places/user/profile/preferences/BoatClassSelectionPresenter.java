package com.sap.sailing.gwt.home.shared.places.user.profile.preferences;

import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionPresenter;

public interface BoatClassSelectionPresenter
        extends SuggestedMultiSelectionPresenter<BoatClassDTO, BoatClassSelectionPresenter.Display> {
    
    void setNotifyAboutUpcomingRaces(boolean notifyAboutUpcomingRaces);
    
    void setNotifyAboutResults(boolean notifyAboutResults);
    
    void initNotifications(boolean notifyAboutUpcomingRaces, boolean notifyAboutResults);
    
    interface Display extends SuggestedMultiSelectionPresenter.Display<BoatClassDTO> {
        
        void setNotifyAboutUpcomingRaces(boolean notifyAboutUpcomingRaces);
        
        void setNotifyAboutResults(boolean notifyAboutResults);
    }

}

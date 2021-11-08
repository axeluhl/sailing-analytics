package com.sap.sailing.gwt.home.shared.places.user.profile.preferences;

import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionPresenter;

public interface CompetitorSelectionPresenter
        extends SuggestedMultiSelectionPresenter<SimpleCompetitorWithIdDTO, CompetitorSelectionPresenter.Display> {
    
    void setNotifyAboutResults(boolean notifyAboutResults);
    
    void initNotifications(boolean notifyAboutResults);

    public static interface Display extends SuggestedMultiSelectionPresenter.Display<SimpleCompetitorWithIdDTO> {
        
        void setNotifyAboutResults(boolean notifyAboutResults);
    }
}

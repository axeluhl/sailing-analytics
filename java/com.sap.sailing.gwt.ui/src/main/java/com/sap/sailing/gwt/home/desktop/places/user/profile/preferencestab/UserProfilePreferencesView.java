package com.sap.sailing.gwt.home.desktop.places.user.profile.preferencestab;

import java.util.Collection;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.SuggestedMultiSelectionDataProvider;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;
import com.sap.sse.security.ui.authentication.decorator.NotLoggedInPresenter;

public interface UserProfilePreferencesView extends IsWidget {

    void setPresenter(Presenter presenter);
    
    NeedsAuthenticationContext getDecorator();
    
    void setFavouriteCompetitors(Collection<SimpleCompetitorWithIdDTO> selectedItems);
    
    void setFavouriteBoatClasses(Collection<BoatClassMasterdata> selectedItems);

    public interface Presenter extends NotLoggedInPresenter, NeedsAuthenticationContext {
        SuggestedMultiSelectionDataProvider<BoatClassMasterdata> getFavoriteBoatClassesDataProvider();
        SuggestedMultiSelectionDataProvider<SimpleCompetitorWithIdDTO> getFavoriteCompetitorsDataProvider();
        void start();
    }
}
package com.sap.sailing.gwt.home.desktop.places.user.profile.preferencestab;

import java.util.Collection;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorDTO;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.SuggestedMultiSelectionDataProvider;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;
import com.sap.sse.security.ui.authentication.decorator.NotLoggedInPresenter;
import com.sap.sse.security.ui.userprofile.shared.userdetails.UserDetailsView;

public interface UserProfilePreferencesView extends IsWidget {

    void setPresenter(Presenter presenter);
    
    UserDetailsView getUserDetailsView();
    
    NeedsAuthenticationContext getDecorator();
    
    void setCompetitorsIBelongTo(Collection<SimpleCompetitorDTO> selectedItems);
    
    void setFavouriteCompetitors(Collection<SimpleCompetitorDTO> selectedItems);
    
    void setFavouriteBoatClasses(Collection<BoatClassMasterdata> selectedItems);

    public interface Presenter extends NotLoggedInPresenter, NeedsAuthenticationContext {
        SuggestedMultiSelectionDataProvider<BoatClassMasterdata> getBoatClassDataProvider();
        SuggestedMultiSelectionDataProvider<SimpleCompetitorDTO> getCompetitorDataProvider();
    }
}
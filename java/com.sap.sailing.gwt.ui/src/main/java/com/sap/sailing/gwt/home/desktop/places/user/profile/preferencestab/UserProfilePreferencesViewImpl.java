package com.sap.sailing.gwt.home.desktop.places.user.profile.preferencestab;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.SuggestedMultiSelection;
import com.sap.sailing.gwt.home.shared.usermanagement.decorator.AuthorizedContentDecoratorDesktop;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;
import com.sap.sse.security.ui.userprofile.shared.userdetails.UserDetailsView;

public class UserProfilePreferencesViewImpl extends Composite implements UserProfilePreferencesView {

    interface MyBinder extends UiBinder<Widget, UserProfilePreferencesViewImpl> {
    }

    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    @UiField(provided = true) AuthorizedContentDecoratorDesktop decoratorUi;
    @UiField(provided = true) SuggestedMultiSelection<SimpleCompetitorWithIdDTO> favoriteCompetitorsSelctionUi;
    @UiField(provided = true) SuggestedMultiSelection<BoatClassMasterdata> favoriteBoatClassesSelctionUi;
    
    public UserDetailsView getUserDetailsView() {
        return null;
    }
    
    @Override
    public void setPresenter(Presenter presenter) {
        decoratorUi = new AuthorizedContentDecoratorDesktop(presenter);
        favoriteCompetitorsSelctionUi = SuggestedMultiSelection
                .forCompetitors(presenter.getFavoriteCompetitorsDataProvider(), "TODO Favourite competitors");
        favoriteBoatClassesSelctionUi = SuggestedMultiSelection
                .forBoatClasses(presenter.getFavoriteBoatClassesDataProvider(), "TODO Favourite boat classes");
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @Override
    public void setFavouriteCompetitors(Collection<SimpleCompetitorWithIdDTO> selectedItems) {
        favoriteCompetitorsSelctionUi.setSelectedItems(selectedItems);
    }
    
    @Override
    public void setFavouriteBoatClasses(Collection<BoatClassMasterdata> selectedItems) {
        favoriteBoatClassesSelctionUi.setSelectedItems(selectedItems);
    }
    
    @Override
    public NeedsAuthenticationContext getDecorator() {
        return decoratorUi;
    }
}
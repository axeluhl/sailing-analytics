package com.sap.sailing.gwt.home.desktop.places.user.profile.preferencestab;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorDTO;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.SuggestedMultiSelection;
import com.sap.sailing.gwt.home.shared.usermanagement.decorator.AuthorizedContentDecoratorDesktop;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;
import com.sap.sse.security.ui.userprofile.shared.userdetails.UserDetailsView;

public class UserProfilePreferencesViewImpl extends Composite implements UserProfilePreferencesView {

    interface MyBinder extends UiBinder<Widget, UserProfilePreferencesViewImpl> {
    }

    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    @UiField(provided = true) AuthorizedContentDecoratorDesktop decoratorUi;
    @UiField(provided = true) SuggestedMultiSelection<SimpleCompetitorDTO> competitorsIBelongToSelctionUi;
    @UiField(provided = true) SuggestedMultiSelection<SimpleCompetitorDTO> favouriteCompetitorsSelctionUi;
    @UiField(provided = true) SuggestedMultiSelection<BoatClassDTO> favouriteBoatClassesSelctionUi;
    
    public UserDetailsView getUserDetailsView() {
        return null;
    }
    
    @Override
    public void setPresenter(Presenter presenter) {
        decoratorUi = new AuthorizedContentDecoratorDesktop(presenter);
        competitorsIBelongToSelctionUi = new SuggestedMultiSelection<>(new ListDataProvider<SimpleCompetitorDTO>(),
                "TODO Competitors I belong to");
        favouriteCompetitorsSelctionUi = new SuggestedMultiSelection<>(new ListDataProvider<SimpleCompetitorDTO>(),
                "TODO Favourite competitors");
        favouriteBoatClassesSelctionUi = new SuggestedMultiSelection<>(new ListDataProvider<BoatClassDTO>(),
                "TODO Favourite boat classes");
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @Override
    public NeedsAuthenticationContext getDecorator() {
        return decoratorUi;
    }
}
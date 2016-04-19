package com.sap.sailing.gwt.home.desktop.places.user.profile.preferencestab;

import java.util.Arrays;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.domain.common.BoatClassMasterdata;
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
    @UiField(provided = true) SuggestedMultiSelection<BoatClassMasterdata> favouriteBoatClassesSelctionUi;
    
    public UserDetailsView getUserDetailsView() {
        return null;
    }
    
    @Override
    public void setPresenter(Presenter presenter) {
        decoratorUi = new AuthorizedContentDecoratorDesktop(presenter);
        competitorsIBelongToSelctionUi = SuggestedMultiSelection.forCompetitors(
                new ListDataProvider<SimpleCompetitorDTO>(), "TODO Competitors I belong to");
        favouriteCompetitorsSelctionUi = SuggestedMultiSelection.forCompetitors(
                new ListDataProvider<SimpleCompetitorDTO>(), "TODO Favourite competitors");
        favouriteBoatClassesSelctionUi = SuggestedMultiSelection.forBoatClasses(
                new ListDataProvider<BoatClassMasterdata>(), "TODO Favourite boat classes");
        initWidget(uiBinder.createAndBindUi(this));
        
        favouriteCompetitorsSelctionUi.setSelectedItems(Arrays.asList(
                new SimpleCompetitorDTO("John Doe", "GBR001", "gb", null),
                new SimpleCompetitorDTO("Max Mustermann", "GER001", "de", null),
                new SimpleCompetitorDTO("Competitor with a very long name to test wrapping in favourite list", "USA 1337", "us", null)));
        favouriteBoatClassesSelctionUi.setSelectedItems(Arrays.asList(
                BoatClassMasterdata.KIELZUGVOGEL,
                BoatClassMasterdata.J22));
    }
    
    @Override
    public NeedsAuthenticationContext getDecorator() {
        return decoratorUi;
    }
}
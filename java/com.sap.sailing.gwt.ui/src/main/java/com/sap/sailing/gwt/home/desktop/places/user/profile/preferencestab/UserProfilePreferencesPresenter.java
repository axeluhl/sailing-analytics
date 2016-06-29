package com.sap.sailing.gwt.home.desktop.places.user.profile.preferencestab;

import java.util.Arrays;

import com.google.gwt.view.client.ProvidesKey;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileView;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.SuggestedMultiSelectionDataProvider;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.SuggestedMultiSelectionListDataProvider;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;

public class UserProfilePreferencesPresenter implements UserProfilePreferencesView.Presenter {

    private final UserProfilePreferencesView view;
    private final UserProfileView.Presenter userProfilePresenter;

    public UserProfilePreferencesPresenter(UserProfilePreferencesView view, UserProfileView.Presenter userProfilePresenter) {
        this.view = view;
        this.userProfilePresenter = userProfilePresenter;
        view.setPresenter(this);
        view.setFavouriteBoatClasses(Arrays.asList(
                BoatClassMasterdata.KIELZUGVOGEL,
                BoatClassMasterdata.J22));
    }

    @Override
    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        view.getDecorator().setAuthenticationContext(authenticationContext);
//        userDetailsPresenter.setAuthenticationContext(authenticationContext);
    }
    
    @Override
    public void doTriggerLoginForm() {
        userProfilePresenter.doTriggerLoginForm();
    }
    
    @Override
    public SuggestedMultiSelectionDataProvider<BoatClassMasterdata> getFavouriteBoatClassesDataProvider() {
        SuggestedMultiSelectionListDataProvider<BoatClassMasterdata> dataProvider =
                new SuggestedMultiSelectionListDataProvider<>(new ProvidesKey<BoatClassMasterdata>() {
                    @Override
                    public Object getKey(BoatClassMasterdata item) {
                        return item.getDisplayName();
                    }
                });
        dataProvider.setSuggestionItems(Arrays.asList(BoatClassMasterdata.values()));
        return dataProvider;
    }
    
//    @Override
//    public SuggestedMultiSelectionDataProvider<SimpleCompetitorDTO> getFavouriteCompetitorsDataProvider() {
//        SuggestedMultiSelectionListDataProvider<SimpleCompetitorDTO> dataProvider =
//                new SuggestedMultiSelectionListDataProvider<>(new ProvidesKey<SimpleCompetitorDTO>() {
//                    @Override
//                    public Object getKey(SimpleCompetitorDTO item) {
//                        return item.getSailID();
//                    }
//                });
//        dataProvider.setSuggestionItems(competitorsDummyData);
//        return dataProvider;
//    }
//    
//    private final List<SimpleCompetitorDTO> competitorsDummyData = Arrays.asList(
//            new SimpleCompetitorDTO("John Doe", "GBR001", "gb", null),
//            new SimpleCompetitorDTO("Max Mustermann", "GER001", "de", null),
//            new SimpleCompetitorDTO("Competitor with a long name to test wrapping in favourite list", "USA 1337", "us", null));
}

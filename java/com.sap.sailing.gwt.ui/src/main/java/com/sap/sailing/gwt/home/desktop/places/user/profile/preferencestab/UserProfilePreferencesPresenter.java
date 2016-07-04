package com.sap.sailing.gwt.home.desktop.places.user.profile.preferencestab;

import java.util.Arrays;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.ProvidesKey;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorDTO;
import com.sap.sailing.gwt.home.communication.user.profile.CompetitorSuggestionResult;
import com.sap.sailing.gwt.home.communication.user.profile.GetCompetitorSuggestionAction;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileView;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.AbstractSuggestedMultiSelectionDataProvider;
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
    }
    
    @Override
    public void doTriggerLoginForm() {
        userProfilePresenter.doTriggerLoginForm();
    }
    
    @Override
    public SuggestedMultiSelectionDataProvider<BoatClassMasterdata> getFavoriteBoatClassesDataProvider() {
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
    
    @Override
    public SuggestedMultiSelectionDataProvider<SimpleCompetitorDTO> getFavoriteCompetitorsDataProvider() {
        return new AbstractSuggestedMultiSelectionDataProvider<SimpleCompetitorDTO>(new ProvidesKey<SimpleCompetitorDTO>() {
                    @Override
                    public Object getKey(SimpleCompetitorDTO item) {
                        return item.getSailID();
                    }
                }) {

                @Override
                protected void getSuggestions(String query, final SuggestionItemsCallback<SimpleCompetitorDTO> callback) {
                    userProfilePresenter.getDispatch().execute(new GetCompetitorSuggestionAction(query, 20), 
                        new AsyncCallback<CompetitorSuggestionResult>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                Window.alert("Error while loading competitor suggestion");
                            }

                            @Override
                            public void onSuccess(CompetitorSuggestionResult result) {
                                callback.setSuggestionItems(result.getValues());
                            }
                        });
                }
        };
    }
    
}

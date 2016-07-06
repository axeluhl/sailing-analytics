package com.sap.sailing.gwt.home.desktop.places.user.profile.preferencestab;

import java.util.Arrays;
import java.util.List;

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
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.AbstractListFilter;
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
        return new BoatClassSuggestedMultiSelectionDataProvider();
    }
    
    @Override
    public SuggestedMultiSelectionDataProvider<SimpleCompetitorDTO> getFavoriteCompetitorsDataProvider() {
        return new CompetitorSuggestedMultiSelectionDataProvider();
    }
    
    private class BoatClassSuggestedMultiSelectionDataProvider
            extends AbstractSuggestedMultiSelectionDataProvider<BoatClassMasterdata> {
        private final AbstractListFilter<BoatClassMasterdata> filter = new AbstractListFilter<BoatClassMasterdata>() {
            @Override
            public Iterable<String> getStrings(BoatClassMasterdata item) {
                return item.getBoatClassNames();
            }
        };
        
        private BoatClassSuggestedMultiSelectionDataProvider() {
            super(new ProvidesKey<BoatClassMasterdata>() {
                @Override
                public Object getKey(BoatClassMasterdata item) {
                    return item.getDisplayName();
                }
            });
        }
        @Override
        protected void getSuggestions(Iterable<String> queryTokens, int limit,
                SuggestionItemsCallback<BoatClassMasterdata> callback) {
            List<BoatClassMasterdata> boatClasses = Arrays.asList(BoatClassMasterdata.values());
            callback.setSuggestionItems(Util.asList(filter.applyFilter(queryTokens, boatClasses)));
        }
    }
    
    private class CompetitorSuggestedMultiSelectionDataProvider
            extends AbstractSuggestedMultiSelectionDataProvider<SimpleCompetitorDTO> {
        private CompetitorSuggestedMultiSelectionDataProvider() {
            super(new ProvidesKey<SimpleCompetitorDTO>() {
                @Override
                public Object getKey(SimpleCompetitorDTO item) {
                    return item.getSailID();
                }
            });
        }
        
        @Override
        protected void getSuggestions(Iterable<String> queryTokens, int limit,
                final SuggestionItemsCallback<SimpleCompetitorDTO> callback) {
            userProfilePresenter.getClientFactory().getDispatch().execute(
                    new GetCompetitorSuggestionAction(queryTokens, limit),
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
    }
}

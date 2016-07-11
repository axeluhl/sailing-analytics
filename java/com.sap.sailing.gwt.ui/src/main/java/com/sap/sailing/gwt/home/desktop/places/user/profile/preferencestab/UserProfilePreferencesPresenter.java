package com.sap.sailing.gwt.home.desktop.places.user.profile.preferencestab;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.CompetitorSuggestionResult;
import com.sap.sailing.gwt.home.communication.user.profile.FavoriteBoatClassesDTO;
import com.sap.sailing.gwt.home.communication.user.profile.FavoriteCompetitorsDTO;
import com.sap.sailing.gwt.home.communication.user.profile.FavoritesResult;
import com.sap.sailing.gwt.home.communication.user.profile.GetCompetitorSuggestionAction;
import com.sap.sailing.gwt.home.communication.user.profile.GetFavoritesAction;
import com.sap.sailing.gwt.home.communication.user.profile.SaveFavoriteBoatClassesAction;
import com.sap.sailing.gwt.home.communication.user.profile.SaveFavoriteCompetitorsAction;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileView;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.AbstractSuggestedMultiSelectionBoatClassDataProvider;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.AbstractSuggestedMultiSelectionCompetitorDataProvider;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.SuggestedMultiSelectionBoatClassDataProvider;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.SuggestedMultiSelectionCompetitorDataProvider;
import com.sap.sse.gwt.dispatch.shared.commands.VoidResult;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;

public class UserProfilePreferencesPresenter implements UserProfilePreferencesView.Presenter {

    private final UserProfilePreferencesView view;
    private final UserProfileView.Presenter userProfilePresenter;
    private final SuggestedMultiSelectionCompetitorDataProvider competitorDataProvider =
            new SuggestedMultiSelectionCompetitorDataProviderImpl();
    private final SuggestedMultiSelectionBoatClassDataProvider boatClassDataProvider =
            new SuggestedMultiSelectionBoatClassDataProviderImpl();
            
    public UserProfilePreferencesPresenter(final UserProfilePreferencesView view,
            final UserProfileView.Presenter userProfilePresenter) {
        this.view = view;
        this.userProfilePresenter = userProfilePresenter;
        view.setPresenter(this);
    }
    
    @Override
    public void start() {
        userProfilePresenter.getClientFactory().getDispatch().execute(new GetFavoritesAction(),
                new AsyncCallback<FavoritesResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        userProfilePresenter.getClientFactory()
                                .createErrorView("Error while loading notification preferences!", caught);
                    }

                    @Override
                    public void onSuccess(FavoritesResult result) {
                        initFavoriteCompetitors(result.getFavoriteCompetitors());
                        initFavoriteBoatClasses(result.getFavoriteBoatClasses());
                    }
                });
    }
    
    private void initFavoriteCompetitors(FavoriteCompetitorsDTO favoriteCompetitors) {
        competitorDataProvider.initNotifications(favoriteCompetitors.isNotifyAboutResults());
        competitorDataProvider.initSelectedItems(favoriteCompetitors.getSelectedCompetitors());
    }
    
    private void initFavoriteBoatClasses(FavoriteBoatClassesDTO favoriteBoatClasses) {
        boatClassDataProvider.initNotifications(favoriteBoatClasses.isNotifyAboutUpcomingRaces(),
                favoriteBoatClasses.isNotifyAboutResults());
        boatClassDataProvider.initSelectedItems(favoriteBoatClasses.getSelectedBoatClasses());
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
    public SuggestedMultiSelectionBoatClassDataProvider getFavoriteBoatClassesDataProvider() {
        return boatClassDataProvider;
    }
    
    @Override
    public SuggestedMultiSelectionCompetitorDataProvider getFavoriteCompetitorsDataProvider() {
        return competitorDataProvider;
    }
    
    private class SuggestedMultiSelectionBoatClassDataProviderImpl
            extends AbstractSuggestedMultiSelectionBoatClassDataProvider {
        @Override
        protected void persist(FavoriteBoatClassesDTO favorites) {
            userProfilePresenter.getClientFactory().getDispatch().execute(
                    new SaveFavoriteBoatClassesAction(favorites), new SaveAsyncCallback());
        }
    }
    
    private class SuggestedMultiSelectionCompetitorDataProviderImpl
            extends AbstractSuggestedMultiSelectionCompetitorDataProvider {
        
        @Override
        protected void getSuggestions(Iterable<String> queryTokens, int limit,
                final SuggestionItemsCallback<SimpleCompetitorWithIdDTO> callback) {
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
        
        @Override
        protected void persist(FavoriteCompetitorsDTO favorites) {
            userProfilePresenter.getClientFactory().getDispatch().execute(new SaveFavoriteCompetitorsAction(favorites),
                    new SaveAsyncCallback());
        }
    }
    
    private class SaveAsyncCallback implements AsyncCallback<VoidResult> {
        @Override
        public void onFailure(Throwable caught) {
            userProfilePresenter.getClientFactory().createErrorView(
                    "Error while saving notification preferences!", caught);
        }

        @Override
        public void onSuccess(VoidResult result) {
        }
    }
}

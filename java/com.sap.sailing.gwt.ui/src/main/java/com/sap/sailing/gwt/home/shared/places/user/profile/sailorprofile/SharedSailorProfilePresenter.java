package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.PlaceController;
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
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.SailorProfileDetailsView;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithDispatch;
import com.sap.sailing.gwt.home.shared.partials.multiselection.AbstractSuggestedMultiSelectionBoatClassDataProvider;
import com.sap.sailing.gwt.home.shared.partials.multiselection.AbstractSuggestedMultiSelectionCompetitorDataProvider;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionBoatClassDataProvider;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionCompetitorDataProvider;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.dataprovider.SailorProfileDataProvider;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.dataprovider.SailorProfileDataProviderImpl;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.mvp.ClientFactory;
import com.sap.sse.gwt.dispatch.shared.commands.VoidResult;

/**
 * Reusable implementation of {@link SharedSailorProfileView.Presenter} which handles the sailor profiles. It only
 * require an appropriate client factory which implements // * {@link ClientFactoryWithDispatch},
 * {@link ErrorAndBusyClientFactory} and {@link ClientFactory}.
 * 
 * @param <C>
 *            the provided client factory type
 */
public class SharedSailorProfilePresenter<C extends ClientFactoryWithDispatch & ErrorAndBusyClientFactory & ClientFactory>
        implements SharedSailorProfileView.Presenter {

    private final SuggestedMultiSelectionCompetitorDataProvider competitorDataProvider = new SuggestedMultiSelectionCompetitorDataProviderImpl();
    private final SuggestedMultiSelectionBoatClassDataProvider boatClassDataProvider = new SuggestedMultiSelectionBoatClassDataProviderImpl();
    private final C clientFactory;

    private final SailorProfileDataProvider sailorProfileDataProvider;

    private SuggestedMultiSelectionCompetitorDataProvider adjustedCompetitorDataProvider = null;

    private SuggestedMultiSelectionCompetitorDataProvider createDataProviderIfNecessary() {
        if (adjustedCompetitorDataProvider == null) {
            adjustedCompetitorDataProvider = new SharedSailorProfileCompetitorDataProvider(competitorDataProvider);
        }
        return adjustedCompetitorDataProvider;
    }

    public SharedSailorProfilePresenter(C clientFactory) {
        this.clientFactory = clientFactory;
        this.sailorProfileDataProvider = new SailorProfileDataProviderImpl(clientFactory);
    }

    @Override
    public void loadPreferences() {
        clientFactory.getDispatch().execute(new GetFavoritesAction(), new AsyncCallback<FavoritesResult>() {
            @Override
            public void onFailure(Throwable caught) {
                clientFactory.createErrorView("Error while loading notification preferences!", caught);
            }

            @Override
            public void onSuccess(FavoritesResult result) {
                initFavoriteCompetitors(result.getFavoriteCompetitors());
                initFavoriteBoatClasses(result.getFavoriteBoatClasses());
            }
        });
    }

    @Override
    public SuggestedMultiSelectionCompetitorDataProvider getFavoriteCompetitorsDataProvider() {
        return competitorDataProvider;
    }

    @Override
    public SuggestedMultiSelectionBoatClassDataProvider getFavoriteBoatClassesDataProvider() {
        return boatClassDataProvider;
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

    private class SuggestedMultiSelectionBoatClassDataProviderImpl
            extends AbstractSuggestedMultiSelectionBoatClassDataProvider {
        @Override
        protected void persist(FavoriteBoatClassesDTO favorites) {
            clientFactory.getDispatch().execute(new SaveFavoriteBoatClassesAction(favorites), new SaveAsyncCallback());
        }
    }

    private class SuggestedMultiSelectionCompetitorDataProviderImpl
            extends AbstractSuggestedMultiSelectionCompetitorDataProvider {

        @Override
        protected void getSuggestions(Iterable<String> queryTokens, int limit,
                final SuggestionItemsCallback<SimpleCompetitorWithIdDTO> callback) {
            clientFactory.getDispatch().execute(new GetCompetitorSuggestionAction(queryTokens, limit),
                    new AsyncCallback<CompetitorSuggestionResult>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            Notification.notify("Error while loading competitor suggestion", NotificationType.ERROR);
                        }

                        @Override
                        public void onSuccess(CompetitorSuggestionResult result) {
                            callback.setSuggestionItems(result.getValues());
                        }
                    });
        }

        @Override
        protected void persist(FavoriteCompetitorsDTO favorites) {
            clientFactory.getDispatch().execute(new SaveFavoriteCompetitorsAction(favorites), new SaveAsyncCallback());
        }
    }

    private class SaveAsyncCallback implements AsyncCallback<VoidResult> {
        @Override
        public void onFailure(Throwable caught) {
            clientFactory.createErrorView("Error while saving notification preferences!", caught);
        }

        @Override
        public void onSuccess(VoidResult result) {
        }
    }

    @Override
    public SailorProfileDataProvider getDataProvider() {
        return sailorProfileDataProvider;
    }

    @Override
    public SuggestedMultiSelectionCompetitorDataProvider getCompetitorsDataProvider() {
        return createDataProviderIfNecessary();
    }

    @Override
    public PlaceController getPlaceController() {
        return clientFactory.getPlaceController();
    }

    @Override
    public void refreshSailorProfile(UUID uuid, SailorProfileDetailsView sailorView) {
        getDataProvider().findSailorProfileById(uuid, new AsyncCallback<SailorProfileDTO>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log(caught.getMessage(), caught);
            }

            @Override
            public void onSuccess(SailorProfileDTO result) {
                sailorView.setEntry(result);
            }
        });
    }

}

package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile;

import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.CompetitorSuggestionResult;
import com.sap.sailing.gwt.home.communication.user.profile.FavoriteCompetitorsDTO;
import com.sap.sailing.gwt.home.communication.user.profile.GetCompetitorSuggestionAction;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithDispatch;
import com.sap.sailing.gwt.home.shared.partials.multiselection.AbstractSuggestedMultiSelectionCompetitorDataProvider;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.dataprovider.StatefulSailorProfileDataProvider;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.mvp.ClientFactory;

/**
 * Reusable implementation of {@link EditSailorProfileView.Presenter} which handles the sailor profiles. It only require
 * an appropriate client factory which implements // * {@link ClientFactoryWithDispatch},
 * {@link ErrorAndBusyClientFactory} and {@link ClientFactory}.
 * 
 * @param <C>
 *            the provided client factory type
 */
public class EditSailorProfilePresenter<C extends ClientFactoryWithDispatch & ErrorAndBusyClientFactory & ClientFactory>
        implements EditSailorProfileView.Presenter {

    private final C clientFactory;

    private final StatefulSailorProfileDataProvider sailorProfileDataProvider;

    public EditSailorProfilePresenter(C clientFactory) {
        this.clientFactory = clientFactory;
        this.sailorProfileDataProvider = new StatefulSailorProfileDataProvider(clientFactory,
                new SuggestedMultiSelectionCompetitorDataProviderImpl());
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
            /** persistence is done in {@link StatefulSailorProfileDataProvider} */
        }
    }

    @Override
    public StatefulSailorProfileDataProvider getDataProvider() {
        return sailorProfileDataProvider;
    }

    @Override
    public PlaceController getPlaceController() {
        return clientFactory.getPlaceController();
    }
}

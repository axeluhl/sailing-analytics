package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile;

import java.util.Collection;

import com.google.gwt.place.shared.PlaceController;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithDispatch;
import com.sap.sailing.gwt.home.shared.partials.multiselection.AbstractSuggestedCompetitorMultiSelectionPresenter;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionPresenter;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.dataprovider.SailorProfileDataProvider;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.dataprovider.SailorProfilesCompetitorSelectionPresenter;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;
import com.sap.sse.gwt.client.mvp.ClientFactory;

/**
 * Reusable implementation of {@link EditSailorProfileView.Presenter} which handles the sailor profiles. It only require
 * an appropriate client factory which implements // * {@link ClientFactoryWithDispatch},
 * {@link ErrorAndBusyClientFactory} and {@link ClientFactory}.
 * 
 * @param <C>
 *            the provided client factory type
 */
public class EditSailorProfilePresenter implements EditSailorProfileView.Presenter {

    private final ClientFactoryWithDispatchAndError clientFactory;

    private final SailorProfileDataProvider sailorProfileDataProvider;
    private final SailorProfilesCompetitorSelectionPresenter sailorProfilesCompetitorSelectionPresenter;

    public EditSailorProfilePresenter(ClientFactoryWithDispatchAndError clientFactory) {
        this.clientFactory = clientFactory;
        this.sailorProfileDataProvider = new SailorProfileDataProvider(clientFactory);
        this.sailorProfilesCompetitorSelectionPresenter = new SailorProfilesCompetitorSelectionPresenter(
                new SuggestedMultiSelectionCompetitorDataProviderImpl(clientFactory), this.sailorProfileDataProvider);
        this.sailorProfileDataProvider.setCompetitorSelectionPresenter(this.sailorProfilesCompetitorSelectionPresenter);
    }

    private class SuggestedMultiSelectionCompetitorDataProviderImpl extends
            AbstractSuggestedCompetitorMultiSelectionPresenter<SuggestedMultiSelectionPresenter.Display<SimpleCompetitorWithIdDTO>> {

        private SuggestedMultiSelectionCompetitorDataProviderImpl(ClientFactoryWithDispatch clientFactory) {
            super(clientFactory);
        }

        @Override
        protected void persist(Collection<SimpleCompetitorWithIdDTO> selectedItem) {
            /** persistence is done in {@link SailorProfileDataProvider} */
        }
    }

    @Override
    public SailorProfileDataProvider getDataProvider() {
        return sailorProfileDataProvider;
    }

    @Override
    public PlaceController getPlaceController() {
        return clientFactory.getPlaceController();
    }

    public ClientFactoryWithDispatchAndError getClientFactory() {
        return clientFactory;
    }

    @Override
    public SailorProfilesCompetitorSelectionPresenter getCompetitorPresenter() {
        return sailorProfilesCompetitorSelectionPresenter;
    }
}

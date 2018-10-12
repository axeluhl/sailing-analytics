package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.dataprovider;

import java.util.Collection;
import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileEventsDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileNumericStatisticType;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileStatisticDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfilesDTO;
import com.sap.sailing.gwt.home.communication.user.profile.sailorprofile.CreateSailorProfileAction;
import com.sap.sailing.gwt.home.communication.user.profile.sailorprofile.GetAllSailorProfilesAction;
import com.sap.sailing.gwt.home.communication.user.profile.sailorprofile.GetEventsForSailorProfileAction;
import com.sap.sailing.gwt.home.communication.user.profile.sailorprofile.GetNumericStatisticForSailorProfileAction;
import com.sap.sailing.gwt.home.communication.user.profile.sailorprofile.GetSailorProfileAction;
import com.sap.sailing.gwt.home.communication.user.profile.sailorprofile.RemoveSailorProfileAction;
import com.sap.sailing.gwt.home.communication.user.profile.sailorprofile.UpdateSailorProfileCompetitorsAction;
import com.sap.sailing.gwt.home.communication.user.profile.sailorprofile.UpdateSailorProfileTitleAction;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.ClientFactoryWithDispatchAndError;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.EditSailorProfileView;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SailorProfilePlace;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;

/**
 * Data Provider to provide information for the sailor profile overview and single sailor profiles. This data provider
 * is also used to add, update or delete sailor profiles. The logic for handling of the competitors (except storing and
 * loading them) can be found in {@link SailorProfilesCompetitorSelectionPresenter}, which is also notified with every
 * refresh.
 */
public class SailorProfileDataProviderImpl implements SailorProfileDataProvider {

    private EditSailorProfileView sailorView;
    private SailorProfilesCompetitorSelectionPresenter competitorSelectionPresenter;

    private final ClientFactoryWithDispatchAndError clientFactory;

    public SailorProfileDataProviderImpl(ClientFactoryWithDispatchAndError clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void setCompetitorSelectionPresenter(
            SailorProfilesCompetitorSelectionPresenter competitorSelectionPresenter) {
        this.competitorSelectionPresenter = competitorSelectionPresenter;
    }

    @Override
    public void setView(EditSailorProfileView sailorView) {
        this.sailorView = sailorView;
    }

    @Override
    public void loadSailorProfile(UUID uuid) {
        clientFactory.getDispatch().execute(new GetSailorProfileAction(uuid), createRefreshCallback(uuid));
    }

    @Override
    public void updateTitle(UUID uuid, String newTitle) {
        clientFactory.getDispatch().execute(new UpdateSailorProfileTitleAction(uuid, newTitle),
                createRefreshCallback(uuid));
    }

    @Override
    public void getEvents(UUID uuid, AsyncCallback<SailorProfileEventsDTO> asyncCallback) {
        clientFactory.getDispatch().execute(new GetEventsForSailorProfileAction(uuid), asyncCallback);
    }

    @Override
    public void loadSailorProfiles(AsyncCallback<SailorProfilesDTO> callback) {
        clientFactory.getDispatch().execute(new GetAllSailorProfilesAction(), callback);
    }

    @Override
    public void createNewEntry(UUID uuid, String newTitle) {
        clientFactory.getDispatch().execute(new CreateSailorProfileAction(uuid, newTitle), createRefreshCallback(uuid));
    }

    @Override
    public void removeSailorProfile(UUID uuid, AsyncCallback<SailorProfileDTO> callback) {
        clientFactory.getDispatch().execute(new RemoveSailorProfileAction(uuid), callback);
    }

    @Override
    public void getStatisticFor(UUID uuid, SailorProfileNumericStatisticType type,
            AsyncCallback<SailorProfileStatisticDTO> callback) {
        clientFactory.getDispatch().execute(new GetNumericStatisticForSailorProfileAction(uuid, type), callback);
    }

    @Override
    public void updateCompetitors(UUID uuid, Collection<SimpleCompetitorWithIdDTO> competitors,
            SailorProfilesCompetitorSelectionPresenter competitorSelectionProvider) {
        clientFactory.getDispatch().execute(new UpdateSailorProfileCompetitorsAction(uuid, competitors),
                createRefreshCallback(uuid, competitorSelectionProvider));
    }

    private AsyncCallback<SailorProfileDTO> createRefreshCallback(UUID uuid) {
        return createRefreshCallback(uuid, this.competitorSelectionPresenter);
    }

    private AsyncCallback<SailorProfileDTO> createRefreshCallback(UUID uuid,
            SailorProfilesCompetitorSelectionPresenter competitorSelectionProvider) {
        return new AsyncCallback<SailorProfileDTO>() {

            @Override
            public void onFailure(Throwable caught) {
                Notification.notify(caught.getMessage(), NotificationType.ERROR);
            }

            @Override
            public void onSuccess(SailorProfileDTO result) {
                if (result.isNotFoundOnServer()) {
                    String uuidAsString = uuid == null ? StringMessages.INSTANCE.unknown() : uuid.toString();
                    Notification.notify(StringMessages.INSTANCE.unknownSailorProfile(uuidAsString),
                            NotificationType.ERROR);
                    clientFactory.getPlaceController().goTo(new SailorProfilePlace());
                } else {
                    if (competitorSelectionProvider != null) {
                        competitorSelectionProvider.setCompetitorsAndUUID(result.getCompetitors(), result.getKey());
                    }
                    sailorView.setEntry(result);
                }
            }
        };
    }

}

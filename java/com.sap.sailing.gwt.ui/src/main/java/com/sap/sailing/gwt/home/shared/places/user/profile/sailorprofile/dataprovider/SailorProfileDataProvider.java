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

public class SailorProfileDataProvider {

    private EditSailorProfileView sailorView;
    private SailorProfilesCompetitorSelectionPresenter competitorSelectionPresenter;

    private final ClientFactoryWithDispatchAndError clientFactory;

    public SailorProfileDataProvider(ClientFactoryWithDispatchAndError clientFactory) {
        this.clientFactory = clientFactory;
    }

    public void setCompetitorSelectionPresenter(
            SailorProfilesCompetitorSelectionPresenter competitorSelectionPresenter) {
        this.competitorSelectionPresenter = competitorSelectionPresenter;
    }

    public void setView(EditSailorProfileView sailorView) {
        this.sailorView = sailorView;
    }

    public void loadSailorProfile(UUID uuid) {
        clientFactory.getDispatch().execute(new GetSailorProfileAction(uuid), createRefreshCallback(uuid));
    }

    public void updateTitle(UUID uuid, String newTitle) {
        clientFactory.getDispatch().execute(new UpdateSailorProfileTitleAction(uuid, newTitle),
                createRefreshCallback(uuid));
    }

    public void getEvents(UUID uuid, AsyncCallback<SailorProfileEventsDTO> asyncCallback) {
        clientFactory.getDispatch().execute(new GetEventsForSailorProfileAction(uuid), asyncCallback);
    }

    public void loadSailorProfiles(AsyncCallback<SailorProfilesDTO> callback) {
        clientFactory.getDispatch().execute(new GetAllSailorProfilesAction(), callback);
    }

    public void createNewEntry(UUID uuid, String newTitle) {
        clientFactory.getDispatch().execute(new CreateSailorProfileAction(uuid, newTitle), createRefreshCallback(uuid));
    }

    public void removeSailorProfile(UUID uuid, AsyncCallback<SailorProfileDTO> callback) {
        clientFactory.getDispatch().execute(new RemoveSailorProfileAction(uuid), callback);
    }

    public void getStatisticFor(UUID uuid, SailorProfileNumericStatisticType type,
            AsyncCallback<SailorProfileStatisticDTO> callback) {
        clientFactory.getDispatch().execute(new GetNumericStatisticForSailorProfileAction(uuid, type), callback);
    }

    public AsyncCallback<SailorProfileDTO> createRefreshCallback(UUID uuid) {
        return createRefreshCallback(uuid, this.competitorSelectionPresenter);
    }

    public void updateCompetitors(UUID uuid, Collection<SimpleCompetitorWithIdDTO> competitors,
            SailorProfilesCompetitorSelectionPresenter competitorSelectionProvider) {
        clientFactory.getDispatch().execute(new UpdateSailorProfileCompetitorsAction(uuid, competitors),
                createRefreshCallback(uuid, competitorSelectionProvider));
    }

    public AsyncCallback<SailorProfileDTO> createRefreshCallback(UUID uuid,
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

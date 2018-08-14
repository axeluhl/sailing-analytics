package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.dataprovider;

import java.util.Collection;
import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.communication.user.profile.GetSailorProfilesAction;
import com.sap.sailing.gwt.home.communication.user.profile.SaveSailorProfileAction;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfilesDTO;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithDispatch;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.domain.ParticipatedEventDTO;
import com.sap.sse.gwt.dispatch.shared.commands.VoidResult;

public class SailorProfileDataProviderImpl implements SailorProfileDataProvider {

    private Collection<ParticipatedEventDTO> events;

    private final ClientFactoryWithDispatch clientFactory;

    public SailorProfileDataProviderImpl(ClientFactoryWithDispatch clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void loadSailorProfiles(AsyncCallback<SailorProfilesDTO> callback) {
        clientFactory.getDispatch().execute(new GetSailorProfilesAction(), callback);
    }

    @Override
    public void findSailorProfileById(UUID uuid, AsyncCallback<SailorProfileDTO> callback) {
        clientFactory.getDispatch().execute(new GetSailorProfilesAction(uuid),
                new AsyncCallback<SailorProfilesDTO>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        callback.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(SailorProfilesDTO result) {
                        callback.onSuccess(result.getEntries().get(0));
                    }
                });
    }

    @Override
    public void getEvents(UUID key, AsyncCallback<Iterable<ParticipatedEventDTO>> asyncCallback) {
        asyncCallback.onSuccess(events);
    }

    @Override
    public void updateOrCreateSailorProfile(SailorProfileDTO sailorProfile,
            AsyncCallback<SailorProfilesDTO> callback) {
        clientFactory.getDispatch().execute(new SaveSailorProfileAction(sailorProfile),
                new AsyncCallback<VoidResult>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        callback.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(VoidResult result) {
                    }
                });
        clientFactory.getDispatch().execute(new GetSailorProfilesAction(sailorProfile.getKey()), callback);
    }

}

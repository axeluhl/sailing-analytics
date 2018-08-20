package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.dataprovider;

import java.util.Collection;
import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileEventsDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfilesDTO;
import com.sap.sailing.gwt.home.communication.user.profile.sailorprofile.CreateSailorProfileAction;
import com.sap.sailing.gwt.home.communication.user.profile.sailorprofile.GetAllSailorProfilesAction;
import com.sap.sailing.gwt.home.communication.user.profile.sailorprofile.GetEventsForSailorProfileAction;
import com.sap.sailing.gwt.home.communication.user.profile.sailorprofile.GetSailorProfileAction;
import com.sap.sailing.gwt.home.communication.user.profile.sailorprofile.RemoveSailorProfileAction;
import com.sap.sailing.gwt.home.communication.user.profile.sailorprofile.UpdateSailorProfileCompetitorsAction;
import com.sap.sailing.gwt.home.communication.user.profile.sailorprofile.UpdateSailorProfileTitleAction;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithDispatch;

public class SailorProfileDataProviderImpl implements SailorProfileDataProvider {

    private final ClientFactoryWithDispatch clientFactory;

    public SailorProfileDataProviderImpl(ClientFactoryWithDispatch clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void loadSailorProfiles(AsyncCallback<SailorProfilesDTO> callback) {
        clientFactory.getDispatch().execute(new GetAllSailorProfilesAction(), callback);
    }

    @Override
    public void findSailorProfileById(UUID uuid, AsyncCallback<SailorProfileDTO> callback) {
        clientFactory.getDispatch().execute(new GetSailorProfileAction(uuid), callback);
    }

    @Override
    public void getEvents(UUID key, AsyncCallback<SailorProfileEventsDTO> callback) {
        clientFactory.getDispatch().execute(new GetEventsForSailorProfileAction(key), callback);
    }

    @Override
    public void createNewSailorProfile(UUID key, String name, AsyncCallback<SailorProfileDTO> callback) {
        clientFactory.getDispatch().execute(new CreateSailorProfileAction(key, name), callback);
    }

    @Override
    public void updateTitle(UUID key, String updatedTitle, AsyncCallback<SailorProfileDTO> callback) {
        clientFactory.getDispatch().execute(new UpdateSailorProfileTitleAction(key, updatedTitle), callback);
    }

    @Override
    public void updateCompetitors(UUID key, Collection<SimpleCompetitorWithIdDTO> competitors,
            AsyncCallback<SailorProfileDTO> callback) {
        clientFactory.getDispatch().execute(new UpdateSailorProfileCompetitorsAction(key, competitors), callback);
    }

    @Override
    public void removeSailorProfile(UUID uuid, AsyncCallback<SailorProfileDTO> callback) {
        clientFactory.getDispatch().execute(new RemoveSailorProfileAction(uuid), callback);
    }
}

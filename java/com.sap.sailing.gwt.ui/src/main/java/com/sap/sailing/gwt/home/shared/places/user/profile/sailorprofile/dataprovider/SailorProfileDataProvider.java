package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.dataprovider;

import java.util.Collection;
import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileEventsDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfilesDTO;

public interface SailorProfileDataProvider {

    void findSailorProfileById(UUID uuid, AsyncCallback<SailorProfileDTO> asyncCallback);

    void getEvents(UUID key, AsyncCallback<SailorProfileEventsDTO> callback);

    void loadSailorProfiles(AsyncCallback<SailorProfilesDTO> callback);

    void updateCompetitors(UUID key, Collection<SimpleCompetitorWithIdDTO> competitors,
            AsyncCallback<SailorProfileDTO> callback);

    void updateTitle(UUID key, String updatedTitle, AsyncCallback<SailorProfileDTO> callback);

    void createNewSailorProfile(UUID key, String name, AsyncCallback<SailorProfileDTO> callback);

    void removeSailorProfile(UUID uuid, AsyncCallback<SailorProfileDTO> callback);


}

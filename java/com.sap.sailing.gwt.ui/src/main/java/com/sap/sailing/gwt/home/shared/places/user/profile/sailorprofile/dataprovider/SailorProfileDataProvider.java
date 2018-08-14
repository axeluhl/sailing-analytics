package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.dataprovider;

import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfilesDTO;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.domain.ParticipatedEventDTO;

public interface SailorProfileDataProvider {

    void findSailorProfileById(UUID uuid, AsyncCallback<SailorProfileDTO> asyncCallback);

    void getEvents(UUID key, AsyncCallback<Iterable<ParticipatedEventDTO>> asyncCallback);

    void loadSailorProfiles(AsyncCallback<SailorProfilesDTO> callback);

    void updateOrCreateSailorProfile(SailorProfileDTO sailorProfile, AsyncCallback<SailorProfilesDTO> callback);

}

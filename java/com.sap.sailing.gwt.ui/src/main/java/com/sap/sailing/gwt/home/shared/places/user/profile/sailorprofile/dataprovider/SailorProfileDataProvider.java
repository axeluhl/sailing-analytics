package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.dataprovider;

import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileEntries;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileEntry;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.domain.ParticipatedEventDTO;

public interface SailorProfileDataProvider {

    void findSailorProfileById(UUID uuid, AsyncCallback<SailorProfileEntry> asyncCallback);

    void getEvents(UUID key, AsyncCallback<Iterable<ParticipatedEventDTO>> asyncCallback);

    void loadSailorProfiles(AsyncCallback<SailorProfileEntries> callback);

    void updateOrCreateSailorProfile(SailorProfileEntry sailorProfile, AsyncCallback<SailorProfileEntries> callback);

}

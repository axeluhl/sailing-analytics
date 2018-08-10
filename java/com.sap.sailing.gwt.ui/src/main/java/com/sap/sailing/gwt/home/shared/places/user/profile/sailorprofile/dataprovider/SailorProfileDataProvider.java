package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.dataprovider;

import java.util.Collection;
import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.domain.ParticipatedEventDTO;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.domain.SailorProfileEntry;

public interface SailorProfileDataProvider {

    Collection<SailorProfileEntry> loadSailorProfiles();

    void findSailorProfileById(UUID uuid, AsyncCallback<SailorProfileEntry> asyncCallback);

    void getEvents(UUID key, AsyncCallback<Collection<ParticipatedEventDTO>> asyncCallback);

}

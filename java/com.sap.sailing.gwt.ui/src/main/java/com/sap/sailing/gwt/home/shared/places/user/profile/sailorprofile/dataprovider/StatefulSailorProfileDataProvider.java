package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.dataprovider;

import java.util.UUID;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfilesDTO;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithDispatch;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.EditSailorProfileView;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.domain.ParticipatedEventDTO;

public class StatefulSailorProfileDataProvider {

    private SailorProfileDataProvider sailorProfileDataProvider;

    private SailorProfileDTO sailorProfile;

    private EditSailorProfileView sailorView;

    public StatefulSailorProfileDataProvider(ClientFactoryWithDispatch clientFactory) {
        sailorProfileDataProvider = new SailorProfileDataProviderImpl(clientFactory);
    }

    public void setView(EditSailorProfileView sailorView) {
        this.sailorView = sailorView;
    }

    public void loadSailorProfile(UUID uuid) {
        sailorProfileDataProvider.findSailorProfileById(uuid, new AsyncCallback<SailorProfileDTO>() {

            @Override
            public void onSuccess(SailorProfileDTO result) {
                sailorProfile = result;
                sailorView.setEntry(result);
            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log(caught.getMessage(), caught);
            }
        });
    }

    private void sendUpdateToBackend() {
        sailorProfileDataProvider.updateOrCreateSailorProfile(sailorProfile, new AsyncCallback<SailorProfilesDTO>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log(caught.getMessage(), caught);
            }

            @Override
            public void onSuccess(SailorProfilesDTO result) {
                sailorProfile = result.getEntries().get(0);
                sailorView.setEntry(sailorProfile);
            }
        });
    }

    public void updateTitle(String newTitle) {
        sailorProfile.setName(newTitle);
        sendUpdateToBackend();
    }

    public void removeCompetitor(SimpleCompetitorWithIdDTO selectedItem) {
        sailorProfile.getCompetitors().remove(selectedItem);
        sendUpdateToBackend();

    }

    public void clearCompetitors() {
        sailorProfile.getCompetitors().clear();
        sendUpdateToBackend();
    }

    public void addCompetitor(SimpleCompetitorWithIdDTO selectedItem) {
        sailorProfile.getCompetitors().add(selectedItem);
        sendUpdateToBackend();
    }

    public void getEvents(UUID key, AsyncCallback<Iterable<ParticipatedEventDTO>> asyncCallback) {
        sailorProfileDataProvider.getEvents(key, asyncCallback);
    }

    public void loadSailorProfiles(AsyncCallback<SailorProfilesDTO> callback) {
        sailorProfileDataProvider.loadSailorProfiles(callback);
    }

    public void createNewEntry(SailorProfileDTO newSailorProfile) {
        sailorProfile = newSailorProfile;
        sendUpdateToBackend();
    }

}

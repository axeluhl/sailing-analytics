package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.dataprovider;

import java.util.Collection;
import java.util.UUID;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfilesDTO;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithDispatch;
import com.sap.sailing.gwt.home.shared.partials.editable.EditableSuggestedMultiSelection.EditModeChangeHandler;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionCompetitorDataProvider;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionCompetitorDataProvider.Display;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionDataProvider;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.EditSailorProfileView;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.domain.ParticipatedEventDTO;

public class StatefulSailorProfileDataProvider
        implements SuggestedMultiSelectionDataProvider<SimpleCompetitorWithIdDTO, Display>, EditModeChangeHandler {

    private SailorProfileDataProvider sailorProfileDataProvider;
    private final SuggestedMultiSelectionCompetitorDataProvider competitorDataProvider;

    private SailorProfileDTO sailorProfile;

    private EditSailorProfileView sailorView;

    public StatefulSailorProfileDataProvider(ClientFactoryWithDispatch clientFactory,
            SuggestedMultiSelectionCompetitorDataProvider competitorDataProvider) {
        sailorProfileDataProvider = new SailorProfileDataProviderImpl(clientFactory);
        this.competitorDataProvider = competitorDataProvider;
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
        GWT.log("remove");
        sailorProfile.getCompetitors().remove(selectedItem);
        sendUpdateToBackend();

    }

    public void clearCompetitors() {
        GWT.log("clear");
        sailorProfile.getCompetitors().clear();
        sendUpdateToBackend();
    }

    public void addCompetitor(SimpleCompetitorWithIdDTO selectedItem) {
        GWT.log("add");
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

    @Override
    public Object getKey(SimpleCompetitorWithIdDTO item) {
        return competitorDataProvider.getKey(item);
    }

    @Override
    public void addSelection(SimpleCompetitorWithIdDTO item) {
        sailorProfile.getCompetitors().add(item);
    }

    @Override
    public void removeSelection(SimpleCompetitorWithIdDTO item) {
        sailorProfile.getCompetitors().remove(item);
    }

    @Override
    public void clearSelection() {
        sailorProfile.getCompetitors().clear();
    }

    @Override
    public void getSuggestionItems(Iterable<String> queryTokens, int limit,
            SuggestionItemsCallback<SimpleCompetitorWithIdDTO> callback) {
        competitorDataProvider.getSuggestionItems(queryTokens, limit, callback);
    }

    @Override
    public String createSuggestionKeyString(SimpleCompetitorWithIdDTO value) {
        return competitorDataProvider.createSuggestionKeyString(value);
    }

    @Override
    public void addDisplay(
            com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionCompetitorDataProvider.Display display) {
        competitorDataProvider.addDisplay(display);
    }

    @Override
    public void persist() {
    }

    @Override
    public void initSelectedItems(Collection<SimpleCompetitorWithIdDTO> selectedItems) {
    }

    @Override
    public String createSuggestionAdditionalDisplayString(SimpleCompetitorWithIdDTO value) {
        return competitorDataProvider.createSuggestionAdditionalDisplayString(value);
    }

    @Override
    public void onEditModeChanged(boolean edit) {
        if (!edit) {
            sendUpdateToBackend();
        }
    }

}

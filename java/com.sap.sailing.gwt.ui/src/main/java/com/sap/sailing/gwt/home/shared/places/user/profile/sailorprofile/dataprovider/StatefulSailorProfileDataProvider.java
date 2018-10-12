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
import com.sap.sailing.gwt.home.shared.partials.editable.EditableSuggestedMultiSelection.EditModeChangeHandler;
import com.sap.sailing.gwt.home.shared.partials.multiselection.AbstractSuggestedCompetitorMultiSelectionPresenter;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionPresenter;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.ClientFactoryWithDispatchAndError;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.EditSailorProfileView;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SailorProfilePlace;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;

public class StatefulSailorProfileDataProvider implements
        SuggestedMultiSelectionPresenter<SimpleCompetitorWithIdDTO, SuggestedMultiSelectionPresenter.Display<SimpleCompetitorWithIdDTO>>,
        EditModeChangeHandler {

    private final AbstractSuggestedCompetitorMultiSelectionPresenter<Display<SimpleCompetitorWithIdDTO>> competitorDataProvider;

    private EditSailorProfileView sailorView;

    private Collection<SimpleCompetitorWithIdDTO> competitors;
    private UUID uuid;
    private final ClientFactoryWithDispatchAndError clientFactory;

    public StatefulSailorProfileDataProvider(ClientFactoryWithDispatchAndError clientFactory,
            AbstractSuggestedCompetitorMultiSelectionPresenter<Display<SimpleCompetitorWithIdDTO>> competitorDataProvider) {
        this.clientFactory = clientFactory;
        this.competitorDataProvider = competitorDataProvider;
    }

    public void setView(EditSailorProfileView sailorView) {
        this.sailorView = sailorView;
    }

    public void loadSailorProfile(UUID uuid) {
        clientFactory.getDispatch().execute(new GetSailorProfileAction(uuid), createRefreshCallback(uuid));
    }

    public void updateTitle(String newTitle) {
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

    @Override
    public Object getKey(SimpleCompetitorWithIdDTO item) {
        return competitorDataProvider.getKey(item);
    }

    @Override
    public void addSelection(SimpleCompetitorWithIdDTO item) {
        competitors.add(item);
        updateCompetitors();
    }

    @Override
    public void removeSelection(SimpleCompetitorWithIdDTO item) {
        competitors.remove(item);
        updateCompetitors();
    }

    @Override
    public void clearSelection() {
        competitors.clear();
        updateCompetitors();
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
    public void addDisplay(SuggestedMultiSelectionPresenter.Display<SimpleCompetitorWithIdDTO> display) {
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
            updateCompetitors();
        }
    }

    public void removeSailorProfile(UUID uuid, AsyncCallback<SailorProfileDTO> callback) {
        clientFactory.getDispatch().execute(new RemoveSailorProfileAction(uuid), callback);
    }

    public void getStatisticFor(UUID uuid, SailorProfileNumericStatisticType type,
            AsyncCallback<SailorProfileStatisticDTO> callback) {
        clientFactory.getDispatch().execute(new GetNumericStatisticForSailorProfileAction(uuid, type), callback);
    }

    public void updateCompetitors() {
        clientFactory.getDispatch().execute(new UpdateSailorProfileCompetitorsAction(uuid, competitors),
                createRefreshCallback(uuid));
    }

    public AsyncCallback<SailorProfileDTO> createRefreshCallback(UUID uuid) {
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
                    competitors = result.getCompetitors();
                    StatefulSailorProfileDataProvider.this.uuid = result.getKey();
                    sailorView.setEntry(result);
                }
            }
        };
    }

}

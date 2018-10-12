package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.dataprovider;

import java.util.Collection;
import java.util.UUID;

import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.shared.partials.editable.EditableSuggestedMultiSelection.EditModeChangeHandler;
import com.sap.sailing.gwt.home.shared.partials.multiselection.AbstractSuggestedCompetitorMultiSelectionPresenter;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionPresenter;

public class SailorProfilesCompetitorSelectionPresenter implements EditModeChangeHandler,
        SuggestedMultiSelectionPresenter<SimpleCompetitorWithIdDTO, SuggestedMultiSelectionPresenter.Display<SimpleCompetitorWithIdDTO>> {

    private final AbstractSuggestedCompetitorMultiSelectionPresenter<Display<SimpleCompetitorWithIdDTO>> competitorDataProvider;

    private Collection<SimpleCompetitorWithIdDTO> competitors;
    private UUID uuid;

    private final SailorProfileDataProvider statefulSailorProfileDataProvider;

    public SailorProfilesCompetitorSelectionPresenter(
            AbstractSuggestedCompetitorMultiSelectionPresenter<Display<SimpleCompetitorWithIdDTO>> competitorDataProvider,
            SailorProfileDataProvider sailorProfileDataProvider) {
        this.competitorDataProvider = competitorDataProvider;
        this.statefulSailorProfileDataProvider = sailorProfileDataProvider;
    }

    public void setCompetitorsAndUUID(Collection<SimpleCompetitorWithIdDTO> competitors, UUID uuid) {
        this.competitors = competitors;
        this.uuid = uuid;
    }

    public Collection<SimpleCompetitorWithIdDTO> getCompetitors() {
        return competitors;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public void onEditModeChanged(boolean edit) {
        if (!edit) {
            statefulSailorProfileDataProvider.updateCompetitors(uuid, competitors, this);
        }
    }

    @Override
    public Object getKey(SimpleCompetitorWithIdDTO item) {
        return competitorDataProvider.getKey(item);
    }

    @Override
    public void addSelection(SimpleCompetitorWithIdDTO item) {
        competitors.add(item);
        statefulSailorProfileDataProvider.updateCompetitors(uuid, competitors, this);
    }

    @Override
    public void removeSelection(SimpleCompetitorWithIdDTO item) {
        competitors.remove(item);
        statefulSailorProfileDataProvider.updateCompetitors(uuid, competitors, this);
    }

    @Override
    public void clearSelection() {
        competitors.clear();
        statefulSailorProfileDataProvider.updateCompetitors(uuid, competitors, this);
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
}

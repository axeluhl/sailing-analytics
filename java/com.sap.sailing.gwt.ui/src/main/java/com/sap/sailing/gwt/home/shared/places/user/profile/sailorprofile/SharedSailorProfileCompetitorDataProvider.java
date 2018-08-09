package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile;

import java.util.Collection;

import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionCompetitorDataProvider;

public class SharedSailorProfileCompetitorDataProvider implements SuggestedMultiSelectionCompetitorDataProvider {

    private SuggestedMultiSelectionCompetitorDataProvider competitorDataProvider;

    public SharedSailorProfileCompetitorDataProvider(
            SuggestedMultiSelectionCompetitorDataProvider competitorDataProvider) {
        this.competitorDataProvider = competitorDataProvider;
    }

    @Override
    public Object getKey(SimpleCompetitorWithIdDTO item) {
        return competitorDataProvider.getKey(item);
    }

    @Override
    public void removeSelection(SimpleCompetitorWithIdDTO item) {
        // competitorDataProvider.removeSelection(item);

    }

    @Override
    public void persist() {
        // TODO Auto-generated method stub

    }

    @Override
    public void initSelectedItems(Collection<SimpleCompetitorWithIdDTO> selectedItems) {
        // competitorDataProvider.initSelectedItems(selectedItems);
    }

    @Override
    public void getSuggestionItems(Iterable<String> queryTokens, int limit,
            SuggestionItemsCallback<SimpleCompetitorWithIdDTO> callback) {
        competitorDataProvider.getSuggestionItems(queryTokens, limit, callback);
    }

    @Override
    public String createSuggestionKeyString(SimpleCompetitorWithIdDTO value) {
        // return "";
        return competitorDataProvider.createSuggestionKeyString(value);
    }

    @Override
    public String createSuggestionAdditionalDisplayString(SimpleCompetitorWithIdDTO value) {
        // return "";
        return competitorDataProvider.createSuggestionAdditionalDisplayString(value);
    }

    @Override
    public void clearSelection() {
        // competitorDataProvider.clearSelection();
    }

    @Override
    public void addSelection(SimpleCompetitorWithIdDTO item) {
        // competitorDataProvider.addSelection(item);
    }

    @Override
    public void addDisplay(Display display) {
        competitorDataProvider.addDisplay(display);
    }

    @Override
    public void setNotifyAboutResults(boolean notifyAboutResults) {
        // competitorDataProvider.setNotifyAboutResults(notifyAboutResults);
    }

    @Override
    public void initNotifications(boolean notifyAboutResults) {
        // competitorDataProvider.initNotifications(notifyAboutResults);
    }
}

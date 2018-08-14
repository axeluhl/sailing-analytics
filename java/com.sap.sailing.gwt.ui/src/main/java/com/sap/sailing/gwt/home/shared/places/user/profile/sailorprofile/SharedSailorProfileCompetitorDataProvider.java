package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile;

import java.util.Collection;

import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.shared.partials.multiselection.AbstractSuggestedMultiSelectionCompetitorDataProvider;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionCompetitorDataProvider;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionCompetitorDataProvider.Display;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionDataProvider;

public class SharedSailorProfileCompetitorDataProvider
        implements SuggestedMultiSelectionDataProvider<SimpleCompetitorWithIdDTO, Display> {

    private SuggestedMultiSelectionCompetitorDataProvider competitorDataProvider;

    public SharedSailorProfileCompetitorDataProvider(
            AbstractSuggestedMultiSelectionCompetitorDataProvider competitorDataProvider) {
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
        return competitorDataProvider.createSuggestionKeyString(value);
    }

    @Override
    public String createSuggestionAdditionalDisplayString(SimpleCompetitorWithIdDTO value) {
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
    public void addDisplay(
            com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionCompetitorDataProvider.Display display) {
        competitorDataProvider.addDisplay(display);
    }

}

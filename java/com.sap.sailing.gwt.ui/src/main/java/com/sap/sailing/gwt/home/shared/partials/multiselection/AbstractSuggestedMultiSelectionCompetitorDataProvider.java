package com.sap.sailing.gwt.home.shared.partials.multiselection;

import java.util.Collection;

import com.google.gwt.view.client.ProvidesKey;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.FavoriteCompetitorsDTO;

public abstract class AbstractSuggestedMultiSelectionCompetitorDataProvider extends
        AbstractSuggestedMultiSelectionDataProvider<SimpleCompetitorWithIdDTO, SuggestedMultiSelectionCompetitorDataProvider.Display>
        implements SuggestedMultiSelectionCompetitorDataProvider {

    private boolean notifyAboutResults;
    
    protected AbstractSuggestedMultiSelectionCompetitorDataProvider() {
        super(new ProvidesKey<SimpleCompetitorWithIdDTO>() {
            @Override
            public Object getKey(SimpleCompetitorWithIdDTO item) {
                return item.getIdAsString();
            }
        });
    }
    
    @Override
    public final String createSuggestionKeyString(SimpleCompetitorWithIdDTO value) {
        return value.getShortInfo();
    }
    
    @Override
    public String createSuggestionAdditionalDisplayString(SimpleCompetitorWithIdDTO value) {
        return value.getName();
    }
    
    @Override
    public void initNotifications(boolean notifyAboutResults) {
        this.notifyAboutResults = notifyAboutResults;
        for (SuggestedMultiSelectionCompetitorDataProvider.Display display : displays) {
            display.setNotifyAboutResults(notifyAboutResults);
        }
    }
    
    @Override
    public void setNotifyAboutResults(boolean notifyAboutResults) {
        this.notifyAboutResults = notifyAboutResults;
        this.persist();
    }
    
    @Override
    protected final void persist(Collection<SimpleCompetitorWithIdDTO> selectedItem) {
        this.persist(new FavoriteCompetitorsDTO(selectedItem, notifyAboutResults));
    }
    
    protected abstract void persist(FavoriteCompetitorsDTO favorites);

}

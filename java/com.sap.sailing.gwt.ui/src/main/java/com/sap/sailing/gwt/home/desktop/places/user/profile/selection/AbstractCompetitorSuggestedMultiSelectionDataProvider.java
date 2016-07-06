package com.sap.sailing.gwt.home.desktop.places.user.profile.selection;

import com.google.gwt.view.client.ProvidesKey;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;

public abstract class AbstractCompetitorSuggestedMultiSelectionDataProvider extends
        AbstractSuggestedMultiSelectionDataProvider<SimpleCompetitorWithIdDTO, CompetitorSuggestedMultiSelectionDataProvider.Display>
        implements CompetitorSuggestedMultiSelectionDataProvider {

    private boolean notifyAboutResults;
    
    protected AbstractCompetitorSuggestedMultiSelectionDataProvider() {
        super(new ProvidesKey<SimpleCompetitorWithIdDTO>() {
            @Override
            public Object getKey(SimpleCompetitorWithIdDTO item) {
                return item.getIdAsString();
            }
        });
    }
    
    @Override
    public void initNotifications(boolean notifyAboutResults) {
        this.notifyAboutResults = notifyAboutResults;
        if (display != null) display.setNotifyAboutResults(notifyAboutResults);
    }
    
    @Override
    public void setNotifyAboutResults(boolean notifyAboutResults) {
        this.notifyAboutResults = notifyAboutResults;
        this.persist();
    }
    
    @Override
    public boolean isNotifyAboutResults() {
        return notifyAboutResults;
    }

}

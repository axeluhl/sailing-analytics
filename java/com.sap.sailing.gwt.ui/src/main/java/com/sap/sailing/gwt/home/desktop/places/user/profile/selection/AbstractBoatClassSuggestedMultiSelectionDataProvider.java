package com.sap.sailing.gwt.home.desktop.places.user.profile.selection;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.view.client.ProvidesKey;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.AbstractListFilter;

public abstract class AbstractBoatClassSuggestedMultiSelectionDataProvider extends
        AbstractSuggestedMultiSelectionDataProvider<BoatClassMasterdata, BoatClassSuggestedMultiSelectionDataProvider.Display>
        implements BoatClassSuggestedMultiSelectionDataProvider {

    private boolean notifyAboutUpcomingRaces;
    private boolean notifyAboutResults;
    
    private final AbstractListFilter<BoatClassMasterdata> filter = new AbstractListFilter<BoatClassMasterdata>() {
        @Override
        public Iterable<String> getStrings(BoatClassMasterdata item) {
            return item.getBoatClassNames();
        }
    };
    
    protected AbstractBoatClassSuggestedMultiSelectionDataProvider() {
        super(new ProvidesKey<BoatClassMasterdata>() {
            @Override
            public Object getKey(BoatClassMasterdata item) {
                return item.getDisplayName();
            }
        });
    }
    
    @Override
    protected void getSuggestions(Iterable<String> queryTokens, int limit,
            SuggestionItemsCallback<BoatClassMasterdata> callback) {
        List<BoatClassMasterdata> boatClasses = Arrays.asList(BoatClassMasterdata.values());
        callback.setSuggestionItems(Util.asList(filter.applyFilter(queryTokens, boatClasses)));
    }
    
    @Override
    public void initNotifications(boolean notifyAboutUpcomingRaces, boolean notifyAboutResults) {
        this.notifyAboutUpcomingRaces = notifyAboutUpcomingRaces;
        this.notifyAboutResults = notifyAboutResults;
        if (display != null) {
            display.setNotifyAboutUpcomingRaces(notifyAboutUpcomingRaces);
            display.setNotifyAboutResults(notifyAboutResults);
        }
    }
    
    @Override
    public boolean isNotifyAboutUpcomingRaces() {
        return notifyAboutUpcomingRaces;
    }
    
    @Override
    public void setNotifyAboutUpcomingRaces(boolean notifyAboutUpcomingRaces) {
        this.notifyAboutUpcomingRaces = notifyAboutUpcomingRaces;
        this.persist();
    }
    
    @Override
    public boolean isNotifyAboutResults() {
        return notifyAboutResults;
    }
    
    @Override
    public void setNotifyAboutResults(boolean notifyAboutResults) {
        this.notifyAboutResults = notifyAboutResults;
        this.persist();
    }
    
}

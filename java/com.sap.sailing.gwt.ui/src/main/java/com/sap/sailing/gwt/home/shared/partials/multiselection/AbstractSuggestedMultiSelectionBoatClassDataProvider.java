package com.sap.sailing.gwt.home.shared.partials.multiselection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.gwt.view.client.ProvidesKey;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.gwt.home.communication.user.profile.FavoriteBoatClassesDTO;
import com.sap.sse.common.filter.AbstractListFilter;

public abstract class AbstractSuggestedMultiSelectionBoatClassDataProvider extends
        AbstractSuggestedMultiSelectionDataProvider<BoatClassDTO, SuggestedMultiSelectionBoatClassDataProvider.Display>
        implements SuggestedMultiSelectionBoatClassDataProvider {

    private boolean notifyAboutUpcomingRaces;
    private boolean notifyAboutResults;
    
    private final AbstractListFilter<BoatClassMasterdata> filter = new AbstractListFilter<BoatClassMasterdata>() {
        @Override
        public Iterable<String> getStrings(BoatClassMasterdata item) {
            return item.getBoatClassNames();
        }
    };
    
    protected AbstractSuggestedMultiSelectionBoatClassDataProvider() {
        super(new ProvidesKey<BoatClassDTO>() {
            @Override
            public Object getKey(BoatClassDTO item) {
                return item.getName();
            }
        });
    }
    
    @Override
    protected void getSuggestions(Iterable<String> queryTokens, int limit,
            SuggestionItemsCallback<BoatClassDTO> callback) {
        List<BoatClassMasterdata> boatClasses = Arrays.asList(BoatClassMasterdata.values());
        List<BoatClassDTO> suggestionItems = new ArrayList<>();
        for (BoatClassMasterdata bcm : filter.applyFilter(queryTokens, boatClasses)) {
            suggestionItems.add(new BoatClassDTO(bcm.getDisplayName(), bcm.getHullLength(), bcm.getHullBeam()));
        }
        callback.setSuggestionItems(suggestionItems);
    }
    
    @Override
    public final String createSuggestionKeyString(BoatClassDTO value) {
        return value.getName();
    }
    
    @Override
    public final String createSuggestionAdditionalDisplayString(BoatClassDTO value) {
        return null;
    }
    
    @Override
    public void initNotifications(boolean notifyAboutUpcomingRaces, boolean notifyAboutResults) {
        this.notifyAboutUpcomingRaces = notifyAboutUpcomingRaces;
        this.notifyAboutResults = notifyAboutResults;
        for (SuggestedMultiSelectionBoatClassDataProvider.Display display : displays) {
            display.setNotifyAboutUpcomingRaces(notifyAboutUpcomingRaces);
            display.setNotifyAboutResults(notifyAboutResults);
        }
    }
    
    @Override
    public void setNotifyAboutUpcomingRaces(boolean notifyAboutUpcomingRaces) {
        this.notifyAboutUpcomingRaces = notifyAboutUpcomingRaces;
        this.persist();
    }
    
    @Override
    public void setNotifyAboutResults(boolean notifyAboutResults) {
        this.notifyAboutResults = notifyAboutResults;
        this.persist();
    }
    
    @Override
    protected final void persist(Collection<BoatClassDTO> selectedItem) {
        this.persist(new FavoriteBoatClassesDTO(selectedItem, notifyAboutUpcomingRaces, notifyAboutResults));
    }
    
    protected abstract void persist(FavoriteBoatClassesDTO favorites);
    
}

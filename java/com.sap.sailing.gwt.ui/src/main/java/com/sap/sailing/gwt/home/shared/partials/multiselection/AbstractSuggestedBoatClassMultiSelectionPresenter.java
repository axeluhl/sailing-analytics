package com.sap.sailing.gwt.home.shared.partials.multiselection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sse.common.filter.AbstractListFilter;

public abstract class AbstractSuggestedBoatClassMultiSelectionPresenter<D extends SuggestedMultiSelectionPresenter.Display<BoatClassDTO>>
        extends AbstractSuggestedMultiSelectionPresenter<BoatClassDTO, D>
        implements SuggestedMultiSelectionPresenter<BoatClassDTO, D> {

    private final AbstractListFilter<BoatClassMasterdata> filter = new AbstractListFilter<BoatClassMasterdata>() {
        @Override
        public Iterable<String> getStrings(BoatClassMasterdata item) {
            return item.getBoatClassNames();
        }
    };
    
    protected AbstractSuggestedBoatClassMultiSelectionPresenter() {
        super(BoatClassDTO::getName);
    }
    
    @Override
    protected final void getSuggestions(final Iterable<String> queryTokens, final int limit,
            final SuggestionItemsCallback<BoatClassDTO> callback) {
        final List<BoatClassMasterdata> boatClasses = Arrays.asList(BoatClassMasterdata.values());
        final List<BoatClassDTO> suggestionItems = new ArrayList<>();
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
    
}

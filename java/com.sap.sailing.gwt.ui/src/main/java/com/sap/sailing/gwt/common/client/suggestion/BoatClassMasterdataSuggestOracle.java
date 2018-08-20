package com.sap.sailing.gwt.common.client.suggestion;

import java.util.Arrays;

import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sse.gwt.client.suggestion.AbstractListSuggestOracle;

public class BoatClassMasterdataSuggestOracle extends AbstractListSuggestOracle<BoatClassMasterdata> {
    
    public BoatClassMasterdataSuggestOracle() {
        this.setSelectableValues(Arrays.asList(BoatClassMasterdata.values()));
    }

    @Override
    protected String createSuggestionKeyString(BoatClassMasterdata value) {
        return value.getDisplayName();
    }

    @Override
    protected String createSuggestionAdditionalDisplayString(BoatClassMasterdata value) {
        return null;
    }

    @Override
    protected Iterable<String> getMatchingStrings(BoatClassMasterdata value) {
        return value.getBoatClassNames();
    }

}

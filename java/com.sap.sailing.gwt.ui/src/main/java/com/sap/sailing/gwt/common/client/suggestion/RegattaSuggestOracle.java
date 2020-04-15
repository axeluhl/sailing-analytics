package com.sap.sailing.gwt.common.client.suggestion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.gwt.client.suggestion.AbstractListSuggestOracle;

public class RegattaSuggestOracle extends AbstractListSuggestOracle<RegattaDTO> implements RegattasDisplayer {
    public RegattaSuggestOracle() {
    }

    @Override
    protected String createSuggestionKeyString(RegattaDTO value) {
        return value.getName();
    }

    @Override
    protected String createSuggestionAdditionalDisplayString(RegattaDTO value) {
        return value.boatClass.getName();
    }

    @Override
    protected Iterable<String> getMatchingStrings(RegattaDTO value) {
        return Arrays.asList(value.getName(), value.boatClass.getName());
    }

    @Override
    public void fillRegattas(Iterable<RegattaDTO> result) {
        final List<RegattaDTO> sorted = new ArrayList<>();
        final NaturalComparator nameComparator = new NaturalComparator(/* case sensitive */ false);
        Util.addAll(result, sorted);
        Collections.sort(sorted, (r1, r2)->nameComparator.compare(r1.getName(), r2.getName()));
        setSelectableValues(sorted);
    }
}

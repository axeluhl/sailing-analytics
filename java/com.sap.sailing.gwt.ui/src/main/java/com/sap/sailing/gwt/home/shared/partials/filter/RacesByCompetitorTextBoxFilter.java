package com.sap.sailing.gwt.home.shared.partials.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sap.sailing.gwt.ui.shared.dispatch.event.SimpleCompetitorDTO;
import com.sap.sailing.gwt.ui.shared.race.SimpleRaceMetadataDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.AbstractListFilter;
import com.sap.sse.common.filter.Filter;

public class RacesByCompetitorTextBoxFilter extends AbstractTextBoxFilter<SimpleRaceMetadataDTO> {
    
    private final RacesByCompetitorFilter filter = new RacesByCompetitorFilter();
    
    @Override
    protected Filter<SimpleRaceMetadataDTO> getFilter(String searchValue) {
        this.filter.keywords.clear();
        if (searchValue != null && !searchValue.isEmpty()) {
            this.filter.keywords.add(searchValue);
        }
        return filter;
    }
    
    private class RacesByCompetitorFilter implements Filter<SimpleRaceMetadataDTO> {

        private final List<String> keywords = new ArrayList<>();
        private final AbstractListFilter<SimpleCompetitorDTO> listFilter = new AbstractListFilter<SimpleCompetitorDTO>() {
            @Override
            public Iterable<String> getStrings(SimpleCompetitorDTO t) {
                return Arrays.asList(t.getName(), t.getSailID());
            }
        };
        
        @Override
        public boolean matches(SimpleRaceMetadataDTO object) {
            return keywords.isEmpty() || !Util.isEmpty(listFilter.applyFilter(keywords, object.getCompetitors()));
        }

        @Override
        public String getName() {
            return getClass().getSimpleName();
        }
        
    }

}

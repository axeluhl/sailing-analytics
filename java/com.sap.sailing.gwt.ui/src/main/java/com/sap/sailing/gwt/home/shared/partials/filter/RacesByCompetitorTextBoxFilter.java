package com.sap.sailing.gwt.home.shared.partials.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorDTO;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.AbstractListFilter;
import com.sap.sse.common.filter.Filter;

public class RacesByCompetitorTextBoxFilter extends AbstractSuggestBoxFilter<SimpleRaceMetadataDTO, SimpleCompetitorDTO> {
    
    private final RacesByCompetitorFilter filter = new RacesByCompetitorFilter();
    
    public RacesByCompetitorTextBoxFilter() {
        super(StringMessages.INSTANCE.competitorsFilter(), " -/");
    }
    
    @Override
    protected Filter<SimpleRaceMetadataDTO> getFilter(String searchValue) {
        this.filter.keywords.clear();
        if (searchValue != null && !searchValue.isEmpty()) {
            this.filter.keywords.add(searchValue);
        }
        return filter;
    }
    
    @Override
    protected String createSuggestionString(SimpleCompetitorDTO value) {
        return value.getSailID() + " - " + value.getName();
    }
    
    private class RacesByCompetitorFilter implements Filter<SimpleRaceMetadataDTO> {

        private final List<String> keywords = new ArrayList<>();
        private final AbstractListFilter<SimpleCompetitorDTO> listFilter = new AbstractListFilter<SimpleCompetitorDTO>() {
            @Override
            public Iterable<String> getStrings(SimpleCompetitorDTO t) {
                return Arrays.asList(t.getName(), t.getSailID(), createSuggestionString(t));
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

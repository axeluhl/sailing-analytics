package com.sap.sailing.gwt.home.shared.partials.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorDTO;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.Filter;

public class RacesByCompetitorTextBoxFilter extends AbstractSuggestBoxFilter<SimpleRaceMetadataDTO, SimpleCompetitorDTO> {
    
    private final RacesByCompetitorFilter filter = new RacesByCompetitorFilter();
    
    public RacesByCompetitorTextBoxFilter() {
        super(StringMessages.INSTANCE.competitorsFilter());
    }
    
    @Override
    protected Filter<SimpleRaceMetadataDTO> getFilter(String searchValue) {
        this.filter.keywords.clear();
        if (searchValue != null && !searchValue.isEmpty()) {
            this.filter.keywords.add(searchValue.trim());
        }
        return filter;
    }
    
    @Override
    protected String createSuggestionDisplayString(SimpleCompetitorDTO value) {
        return value.getName();
    }
    
    @Override
    protected String createSuggestionReplacementString(SimpleCompetitorDTO value) {
        return value.getSailID();
    }
    
    @Override
    protected Iterable<String> getMatchingStrings(SimpleCompetitorDTO value) {
        return Arrays.asList(value.getName(), value.getSailID());
    }
    
    private class RacesByCompetitorFilter implements Filter<SimpleRaceMetadataDTO> {

        private final List<String> keywords = new ArrayList<>();
        
        @Override
        public boolean matches(SimpleRaceMetadataDTO object) {
            return keywords.isEmpty() || !Util.isEmpty(suggestionMatchingFilter.applyFilter(keywords, object.getCompetitors()));
        }

        @Override
        public String getName() {
            return getClass().getSimpleName();
        }
        
    }

}

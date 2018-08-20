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
import com.sap.sse.gwt.client.suggestion.AbstractListSuggestOracle;

public class RacesByCompetitorTextBoxFilter extends AbstractListSuggestBoxFilter<SimpleRaceMetadataDTO, SimpleCompetitorDTO> {
    
    private final RacesByCompetitorFilter filter = new RacesByCompetitorFilter();
    
    public RacesByCompetitorTextBoxFilter() {
        super(new AbstractListSuggestOracle<SimpleCompetitorDTO>() {
            @Override
            protected Iterable<String> getMatchingStrings(SimpleCompetitorDTO value) {
                return Arrays.asList(value.getName(), value.getShortInfo());
            }

            @Override
            protected String createSuggestionKeyString(SimpleCompetitorDTO value) {
                return value.getShortInfo();
            }

            @Override
            protected String createSuggestionAdditionalDisplayString(SimpleCompetitorDTO value) {
                return value.getName();
            }
        }, StringMessages.INSTANCE.competitorsFilter());
    }
    
    @Override
    protected Filter<SimpleRaceMetadataDTO> getFilter(String searchValue) {
        this.filter.keywords.clear();
        if (searchValue != null && !searchValue.isEmpty()) {
            Util.addAll(Util.splitAlongWhitespaceRespectingDoubleQuotedPhrases(searchValue), this.filter.keywords);
        }
        return filter;
    }
    
    @Override
    protected void onSuggestionSelected(SimpleCompetitorDTO selectedItem) {
    }
    
    private class RacesByCompetitorFilter implements Filter<SimpleRaceMetadataDTO> {

        private final List<String> keywords = new ArrayList<>();
        
        @Override
        public boolean matches(SimpleRaceMetadataDTO object) {
            AbstractListFilter<SimpleCompetitorDTO> filter = getSuggestOracle().getSuggestionMatchingFilter();
            return keywords.isEmpty() || !Util.isEmpty(filter.applyFilter(keywords, object.getCompetitors()));
        }

        @Override
        public String getName() {
            return getClass().getSimpleName();
        }
        
    }

}

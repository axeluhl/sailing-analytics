package com.sap.sailing.gwt.home.communication.user.profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.AbstractListFilter;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

public class GetCompetitorSuggestionAction implements SailingAction<CompetitorSuggestionResult> {
    
    private String query;
    private int limit;
    
    @GwtIncompatible
    private final AbstractListFilter<Competitor> competitorFilter = new AbstractListFilter<Competitor>() {
        @Override
        public Iterable<String> getStrings(Competitor competitor) {
            if (competitor.getBoat() == null) return Arrays.asList(competitor.getName());
            return Arrays.asList(competitor.getBoat().getSailID(), competitor.getName());
        }
    };
    
    @SuppressWarnings("unused")
    private GetCompetitorSuggestionAction() {
    }

    public GetCompetitorSuggestionAction(String query, int limit) {
        this.query = query;
        this.limit = limit;
    }

    @Override
    @GwtIncompatible
    public CompetitorSuggestionResult execute(SailingDispatchContext ctx) throws DispatchException {
        Iterable<Competitor> filteredCompetitors = getFilteredCompetitors(ctx);
        Collection<SimpleCompetitorDTO> result = convertToSimpleCompetitorDTOs(filteredCompetitors);
        return new CompetitorSuggestionResult(result, Math.max(0, Util.size(filteredCompetitors) - limit));
    }
    
    @GwtIncompatible
    private Iterable<Competitor> getFilteredCompetitors(SailingDispatchContext ctx) {
        Iterable<? extends Competitor> allCompetitors = ctx.getRacingEventService().getCompetitorStore().getCompetitors();
        Set<String> normalizedQuery = Collections.singleton(query.trim());
        return competitorFilter.applyFilter(normalizedQuery, Util.addAll(allCompetitors, new ArrayList<Competitor>())); 
    }
    
    @GwtIncompatible
    private Collection<SimpleCompetitorDTO> convertToSimpleCompetitorDTOs(Iterable<Competitor> filteredCompetitors) {
        Collection<SimpleCompetitorDTO> result = new ArrayList<>();
        int count = 0;
        for (Competitor competitor : filteredCompetitors) {
            result.add(new SimpleCompetitorDTO(competitor));
            if(++count >= limit) break;
        }
        return result;
    }

}

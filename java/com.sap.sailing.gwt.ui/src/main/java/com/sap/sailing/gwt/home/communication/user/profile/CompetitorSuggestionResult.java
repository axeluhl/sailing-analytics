package com.sap.sailing.gwt.home.communication.user.profile;

import java.util.Collection;

import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorDTO;
import com.sap.sse.gwt.dispatch.shared.commands.SortedSetResult;

public class CompetitorSuggestionResult extends SortedSetResult<SimpleCompetitorDTO> {

    private int moreSuggestionsCount;
    
    protected CompetitorSuggestionResult() {
    }
    
    public CompetitorSuggestionResult(Collection<SimpleCompetitorDTO> values, int moreSuggestionsCount) {
        super(values);
        this.moreSuggestionsCount = moreSuggestionsCount;
    }

    public int getMoreSuggestionsCount() {
        return moreSuggestionsCount;
    }
    
    public boolean hasMoreSuggestions() {
        return moreSuggestionsCount > 0;
    }
}

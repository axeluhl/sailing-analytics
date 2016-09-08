package com.sap.sailing.gwt.home.communication.user.profile;

import java.util.Collection;

import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sse.gwt.dispatch.shared.commands.SortedSetResult;

/**
 * {@link SortedSetResult} extension for suggested competitor search. This result contains a set of
 * {@link SimpleCompetitorWithIdDTO competitor}s representing the suggestions, as well as information if there are more
 * suggestions possible.
 */
public class CompetitorSuggestionResult extends SortedSetResult<SimpleCompetitorWithIdDTO> {

    private int moreSuggestionsCount;
    
    protected CompetitorSuggestionResult() {
    }
    
    public CompetitorSuggestionResult(Collection<SimpleCompetitorWithIdDTO> values, int moreSuggestionsCount) {
        super(values);
        this.moreSuggestionsCount = moreSuggestionsCount;
    }

    /**
     * @return the amount of additionally possible suggestions.
     */
    public int getMoreSuggestionsCount() {
        return moreSuggestionsCount;
    }
    
    /**
     * @return <code>true</code> if there is at least one {@link #getMoreSuggestionsCount() additional suggestion
     *         possible}, <code>false</code> otherwise
     */
    public boolean hasMoreSuggestions() {
        return moreSuggestionsCount > 0;
    }
}

package com.sap.sailing.gwt.ui.client.shared.filter;

import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.filter.AbstractTextFilter;

/**
 * A filter filtering tags by their tag attribute
 * 
 * @author Julian Rendl(D067890)
 * 
 */
public class TagTagFilter extends AbstractTextFilter<TagDTO> implements FilterWithUI<TagDTO> {
    public static final String FILTER_NAME = "Tag";

    private LeaderboardFetcher leaderboardFetcher;
    private RaceIdentifier selectedRace;
    

    public TagTagFilter() {
    }

    private LeaderboardDTO getLeaderboard() {
        return leaderboardFetcher != null ? leaderboardFetcher.getLeaderboard() : null;
    }

    @Override
    public boolean matches(TagDTO tag) {
        if(value != null && operator != null) {
            switch (operator.getOperator()) {
            case Contains:
                if(tag.getTag().contains(value)) {
                    return true;       
                }
            case Equals:
                if(tag.getTag().equals(value)) {
                    return true;       
                }
            case NotContains:
                if(!tag.getTag().contains(value)) {
                    return true;       
                }
            case NotEqualTo:
                if(!tag.getTag().equals(value)) {
                    return true;       
                }
            case EndsWith:
                if(tag.getTag().endsWith(value)) {
                    return true;       
                }
            case StartsWith:
                if(tag.getTag().startsWith(value)) {
                    return true;       
                }
            default:    
                throw new RuntimeException("Operator " + operator.getOperator().name() + " is not supported."); 
            }
        }
        return false;
    }

    /**
     * @return <code>null</code> if no rank could be determined for <code>TagDTO</code>, a 1-based rank otherwise
     */

    @Override
    public TagTagFilter copy() {
        TagTagFilter result = new TagTagFilter();
        result.setValue(getValue());
        result.setOperator(getOperator());
        return result;
    }

    @Override
    public String getName() {
        if (this.getOperator() != null) {
            return FILTER_NAME + " " + this.getOperator().getName() + " " + this.getValue();
        }
        return FILTER_NAME + " " + this.getValue();
    }

    @Override
    public String getLocalizedName(StringMessages stringMessages) {
        return stringMessages.raceRank();
    }

    @Override
    public String getLocalizedDescription(StringMessages stringMessages) {
        return "Top " + this.getValue() + " " + stringMessages.raceRank();
    }

    @Override
    public String validate(StringMessages stringMessages) {
        String errorMessage = null;
        if(value == null) {
            errorMessage = stringMessages.pleaseEnterAValue();
        } 
        return errorMessage;
    }

    @Override
    public FilterUIFactory<TagDTO> createUIFactory() {
        return new TagTagFilterUIFactory(this);
    }
}

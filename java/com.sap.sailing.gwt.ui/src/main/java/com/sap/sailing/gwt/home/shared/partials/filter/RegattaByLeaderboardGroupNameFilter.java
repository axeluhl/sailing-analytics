package com.sap.sailing.gwt.home.shared.partials.filter;

import com.sap.sailing.gwt.home.communication.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.Filter;

public class RegattaByLeaderboardGroupNameFilter extends AbstractSelectionFilter<RegattaMetadataDTO, String> {
    
    @Override
    public Filter<RegattaMetadataDTO> getFilter() {
        final String selectedLeaderboardGroupName = getSelectedValue();
        return new Filter<RegattaMetadataDTO>() {
            @Override
            public boolean matches(RegattaMetadataDTO object) {
                return selectedLeaderboardGroupName == null || Util.contains(object.getLeaderboardGroupNames(), selectedLeaderboardGroupName);
            }

            @Override
            public String getName() {
                return getClass().getName();
            }
        };
    }

    /**
     * The method is not used because this class overrides the {@link #getFilter()} method where
     * it obtains all leaderboard group names from the {@link RegattaMetadataDTO} instead of
     * requesting only a single string.
     */
    @Override
    protected String getFilterCriteria(RegattaMetadataDTO object) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected String getFilterItemLabel(String item) {
        return item == null ? StringMessages.INSTANCE.all() : item;
    }
    
}

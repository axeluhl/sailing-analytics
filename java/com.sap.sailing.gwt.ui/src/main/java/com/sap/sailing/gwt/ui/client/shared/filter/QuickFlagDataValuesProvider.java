package com.sap.sailing.gwt.ui.client.shared.filter;

import com.sap.sailing.domain.common.dto.CompetitorDTO;

/**
 * Can provide a race flag data for a {@link CompetitorDTO} quickly.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface QuickFlagDataValuesProvider {
    /**
     * @return the 1-based rank of the <code>competitor</code> if found, or <code>null</code> otherwise
     */
    Integer getRank(CompetitorDTO competitor);

    /**
     * 
     * @param the
     *            {@link CompetitorDTO} for which speed values is provided
     * @return the speed of the <code>competitor</code if found, or <code>null</code> otherwise
     */
    Double getSpeed(CompetitorDTO competitor);
}

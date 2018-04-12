package com.sap.sailing.gwt.ui.client.shared.filter;

import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;

/**
 * Can provide a race rank for a {@link CompetitorWithBoatDTO} quickly.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface QuickRankProvider {
    /**
     * @return the 1-based rank of the <code>competitor</code> if found, or <code>null</code> otherwise
     */
    Integer getRank(CompetitorWithBoatDTO competitor);
}

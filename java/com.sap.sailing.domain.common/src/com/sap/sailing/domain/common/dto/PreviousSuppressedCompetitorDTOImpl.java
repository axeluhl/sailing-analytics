package com.sap.sailing.domain.common.dto;

import com.sap.sse.common.Util;

/**
 * Like the superclass, but competitors are fetched from the {@link LeaderboardDTO#getSuppressedCompetitors()} list
 * instead of the {@link LeaderboardDTO#competitors} list where suppressed competitors don't show.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class PreviousSuppressedCompetitorDTOImpl extends PreviousCompetitorDTOImpl {
    private static final long serialVersionUID = -4879238928633525593L;

    @Deprecated
    PreviousSuppressedCompetitorDTOImpl() {
        super();
    }

    public PreviousSuppressedCompetitorDTOImpl(int indexInPreviousCompetitorList) {
        super(indexInPreviousCompetitorList);
    }

    @Override
    public CompetitorDTO getCompetitorFromPrevious(LeaderboardDTO previousVersion) {
        return Util.get(previousVersion.getSuppressedCompetitors(), getIndexInPreviousCompetitorList());
    }
}

package com.sap.sailing.domain.common.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * Allows for incremental serialization of a {@link LeaderboardDTO} by providing an implementation that refers to a previous
 * version and provides only updates, or alternatively sending the full leaderboard.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface IncrementalOrFullLeaderboardDTO extends Serializable, CachableRPCResult {
    /**
     * Produces the leaderboard DTO by providing the previous version relative to which a delta was requested.
     * If <code>null</code> was used to identify the previous version, <code>null</code> may be passed for
     * <code>previousVersion</code> here as well because a full leaderboard will have been transmitted anyhow.
     * Passing a <code>null</code> value when a non-<code>null</code> previous leaderboard identifier was used
     * to request this object will result in an {@link IllegalArgumentException}.
     */
    LeaderboardDTO getLeaderboardDTO(LeaderboardDTO previousVersion);

    /**
     * Time when the server sent out this object, in the server clock's time. 
     */
    Date getCurrentServerTime();
}

package com.sap.sailing.gwt.ui.leaderboard;

import java.util.Set;

import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.settings.client.leaderboard.RaceColumnSelectionStrategies;

/**
 * Represents a set of race columns that are currently to be shown in a leaderboard. Implementations may, e.g.,
 * be based on a more or less fixed but editable set of column names, or it may be a dynamically-computed selection
 * based on a timer and the set of races going on at or around that time.
 *  
 * @author Axel Uhl (D043530)
 *
 */
public interface RaceColumnSelection {
    Iterable<String> getSelectedRaceColumnNames();
    
    Iterable<RaceColumnDTO> getSelectedRaceColumns();
    
    /**
     * Short for {@link #getSelectedRaceColumnNames()}.{@link Set#contains(Object) contains(raceColumnName)}
     */
    boolean isSelected(String raceColumnName);
    
    /**
     * A client may request a column to be selected. An implementation doesn't necessarily have to comply with this
     * request, particularly if it represents an implicit, automatic column selection.
     */
    void requestRaceColumnSelection(RaceColumnDTO column);
    
    /**
     * A client may request all columns to be de-selected. An implementation doesn't necessarily have to comply with this
     * request, particularly if it represents an implicit, automatic column selection.
     */
    void requestClear();

    /**
     * Returns the same set of race columns as {@link #getSelectedRaceColumns()}, ordered in the same way they
     * are ordered in {@link LeaderboardDTO#getRaceList()}
     */
    Iterable<RaceColumnDTO> getSelectedRaceColumnsOrderedAsInLeaderboard(LeaderboardDTO leaderboard);

    /**
     * Tells the column selection that a new, probably updated version of the leaderboard is available. The race column
     * selection shall take this as an opportunity to make adjustments to the selection that are to happen automatically. 
     */
    void autoUpdateRaceColumnSelectionForUpdatedLeaderboard(LeaderboardDTO oldLeaderboard, LeaderboardDTO newLeaderboard);
    
    RaceColumnSelectionStrategies getType();
    
    Integer getNumberOfLastRaceColumnsToShow();
}

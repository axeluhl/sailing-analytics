package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;


/**
 * Allows UI components to observe a race selector, such as a drop-down box showing a list of races
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface RaceSelectionChangeListener {

    /**
     * The first element is the first one selected
     * 
     * @param selectedRaces a non-<code>null</code> list which is empty if nothing is selected
     */
    void onRaceSelectionChange(List<RegattaAndRaceIdentifier> selectedRaces);

}

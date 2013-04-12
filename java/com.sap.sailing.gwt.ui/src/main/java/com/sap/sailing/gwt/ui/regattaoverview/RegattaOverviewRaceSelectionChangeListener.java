package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.List;

import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;

public interface RegattaOverviewRaceSelectionChangeListener {
    /**
     * The first element is the first one selected
     * 
     * @param selectedRegattaOverviewEntries a non-<code>null</code> list which is empty if nothing is selected
     */
    void onRegattaOverviewEntrySelectionChange(List<RegattaOverviewEntryDTO> selectedRegattaOverviewEntries);
}

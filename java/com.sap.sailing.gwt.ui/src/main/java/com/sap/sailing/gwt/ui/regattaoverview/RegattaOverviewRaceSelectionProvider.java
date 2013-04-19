package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.List;

import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;

public interface RegattaOverviewRaceSelectionProvider {
    /**
     * @return a non-<code>null</code> list which may be empty
     */
    List<RegattaOverviewEntryDTO> getSelectedEntries();

    void addRegattaOverviewRaceSelectionChangeListener(RegattaOverviewRaceSelectionChangeListener listener);

    void removeRegattaOverviewRaceSelectionChangeListener(RegattaOverviewRaceSelectionChangeListener listener);
    
    void setSelection(List<RegattaOverviewEntryDTO> newSelection, RegattaOverviewRaceSelectionChangeListener... listenersNotToNotify);

    boolean hasMultiSelection();

    /**
     * @return a non-<code>null</code> list which may be empty
     */
    List<RegattaOverviewEntryDTO> getAllRegattaOverviewEntries();
    
    void setAllRegattaOverviewEntries(List<RegattaOverviewEntryDTO> newAllRegattaOverviewEntries, RegattaOverviewRaceSelectionChangeListener... listenersNotToNotify);
}

package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RaceDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public interface RaceSelectionProvider {
    /**
     * @return a non-<code>null</code> list which may be empty
     */
    List<Triple<EventDTO, RegattaDTO, RaceDTO>> getSelectedEventAndRace();

    void addRaceSelectionChangeListener(RaceSelectionChangeListener listener);

    void removeRaceSelectionChangeListener(RaceSelectionChangeListener listener);
}

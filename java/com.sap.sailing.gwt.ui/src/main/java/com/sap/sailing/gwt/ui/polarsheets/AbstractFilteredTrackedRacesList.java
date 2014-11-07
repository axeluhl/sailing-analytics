package com.sap.sailing.gwt.ui.polarsheets;

import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.ui.adminconsole.AbstractTrackedRacesListComposite;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;

public abstract class AbstractFilteredTrackedRacesList extends AbstractTrackedRacesListComposite {
    
    private RaceFilter filter;

    public AbstractFilteredTrackedRacesList(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            RegattaRefresher regattaRefresher, RaceSelectionProvider raceSelectionProvider,
            StringMessages stringMessages, boolean hasMultiSelection, RaceFilter filter) {
        super(sailingService, errorReporter, regattaRefresher, raceSelectionProvider, stringMessages, hasMultiSelection);
        this.filter = filter;
    }
    
    @Override
    protected boolean raceIsToBeAddedToList(RaceDTO race) {
        if (filter!=null) {
            return filter.compliesToFilter(race);
        }
        return true;
    }
  

}

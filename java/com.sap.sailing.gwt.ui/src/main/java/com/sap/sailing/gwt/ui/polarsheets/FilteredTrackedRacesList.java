package com.sap.sailing.gwt.ui.polarsheets;

import com.sap.sailing.gwt.ui.adminconsole.TrackedRacesListComposite;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceDTO;

public class FilteredTrackedRacesList extends TrackedRacesListComposite {
    
    private RaceFilter filter;

    public FilteredTrackedRacesList(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            RegattaRefresher regattaRefresher, RaceSelectionProvider raceSelectionProvider,
            StringMessages stringMessages, boolean hasMultiSelection, RaceFilter filter) {
        super(sailingService, errorReporter, regattaRefresher, raceSelectionProvider, stringMessages, hasMultiSelection);
        this.filter = filter;
    }
    
    @Override
    protected boolean raceCompliesToFilter(RaceDTO race) {
        if (filter!=null) {
            return filter.compliesToFilter(race);
        }
        return true;
    }

  

}

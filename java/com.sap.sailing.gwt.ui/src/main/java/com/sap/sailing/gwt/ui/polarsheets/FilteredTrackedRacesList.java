package com.sap.sailing.gwt.ui.polarsheets;

import java.util.List;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.sap.sailing.gwt.ui.adminconsole.AbstractTrackedRacesListComposite;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class FilteredTrackedRacesList extends AbstractTrackedRacesListComposite {
    
    private RaceFilter filter;

    public FilteredTrackedRacesList(SailingServiceAsync sailingService, ErrorReporter errorReporter,
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

    @Override
    protected void makeControlsReactToSelectionChange(List<RaceDTO> selectedRaces) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void addControlButtons(HorizontalPanel trackedRacesButtonPanel) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void makeControlsReactToFillRegattas(List<RegattaDTO> regattas) {
        // TODO Auto-generated method stub
        
    }

  

}

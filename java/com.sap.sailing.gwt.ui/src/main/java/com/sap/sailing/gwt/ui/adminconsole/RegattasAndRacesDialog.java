package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class RegattasAndRacesDialog extends DataEntryDialog<Set<RegattaAndRaceIdentifier>> implements RegattasDisplayer{

    protected StringMessages stringMessages;
    protected final TrackedRacesListComposite trackedRacesListComposite;
    protected final MediaTrack mediaTrack; 

    protected List<EventDTO> existingEvents;

    public RegattasAndRacesDialog(SailingServiceAsync sailingService, MediaTrack mediaTrack,
            ErrorReporter errorReporter, RegattaRefresher regattaRefresher, 
            StringMessages stringMessages, Validator<Set<RegattaAndRaceIdentifier>> validator, DialogCallback<Set<RegattaAndRaceIdentifier>> callback) {
        super(stringMessages.addRegatta(), null, stringMessages.ok(), stringMessages.cancel(), validator, callback);
        this.stringMessages = stringMessages;
        // this.regatta = regatta;
        // this.existingEvents = existingEvents;
        trackedRacesListComposite = new TrackedRacesListComposite(sailingService, errorReporter, regattaRefresher,
                new RaceSelectionModel(), stringMessages, /* multiselection */true){
            protected boolean raceIsToBeAddedToList(RaceDTO race) {
                if(mediaTrackIsInTimerangeOf(race)){
                    return true;
                }
                return false;
            }
            protected void addControlButtons(HorizontalPanel trackedRacesButtonPanel) {}
            protected void makeControlsReactToSelectionChange(List<RaceDTO> selectedRaces) {}
            protected void makeControlsReactToFillRegattas(List<RegattaDTO> regattas) {}
        };
        trackedRacesListComposite.ensureDebugId("TrackedRacesListComposite");
        this.mediaTrack = mediaTrack;
        regattaRefresher.fillRegattas();
    }

    
     @Override
     protected Widget getAdditionalWidget() {
     final VerticalPanel panel = new VerticalPanel();
     Widget additionalWidget = super.getAdditionalWidget();
     if (additionalWidget != null) {
     panel.add(additionalWidget);
     }
     Grid formGrid = new Grid(1,1);
     panel.add(formGrid);
     
     formGrid.setWidget(0, 0, trackedRacesListComposite);
    
     return panel;
     }

    @Override
    protected Set<RegattaAndRaceIdentifier> getResult() {
        return getSelectedRegattasAndRaces();
    }


    @Override
    public void fillRegattas(List<RegattaDTO> result) {
        this.trackedRacesListComposite.fillRegattas(result);
    }
    
    public Set<RegattaAndRaceIdentifier> getSelectedRegattasAndRaces(){
        List<RaceDTO> races = trackedRacesListComposite.getSelectedRaces();
        Set<RegattaAndRaceIdentifier> regattasAndRaces = new HashSet<RegattaAndRaceIdentifier>();
        for(RaceDTO race: races){
            regattasAndRaces.add(race.getRaceIdentifier());
        }
        return regattasAndRaces;
    }
    
    private boolean mediaTrackIsInTimerangeOf(RaceDTO race){
        if(mediaTrack.beginsAfter(race.endOfRace) || mediaTrack.endsBefore(race.startOfRace)){
            return false;
        }
        return true;
     }
     
     private boolean eventIsNotConnectedToAnyRaces(EventDTO event){
         if(regattasAreNotConnectedToAnyRaces(event.regattas)){
             return true;
         }
         return false;
     }
     
     private boolean regattasAreNotConnectedToAnyRaces(List<RegattaDTO> regattas){
         for (RegattaDTO regatta : regattas) {
             if(regattaIsConnectedToAnyRaces(regatta)){
                 return false;
             }
         }
         return true;
     }
     private boolean regattaIsConnectedToAnyRaces(RegattaDTO regatta){
         if(regatta.races.size() > 0){
             return true;
         }
         return false;
     }
    
    
}

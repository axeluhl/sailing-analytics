package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Date;
import java.util.List;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.TimeZone;
import com.google.gwt.text.client.DateTimeFormatRenderer;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class TrackedRacesManagementPanel extends AbstractEventManagementPanel implements RaceSelectionChangeListener {
    private RegattaAndRaceIdentifier singleSelectedRace;
    
    private RaceDTO selectedRaceDTO;
    
    private final CaptionPanel selectedRacePanel;
    
    private final DateTimeFormatRenderer dateFormatter = new DateTimeFormatRenderer(
            DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT));
    private final DateTimeFormatRenderer timeFormatter = new DateTimeFormatRenderer(
            DateTimeFormat.getFormat(PredefinedFormat.TIME_LONG));
    private final DateTimeFormatRenderer durationFormatter = new DateTimeFormatRenderer(
            DateTimeFormat.getFormat(PredefinedFormat.TIME_MEDIUM), TimeZone.createTimeZone(0));

    private List<RegattaDTO> savedEvents;
    
    private final Grid raceDataGrid;
    
    public TrackedRacesManagementPanel(final SailingServiceAsync sailingService, ErrorReporter errorReporter,
            RegattaRefresher regattaRefresher, StringMessages stringConstants) {
        super(sailingService, regattaRefresher, errorReporter, new RaceSelectionModel(), stringConstants);

        VerticalPanel mainPanel = new VerticalPanel();
        this.setWidget(mainPanel);
        mainPanel.setWidth("100%");
        
        mainPanel.add(trackedRacesListComposite);

        trackedRacesListComposite.addRaceSelectionChangeListener(this);
        
        singleSelectedRace = null;
        
        selectedRacePanel = new CaptionPanel("???");
        selectedRacePanel.setWidth("100%");
        mainPanel.add(selectedRacePanel);

        VerticalPanel vPanel = new VerticalPanel();
        vPanel.setWidth("100%");
        selectedRacePanel.setContentWidget(vPanel);
        selectedRacePanel.setVisible(false);
        
        raceDataGrid = new Grid(6,2);
        vPanel.add(raceDataGrid);
        
        raceDataGrid.setText(0, 0, "StartTime:");
        raceDataGrid.setText(1, 0, "EndTime:");
        raceDataGrid.setText(2, 0, "Duration:");
        raceDataGrid.setText(3, 0, "Start of tracking:");
        raceDataGrid.setText(4, 0, "End of tracking:");
        raceDataGrid.setText(5, 0, "Delay to live (ms):");
    }

    @Override
    public void fillRegattas(List<RegattaDTO> result) {
        trackedRacesListComposite.fillRegattas(result);
        savedEvents = result;
    }
    
    public void onRaceSelectionChange(List<RegattaAndRaceIdentifier> selectedRaces) {
        if (selectedRaces.size() == 1) {
            singleSelectedRace = selectedRaces.get(0);
            selectedRacePanel.setCaptionText(singleSelectedRace.getRaceName());
            selectedRacePanel.setVisible(true);
            
            for (RegattaDTO regatta : savedEvents) {
                for (RaceDTO race : regatta.races) {
                    if (race != null && race.getRaceIdentifier().equals(singleSelectedRace)) {
                        this.selectedRaceDTO = race;
                        refreshRaceData();
                        break;
                    }
                }
            }
        } else {
            selectedRacePanel.setCaptionText("");
            singleSelectedRace = null;
            selectedRacePanel.setVisible(false);
        }
    }
    
    private void refreshRaceData() {
        if (singleSelectedRace != null && selectedRaceDTO != null) {
            if (selectedRaceDTO.startOfRace != null) {
                raceDataGrid.setText(0, 1, dateFormatter.render(selectedRaceDTO.startOfRace) + " "
                        + timeFormatter.render(selectedRaceDTO.startOfRace));
            } else {
                raceDataGrid.setText(0, 1, "");
            }
            if (selectedRaceDTO.endOfRace != null) {
                raceDataGrid.setText(1, 1, dateFormatter.render(selectedRaceDTO.endOfRace) + " "
                        + timeFormatter.render(selectedRaceDTO.endOfRace));
            } else {
                raceDataGrid.setText(1, 1, "");
            }
            if(selectedRaceDTO.startOfRace != null && selectedRaceDTO.endOfRace != null) {
                Date duration = new Date(selectedRaceDTO.endOfRace.getTime() - selectedRaceDTO.startOfRace.getTime());
                raceDataGrid.setText(2, 1, durationFormatter.render(duration));
            } else {
                raceDataGrid.setText(2, 1, "");
            }
            if(selectedRaceDTO.startOfTracking != null) {
                raceDataGrid.setText(3, 1, dateFormatter.render(selectedRaceDTO.startOfTracking) + " "
                        + timeFormatter.render(selectedRaceDTO.startOfTracking));
            } else {
                raceDataGrid.setText(3, 1, "");
            }
            if(selectedRaceDTO.endOfTracking != null) {
                raceDataGrid.setText(4, 1, dateFormatter.render(selectedRaceDTO.endOfTracking) + " "
                        + timeFormatter.render(selectedRaceDTO.endOfTracking));
            } else {
                raceDataGrid.setText(4, 1, "");
            }
            raceDataGrid.setText(5, 1, "" + selectedRaceDTO.delayToLiveInMs);
        }
    }
}

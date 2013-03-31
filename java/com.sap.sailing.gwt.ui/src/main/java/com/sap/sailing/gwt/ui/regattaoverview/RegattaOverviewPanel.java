package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.MarkedAsyncCallback;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;

public class RegattaOverviewPanel extends SimplePanel implements RegattaOverviewRaceSelectionChangeListener {
    private final SailingServiceAsync sailingService;
    @SuppressWarnings("unused")
    private final ErrorReporter errorReporter;
    @SuppressWarnings("unused")
    private final StringMessages stringMessages;
    private final String eventIdAsString;
    
    private RegattaOverviewRaceSelectionModel raceSelectionProvider;
    
    private RegattaOverviewTableComposite regattaOverviewTableComposite;
    private CourseDesignTableComposite raceCourseDesignDetailsComposite;
    
    private final Label eventNameLabel;
    private final Label venueNameLabel;
    
    public RegattaOverviewPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter, StringMessages stringMessages, String eventIdAsString) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.eventIdAsString = eventIdAsString;
        
        VerticalPanel mainPanel = new VerticalPanel();
        setWidget(mainPanel);
        mainPanel.setWidth("100%");
        
        eventNameLabel = new Label();
        venueNameLabel = new Label();
        
        Grid grid = new Grid(1, 2);
        
        
        raceSelectionProvider = new RegattaOverviewRaceSelectionModel(false);
        raceSelectionProvider.addRegattaOverviewRaceSelectionChangeListener(this);
        
        regattaOverviewTableComposite = new RegattaOverviewTableComposite(sailingService, errorReporter, stringMessages, eventIdAsString, raceSelectionProvider);
        
        raceCourseDesignDetailsComposite = new CourseDesignTableComposite(sailingService, errorReporter, stringMessages);
        grid.setWidget(0, 0, raceCourseDesignDetailsComposite);
        
        grid.setWidget(0, 1, regattaOverviewTableComposite);
        grid.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment.ALIGN_TOP);
        grid.getColumnFormatter().getElement(1).getStyle().setPaddingTop(2.0, Unit.EM);
        
        mainPanel.add(eventNameLabel);
        mainPanel.add(venueNameLabel);
        mainPanel.add(grid);
        
        fillEventAndVenueName();
    }

    @Override
    public void onRegattaOverviewEntrySelectionChange(List<RegattaOverviewEntryDTO> selectedRegattaOverviewEntries) {
        final RegattaOverviewEntryDTO selectedRegattaOverviewEntry;
        if (selectedRegattaOverviewEntries.iterator().hasNext()) {
            selectedRegattaOverviewEntry = selectedRegattaOverviewEntries.iterator().next();
        if (selectedRegattaOverviewEntry != null && regattaOverviewTableComposite.getAllRaces() != null) {
            for(RegattaOverviewEntryDTO regattaOverviewEntryDTO: regattaOverviewTableComposite.getAllRaces()) {
                if(regattaOverviewEntryDTO.equals(selectedRegattaOverviewEntry)) {//TOTOTOTOTO!!!!!!!!!!!!
                    raceCourseDesignDetailsComposite.setRace(regattaOverviewEntryDTO);
                    break;
                }
            }
        }
        } else {
            raceCourseDesignDetailsComposite.setRace(null);
        }
    }
    
    private void fillEventAndVenueName() {
        sailingService.getEventByIdAsString(eventIdAsString, new MarkedAsyncCallback<EventDTO>() {

            @Override
            protected void handleFailure(Throwable cause) {
                
            }

            @Override
            protected void handleSuccess(EventDTO result) {
                if (result != null) {
                    eventNameLabel.setText(result.name);
                    venueNameLabel.setText(result.venue.name);
                }
            }
        });
    }
    
}

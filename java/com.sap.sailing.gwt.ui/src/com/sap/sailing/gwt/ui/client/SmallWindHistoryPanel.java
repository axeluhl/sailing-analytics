package com.sap.sailing.gwt.ui.client;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.PositionDAO;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;
import com.sap.sailing.gwt.ui.shared.Triple;

/**
 * Displays a bit of wind history around the time notified to this time listener.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class SmallWindHistoryPanel extends FormPanel implements TimeListener, RaceSelectionChangeListener {
    private final PositionDAO position;
    private final Label[] windSpeedLabels;
    private final Label[] windDirectionLabels;
    private Date date;
    private EventDAO event;
    private RaceDAO race;
    
    /**
     * @param windTrack
     *            the wind track to visualize using this panel
     * @param position
     *            the position to show the wind for; can, e.g., be the middle of the race course if an average wind
     *            display for a race is desired
     * @param numberOfTimepoints
     *            the number of wind displays arranged in this panel; each display shows the estimated wind
     * @param millisecondStepsPerLabel
     */
    public SmallWindHistoryPanel(PositionDAO position, int numberOfTimepoints,
            long millisecondStepsPerLabel, StringConstants stringConstants) {
        this.position = position;
        windSpeedLabels = new Label[numberOfTimepoints];
        windDirectionLabels = new Label[numberOfTimepoints];
        Grid grid = new Grid(4, numberOfTimepoints+1);
        setWidget(grid);
        grid.setWidget(0, 0, new Label(stringConstants.wind()));
        grid.setWidget(1, 0, new Label(stringConstants.speedInKnots()));
        grid.setWidget(2, 0, new Label(stringConstants.fromDeg()));
        for (int i=0; i<numberOfTimepoints; i++) {
            grid.setWidget(0, i+1, new Label("t-"+((numberOfTimepoints-i)*millisecondStepsPerLabel/1000)+"s"));
        }
    }
    
    private void updateWindDisplay() {
        // TODO fetch wind information for event/race/date and display in labels
    }
    
    @Override
    public void timeChanged(Date date) {
        this.date = date;
        updateWindDisplay();
    }

    @Override
    public void onRaceSelectionChange(List<Triple<EventDAO, RegattaDAO, RaceDAO>> selectedRaces) {
        event = selectedRaces.get(selectedRaces.size()-1).getA();
        race = selectedRaces.get(selectedRaces.size()-1).getC();
        updateWindDisplay();
    }

}

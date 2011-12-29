package com.sap.sailing.gwt.ui.client;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.PositionDAO;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;
import com.sap.sailing.gwt.ui.shared.Triple;
import com.sap.sailing.gwt.ui.shared.WindDAO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDAO;
import com.sap.sailing.server.api.EventNameAndRaceName;

/**
 * Displays a bit of wind history around the time notified to this time listener.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class SmallWindHistoryPanel extends FormPanel implements TimeListener, RaceSelectionChangeListener {
    private final PositionDAO position;
    private final WindIndicator[] windIndicators;
    private final Label selectedWindSourceLabel;
    private Date date;
    private EventDAO event;
    private RaceDAO race;
    private final SailingServiceAsync sailingService;
    private final long millisecondStepsPerLabel;
    private final ErrorReporter errorReporter;
    private final StringConstants stringConstants;
    
    /**
     * @param position
     *            the position to show the wind for; can, e.g., be the middle of the race course if an average wind
     *            display for a race is desired
     * @param numberOfTimepoints
     *            the number of wind displays arranged in this panel; each display shows the estimated wind
     * @param windTrack
     *            the wind track to visualize using this panel
     */
    public SmallWindHistoryPanel(SailingServiceAsync sailingService, PositionDAO position,
            int numberOfTimepoints, long millisecondStepsPerLabel, StringConstants stringConstants, ErrorReporter errorReporter) {
        this.sailingService = sailingService;
        this.position = position;
        this.stringConstants = stringConstants;
        this.millisecondStepsPerLabel = millisecondStepsPerLabel;
        this.errorReporter = errorReporter;
        windIndicators = new WindIndicator[numberOfTimepoints];
        VerticalPanel vp = new VerticalPanel();
        setWidget(vp);
        Grid grid = new Grid(2, numberOfTimepoints);
        selectedWindSourceLabel = new Label();
        vp.add(selectedWindSourceLabel);
        for (int i=0; i<numberOfTimepoints; i++) {
            windIndicators[i] = new WindIndicator();
            grid.setWidget(0, i, new Label("t-"+((numberOfTimepoints-i-1)*millisecondStepsPerLabel/1000)+"s"));
            grid.setWidget(1, i, windIndicators[i]);
        }
        vp.add(grid);
    }
    
    private void updateWindDisplay() {
        if (date != null) {
            Date from = new Date(date.getTime() - windIndicators.length * millisecondStepsPerLabel);
            if (event != null && race != null) {
                sailingService.getWindInfo(new EventNameAndRaceName(event.name, race.name), from, millisecondStepsPerLabel,
                        windIndicators.length, position.latDeg, position.lngDeg, /* all sources */ null,
                        new AsyncCallback<WindInfoForRaceDAO>() {
                            @Override
                            public void onSuccess(WindInfoForRaceDAO result) {
                                // expecting to find windIndicators.length fixes
                                if (result == null || result.windTrackInfoByWindSource.get(result.selectedWindSource).windFixes
                                        .size() != windIndicators.length) {
                                    clearWindDisplay();
                                } else {
                                    setSelectedWindSource(result.selectedWindSource);
                                    int i = 0;
                                    for (WindDAO fix : result.windTrackInfoByWindSource.get(result.selectedWindSource).windFixes) {
                                        updateWindIndicator(i, fix);
                                        i++;
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Error trying to obtain wind information for race "
                                        + race.name + " in event " + event.name + ": " + caught.getMessage());
                            }
                        });
            }
        }
    }
    
    private void clearWindDisplay() {
        for (int i=0; i<windIndicators.length; i++) {
            windIndicators[i].setFromDeg(0);
            windIndicators[i].setSpeedInKnots(0.0);
        }
    }

    private void updateWindIndicator(int i, WindDAO fix) {
        windIndicators[i].setFromDeg(fix.dampenedTrueWindFromDeg);
        windIndicators[i].setSpeedInKnots(fix.dampenedTrueWindSpeedInKnots);
    }

    private void setSelectedWindSource(WindSource selectedWindSource) {
        selectedWindSourceLabel.setText(stringConstants.windSource()+": "+selectedWindSource.name());
    }

    @Override
    public void timeChanged(Date date) {
        this.date = date;
        updateWindDisplay();
    }

    @Override
    public void onRaceSelectionChange(List<Triple<EventDAO, RegattaDAO, RaceDAO>> selectedRaces) {
        if (!selectedRaces.isEmpty()) {
            event = selectedRaces.get(selectedRaces.size() - 1).getA();
            race = selectedRaces.get(selectedRaces.size() - 1).getC();
            updateWindDisplay();
        }
    }

}

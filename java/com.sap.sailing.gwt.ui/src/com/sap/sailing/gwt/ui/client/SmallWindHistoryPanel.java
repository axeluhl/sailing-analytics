package com.sap.sailing.gwt.ui.client;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.PositionDAO;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;
import com.sap.sailing.gwt.ui.shared.Triple;
import com.sap.sailing.gwt.ui.shared.WindDAO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDAO;

/**
 * Displays a bit of wind history around the time notified to this time listener.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class SmallWindHistoryPanel extends FormPanel implements TimeListener, RaceSelectionChangeListener {
    private final PositionDAO position;
    private final HTML[] windSpeedAndDirectionLabels;
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
        windSpeedAndDirectionLabels = new HTML[numberOfTimepoints];
        windIndicators = new WindIndicator[numberOfTimepoints];
        Grid grid = new Grid(4, numberOfTimepoints+1);
        selectedWindSourceLabel = new Label();
        setWidget(grid);
        grid.setWidget(0, 0, selectedWindSourceLabel);
        for (int i=0; i<numberOfTimepoints; i++) {
            windSpeedAndDirectionLabels[i] = new HTML();
            windIndicators[i] = new WindIndicator();
            grid.setWidget(0, i+1, new Label("t-"+((numberOfTimepoints-i)*millisecondStepsPerLabel/1000)+"s"));
            grid.setWidget(1, i+1, windSpeedAndDirectionLabels[i]);
            grid.setWidget(2, i+1, windIndicators[i]);
        }
    }
    
    private void updateWindDisplay() {
        if (date != null) {
            Date from = new Date(date.getTime() - windIndicators.length * millisecondStepsPerLabel);
            if (event != null && race != null) {
                sailingService.getWindInfo(event.name, race.name, from, millisecondStepsPerLabel,
                        windIndicators.length, position.latDeg, position.lngDeg,
                        new AsyncCallback<WindInfoForRaceDAO>() {
                            @Override
                            public void onSuccess(WindInfoForRaceDAO result) {
                                // expecting to find windIndicators.length fixes
                                if (result.windTrackInfoByWindSourceName.get(result.selectedWindSourceName).windFixes
                                        .size() != windIndicators.length) {
                                    throw new RuntimeException(
                                            "Unexpected number of wind fixes. Expected "
                                                    + windIndicators.length
                                                    + " but received "
                                                    + result.windTrackInfoByWindSourceName
                                                            .get(result.selectedWindSourceName).windFixes.size());
                                } else {
                                    setSelectedWindSource(result.selectedWindSourceName);
                                    int i = 0;
                                    for (WindDAO fix : result.windTrackInfoByWindSourceName
                                            .get(result.selectedWindSourceName).windFixes) {
                                        updateLabel(i, fix);
                                        updateWindIndicator(i, fix);
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
    
    private void updateWindIndicator(int i, WindDAO fix) {
        windIndicators[i].setFromDeg(fix.dampenedTrueWindFromDeg);
        windIndicators[i].setSpeedInKnots(fix.dampenedTrueWindSpeedInKnots);
    }

    private void updateLabel(int i, WindDAO fix) {
        HTML l = windSpeedAndDirectionLabels[i];
        l.setHTML(fix.dampenedTrueWindSpeedInKnots+"kn from "+fix.dampenedTrueWindFromDeg.intValue()+"&deg;");
    }

    private void setSelectedWindSource(String selectedWindSourceName) {
        selectedWindSourceLabel.setText(stringConstants.windSource()+": "+selectedWindSourceName);
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

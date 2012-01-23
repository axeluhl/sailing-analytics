package com.sap.sailing.gwt.ui.leaderboard;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.gwt.ui.adminconsole.WindIndicator;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TimeListener;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.RaceDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sailing.server.api.EventNameAndRaceName;

/**
 * Displays a bit of wind history around the time notified to this time listener.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class SmallWindHistoryPanel extends FormPanel implements TimeListener, RaceSelectionChangeListener {
    private final PositionDTO position;
    private final WindIndicator[] windIndicators;
    private final Label selectedWindSourceLabel;
    private Date date;
    private EventDTO event;
    private RaceDTO race;
    private final SailingServiceAsync sailingService;
    private final long millisecondStepsPerLabel;
    private final ErrorReporter errorReporter;
    private final StringMessages stringConstants;
    
    /**
     * @param position
     *            the position to show the wind for; can, e.g., be the middle of the race course if an average wind
     *            display for a race is desired
     * @param numberOfTimepoints
     *            the number of wind displays arranged in this panel; each display shows the estimated wind
     * @param windTrack
     *            the wind track to visualize using this panel
     */
    public SmallWindHistoryPanel(SailingServiceAsync sailingService, PositionDTO position,
            int numberOfTimepoints, long millisecondStepsPerLabel, StringMessages stringConstants, ErrorReporter errorReporter) {
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
                        new AsyncCallback<WindInfoForRaceDTO>() {
                            @Override
                            public void onSuccess(WindInfoForRaceDTO result) {
                                // expecting to find windIndicators.length fixes
                                if (result == null || result.windTrackInfoByWindSource.get(result.selectedWindSource).windFixes
                                        .size() != windIndicators.length) {
                                    clearWindDisplay();
                                } else {
                                    setSelectedWindSource(result.selectedWindSource);
                                    int i = 0;
                                    for (WindDTO fix : result.windTrackInfoByWindSource.get(result.selectedWindSource).windFixes) {
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

    private void updateWindIndicator(int i, WindDTO fix) {
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
    public void onRaceSelectionChange(List<RaceDTO> selectedRaces) {
        if (!selectedRaces.isEmpty()) {
            race = selectedRaces.get(selectedRaces.size() - 1);
            event = race.getEvent();
            updateWindDisplay();
        }
    }

}

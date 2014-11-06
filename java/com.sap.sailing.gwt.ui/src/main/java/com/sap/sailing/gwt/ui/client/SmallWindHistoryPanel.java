package com.sap.sailing.gwt.ui.client;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.dto.PositionDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.player.TimeListener;

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
    private RegattaAndRaceIdentifier race;
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
            if (race != null) {
                sailingService.getAveragedWindInfo(race, from, millisecondStepsPerLabel,
                        windIndicators.length, position.latDeg, position.lngDeg, /* all sources */ null,
                        new AsyncCallback<WindInfoForRaceDTO>() {
                            @Override
                            public void onSuccess(WindInfoForRaceDTO result) {
                                if (result != null) {
                                    WindSource bestWindSource = getBestWindSource(result, windIndicators.length);
                                    // expecting to find windIndicators.length fixes
                                    if (result == null
                                            || result.windTrackInfoByWindSource.get(bestWindSource).windFixes.size() != windIndicators.length) {
                                        clearWindDisplay();
                                    } else {
                                        setSelectedWindSource(bestWindSource);
                                        int i = 0;
                                        for (WindDTO fix : result.windTrackInfoByWindSource.get(bestWindSource).windFixes) {
                                            updateWindIndicator(i, fix);
                                            i++;
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Error trying to obtain wind information for race "
                                        + race + ": " + caught.getMessage());
                            }
                        });
            }
        }
    }
    
    /**
     * Looks for the best wind source in <code>windInfo</code> that has <code>length</code> fixes. Uses the order of
     * literals of {@link WindSourceType} to determine precedence.
     * 
     * @return <code>null</code> if no wind source has the expected number of fixes; the best wind source with the
     *         expected number of fixes otherwise
     */
    private WindSource getBestWindSource(WindInfoForRaceDTO windInfo, int expectedNumberOfFixes) {
        List<WindSourceType> windSourceTypesInOrder = Arrays.asList(WindSourceType.values());
        WindSource result = null;
        int bestIndexSoFar = Integer.MAX_VALUE;
        for (Map.Entry<WindSource, WindTrackInfoDTO> e : windInfo.windTrackInfoByWindSource.entrySet()) {
            if (e.getValue().windFixes.size() == expectedNumberOfFixes) {
                int index = windSourceTypesInOrder.indexOf(e.getKey());
                if (index < bestIndexSoFar) {
                    bestIndexSoFar = index;
                    result = e.getKey();
                }
            }
        }
        return result;
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
    public void timeChanged(Date newTime, Date oldTime) {
        this.date = newTime;
        updateWindDisplay();
    }

    @Override
    public void onRaceSelectionChange(List<RegattaAndRaceIdentifier> selectedRaces) {
        if (!selectedRaces.isEmpty()) {
            race = selectedRaces.get(selectedRaces.size() - 1);
            updateWindDisplay();
        }
    }

}

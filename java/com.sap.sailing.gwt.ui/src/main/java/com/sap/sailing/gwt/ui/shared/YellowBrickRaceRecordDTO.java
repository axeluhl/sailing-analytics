package com.sap.sailing.gwt.ui.shared;

import java.util.Collections;

import com.sap.sse.common.TimePoint;

/**
 * The "race URL" is the part of the YellowBrick API URL that provides an ID representing the race. For example, the
 * "race URL" for the Rolex Middle Sea Race 2019 is "rmsr2019".
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class YellowBrickRaceRecordDTO extends AbstractRaceRecordDTO {
    private String raceUrl;
    private TimePoint timePointOfLastFix;
    private int numberOfCompetitors;

    @Deprecated // GWT only
    YellowBrickRaceRecordDTO() {
    }

    public YellowBrickRaceRecordDTO(String name, String raceUrl, boolean hasRememberedRegatta,
            TimePoint timePointOfLastFix, int numberOfCompetitors) {
        super(name, hasRememberedRegatta);
        this.raceUrl = raceUrl;
        this.timePointOfLastFix = timePointOfLastFix;
        this.numberOfCompetitors = numberOfCompetitors;
    }

    @Override
    public Iterable<String> getBoatClassNames() {
        return Collections.emptySet();
    }

    public String getRaceUrl() {
        return raceUrl;
    }

    public TimePoint getTimePointOfLastFix() {
        return timePointOfLastFix;
    }

    public int getNumberOfCompetitors() {
        return numberOfCompetitors;
    }
}
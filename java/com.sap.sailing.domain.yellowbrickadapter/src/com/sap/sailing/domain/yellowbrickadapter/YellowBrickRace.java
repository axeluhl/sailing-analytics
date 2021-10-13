package com.sap.sailing.domain.yellowbrickadapter;

import com.sap.sse.common.TimePoint;

/**
 * Some metadata about a YellowBrick race, including its race URL which is considered a unique key for the YellowBrick
 * API, as well as the number of competitors and the time point of the last fix received for that race, which may help
 * for an approximate time ordering of races / events.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface YellowBrickRace {
    String getRaceUrl();

    /**
     * @return may return {@code null} if no fix exists yet or the race has no competitors assigned
     */
    TimePoint getTimePointOfLastFix();

    int getNumberOfCompetitors();
}

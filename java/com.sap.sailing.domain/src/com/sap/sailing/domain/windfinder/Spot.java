package com.sap.sailing.domain.windfinder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.windfinder.SpotBase;
import com.sap.sse.common.TimePoint;

/**
 * A measurement spot for which a report and / or a forecast may exist. Equality and
 * hash code are based on the {@link #getId() ID} only.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface Spot extends SpotBase {
    Wind getLatestMeasurement() throws NumberFormatException, ParseException, org.json.simple.parser.ParseException, MalformedURLException, IOException;

    Iterable<Wind> getAllMeasurements() throws IOException, MalformedURLException, ParseException, org.json.simple.parser.ParseException;

    /**
     * @param timePoint
     *            if {@code null} then this call delivers the result of {@link #getAllMeasurements()}
     * @return an always valid but possibly empty collection
     */
    Iterable<Wind> getAllMeasurementsAfter(TimePoint timePoint) throws MalformedURLException, IOException, ParseException, org.json.simple.parser.ParseException;
}

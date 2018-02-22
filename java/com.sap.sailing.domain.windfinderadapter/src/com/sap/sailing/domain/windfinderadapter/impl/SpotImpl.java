package com.sap.sailing.domain.windfinderadapter.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.windfinder.SpotDTO;
import com.sap.sailing.domain.windfinder.ReviewedSpotsCollection;
import com.sap.sailing.domain.windfinder.Spot;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class SpotImpl extends SpotDTO implements Spot {
    private static final long serialVersionUID = -7793334775486020313L;

    private final WindFinderReportParser parser;
    private final ReviewedSpotsCollection collection;
    
    public SpotImpl(String name, String id, String keyword, String englishCountryName, Position position, WindFinderReportParser parser, ReviewedSpotsCollection collection) {
        super(name, id, keyword, englishCountryName, position);
        this.parser = parser;
        this.collection = collection;
    }
    
    @Override
    public Wind getLatestMeasurement() throws NumberFormatException, ParseException, org.json.simple.parser.ParseException, MalformedURLException, IOException {
        final Iterable<Wind> measurements = getAllMeasurements();
        final Wind result;
        if (measurements != null && !Util.isEmpty(measurements)) {
            final List<Wind> measurementsSortedByTimepoint = new ArrayList<>();
            Util.addAll(measurements, measurementsSortedByTimepoint);
            Collections.sort(measurementsSortedByTimepoint, (w1, w2)->w1.getTimePoint().compareTo(w2.getTimePoint()));
            result = Util.last(measurementsSortedByTimepoint);
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public Iterable<Wind> getAllMeasurementsAfter(TimePoint timePoint) throws MalformedURLException, IOException, ParseException, org.json.simple.parser.ParseException {
        final List<Wind> result = new ArrayList<>();
        for (final Wind measurement : getAllMeasurements()) {
            if (timePoint == null || measurement.getTimePoint().after(timePoint)) {
                result.add(measurement);
            }
        }
        return result;
    }
    
    @Override
    public Iterable<Wind> getAllMeasurements()
            throws IOException, MalformedURLException, ParseException, org.json.simple.parser.ParseException {
        final InputStream response = (InputStream) getMeasurementsUrl().getContent();
        final Iterable<Wind> measurements = parser.parse(getPosition(), (JSONArray) new JSONParser().parse(new InputStreamReader(response)));
        return measurements;
    }

    private URL getMeasurementsUrl() throws MalformedURLException {
        return new URL(Activator.BASE_URL_FOR_JSON_DOCUMENTS+"/"+collection.getId()+"_"+getId()+".json");
    }
}

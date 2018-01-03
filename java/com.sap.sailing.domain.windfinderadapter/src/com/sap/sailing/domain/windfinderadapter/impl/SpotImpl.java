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
import com.sap.sailing.domain.windfinderadapter.ReviewedSpotsCollection;
import com.sap.sailing.domain.windfinderadapter.Spot;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.NamedImpl;

public class SpotImpl extends NamedImpl implements Spot {
    private static final long serialVersionUID = 1642900468710612984L;
    private static final String BASE_URL = "https://www.windfinder.com";
    private static final String BASE_REPORT_URL = BASE_URL + "/report";
    private static final String BASE_FORECAST_URL = BASE_URL + "/forecast";
    private static final String BASE_STATISTICS_URL = BASE_URL + "/windstatistics";

    private final String id;
    private final String keyword;
    private final String englishCountryName;
    private final Position position;
    private final WindFinderReportParser parser;
    private final ReviewedSpotsCollection collection;
    
    public SpotImpl(String name, String id, String keyword, String englishCountryName, Position position, WindFinderReportParser parser, ReviewedSpotsCollection collection) {
        super(name);
        this.id = id;
        this.keyword = keyword;
        this.englishCountryName = englishCountryName;
        this.position = position;
        this.parser = parser;
        this.collection = collection;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SpotImpl other = (SpotImpl) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getKeyword() {
        return keyword;
    }

    @Override
    public String getEnglishCountryName() {
        return englishCountryName;
    }

    @Override
    public URL getReportUrl() throws MalformedURLException {
        return new URL(BASE_REPORT_URL+"/"+getKeyword());
    }

    @Override
    public URL getForecastUrl() throws MalformedURLException {
        return new URL(BASE_FORECAST_URL+"/"+getKeyword());
    }

    @Override
    public URL getStatisticsUrl() throws MalformedURLException {
        return new URL(BASE_STATISTICS_URL+"/"+getKeyword());
    }
    
    @Override
    public Wind getLatestMeasurement() throws NumberFormatException, ParseException, org.json.simple.parser.ParseException, MalformedURLException, IOException {
        final InputStream response = (InputStream) getMeasurementsUrl().getContent();
        final Iterable<Wind> measurements = parser.parse(getPosition(), (JSONArray) new JSONParser().parse(new InputStreamReader(response)));
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

    private URL getMeasurementsUrl() throws MalformedURLException {
        return new URL(Activator.BASE_URL_FOR_JSON_DOCUMENTS+"/"+collection.getId()+"_"+getId()+".json");
    }
}

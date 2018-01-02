package com.sap.sailing.domain.windfinderadapter.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Wind;
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
    private final Position position;
    private final WindFinderReportParser parser;
    
    public SpotImpl(String name, String id, String keyword, Position position, WindFinderReportParser parser) {
        super(name);
        this.id = id;
        this.keyword = keyword;
        this.position = position;
        this.parser = parser;
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
            result = Util.last(measurements);
        } else {
            result = null;
        }
        return result;
    }

    private URL getMeasurementsUrl() throws MalformedURLException {
        // TODO this hasn't been aligned with WindFinder yet; so far we're seeing "random" filenames in the URLs such as sap_schilksee_10044N.json...
//        return new URL("http://external.windfinder.com/sap_"+getId()+".json");
        return new URL("http://external.windfinder.com/sap_schilksee_10044N.json");
    }
}

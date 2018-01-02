package com.sap.sailing.domain.windfinderadapter.impl;

import java.net.MalformedURLException;
import java.net.URL;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.windfinderadapter.Spot;
import com.sap.sse.common.impl.NamedImpl;

public class SpotImpl extends NamedImpl implements Spot {
    private static final long serialVersionUID = 1642900468710612984L;
    private static final String BASE_URL = "https://www.windfinder.com";
    private static final String BASE_REPORT_URL = BASE_URL + "/report";
    private static final String BASE_FORECAST_URL = BASE_URL + "/forecast";

    private final String id;
    private final String keyword;
    private final Position position;
    
    public SpotImpl(String name, String id, String keyword, Position position) {
        super(name);
        this.id = id;
        this.keyword = keyword;
        this.position = position;
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

}

package com.sap.sailing.domain.windfinderadapter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

import com.sap.sailing.domain.common.Positioned;
import com.sap.sailing.domain.common.Wind;
import com.sap.sse.common.NamedWithID;

/**
 * A measurement spot for which a report and / or a forecast may exist. Equality and
 * hash code are based on the {@link #getId() ID} only.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface Spot extends NamedWithID, Positioned {
    /**
     * A spot's ID is always a {@link String}
     */
    @Override
    String getId();
    
    /**
     * A keyword that is used, in particular, when constructing the URLs for {@link #getReportUrl() report} and
     * {@link #getForecastUrl() forecast} web pages.
     */
    String getKeyword();
    
    /**
     * Name of the country this spot is in, provided in the English language
     */
    String getEnglishCountryName();
    
    /**
     * @return the URL of a web page showing a current weather report for this spot
     */
    URL getReportUrl() throws MalformedURLException;
    
    /**
     * @return the URL of a web page showing a weather forecast for this spot
     */
    URL getForecastUrl() throws MalformedURLException;
    
    /**
     * @return the URL of a web page showing general wind statistics for this spot
     */
    URL getStatisticsUrl() throws MalformedURLException;

    Wind getLatestMeasurement() throws NumberFormatException, ParseException, org.json.simple.parser.ParseException, MalformedURLException, IOException;
}

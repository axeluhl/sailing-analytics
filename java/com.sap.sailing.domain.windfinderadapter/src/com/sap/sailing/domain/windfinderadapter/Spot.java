package com.sap.sailing.domain.windfinderadapter;

import java.net.MalformedURLException;
import java.net.URL;

import com.sap.sailing.domain.common.Positioned;
import com.sap.sse.common.NamedWithID;

/**
 * A measurement spot for which a report and / or a forecast may exist.
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
    
    URL getReportUrl() throws MalformedURLException;
    
    URL getForecastUrl() throws MalformedURLException;
}

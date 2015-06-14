package com.sap.sailing.domain.base;

import java.net.URL;

import com.sap.sailing.domain.common.Renamable;
import com.sap.sse.common.Named;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.WithID;
import com.sap.sse.common.media.WithMedia;

/**
 * Base interface for an Event consisting of all static information, which might be shared
 * by the server and an Android application.
 */
public interface EventBase extends Named, WithDescription, Renamable, WithID, WithMedia {

    void setDescription(String description);
    
    /**
     * @return a non-<code>null</code> venue for this event
     */
    Venue getVenue();

    /**
     *  @return the start date of the event 
     */
    TimePoint getStartDate();

    void setStartDate(TimePoint startDate);

    /**
     *  @return the end date of the event 
     */
    TimePoint getEndDate();

    void setEndDate(TimePoint startDate);

    boolean isPublic();

    void setPublic(boolean isPublic);

    /**
     * @deprecated
     * Returns a non-<code>null</code> live but unmodifiable collection of URLs pointing to image resources that can be
     * used to represent the event, e.g., on a web page.
     * 
     * @return a non-<code>null</code> value which may be empty
     */
    Iterable<URL> getImageURLs();
    
    /**
     * @deprecated
     * An event may have zero or more sponsors, each of which usually want to see their logo on the web page.
     * 
     * @return the sponsors' logos; always non-<code>null</code> but possibly empty
     */
    Iterable<URL> getSponsorImageURLs();

    /**
     * @deprecated
     * An optional logo image; may return <code>null</code>.
     */
    URL getLogoImageURL();

    /**
     * @deprecated
     * Returns a non-<code>null</code> live but unmodifiable collection of URLs pointing to video resources that can be
     * used to represent the event, e.g., on a web page.
     * 
     * @return a non-<code>null</code> value which may be empty
     */
    Iterable<URL> getVideoURLs();

    /**
     * @return the URL of the event's official web site, or <code>null</code> if such a site does not exist or its URL
     *         is not known
     */
    URL getOfficialWebsiteURL();
    
    void setOfficialWebsiteURL(URL officialWebsiteURL);

    Iterable<? extends LeaderboardGroupBase> getLeaderboardGroups();

    /** 
     * Sets and converts all event images and videos from the old URL based format to the new richer format 
     * */ 
    boolean setMediaURLs(Iterable<URL> imageURLs, Iterable<URL> sponsorImageURLs, Iterable<URL> videoURLs, URL logoImageURL);
}

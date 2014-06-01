package com.sap.sailing.domain.base;

import java.net.URL;

import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.common.Renamable;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WithID;

/**
 * Base interface for an Event consisting of all static information, which might be shared
 * by the server and an Android application.
 */
public interface EventBase extends Named, Renamable, WithID {

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
     * Returns a non-<code>null</code> live but unmodifiable collection of URLs pointing to image resources that can be
     * used to represent the event, e.g., on a web page.
     * 
     * @return a non-<code>null</code> value which may be empty
     */
    Iterable<URL> getImageURLs();
    
    void addImageURL(URL imageURL);
    
    void removeImageURL(URL imageURL);
    
    /**
     * Replaces the {@link #getImageURLs() current contents of the image URL sequence} by the image URLs in
     * <code>imageURLs</code>.
     * 
     * @param imageURLs
     *            if <code>null</code>, the internal sequence of image URLs is cleared but remains valid (non-
     *            <code>null</code>)
     */
    void setImageURLs(Iterable<URL> imageURLs);

    /**
     * Returns a non-<code>null</code> live but unmodifiable collection of URLs pointing to video resources that can be
     * used to represent the event, e.g., on a web page.
     * 
     * @return a non-<code>null</code> value which may be empty
     */
    Iterable<URL> getVideoURLs();
    
    void addVideoURL(URL videoURL);
    
    void removeVideoURL(URL videoURL);

    /**
     * Replaces the {@link #getVideoURLs() current contents of the video URL sequence} by the video URLs in
     * <code>videoURLs</code>.
     * 
     * @param videoURLs
     *            if <code>null</code>, the internal sequence of image URLs is cleared but remains valid (non-
     *            <code>null</code>)
     */
    void setVideoURLs(Iterable<URL> videoURLs);
}

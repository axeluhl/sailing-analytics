package com.sap.sailing.domain.base;

import java.net.URL;
import java.util.concurrent.ExecutionException;

import com.sap.sailing.domain.common.ImageSize;
import com.sap.sailing.domain.common.Renamable;
import com.sap.sse.common.Named;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.WithID;

/**
 * Base interface for an Event consisting of all static information, which might be shared
 * by the server and an Android application.
 */
public interface EventBase extends Named, WithDescription, Renamable, WithID {

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
     * Returns a non-<code>null</code> live but unmodifiable collection of URLs pointing to image resources that can be
     * used to represent the event, e.g., on a web page.
     * 
     * @return a non-<code>null</code> value which may be empty
     */
    Iterable<URL> getImageURLs();
    
    void addImageURL(URL imageURL);
    
    void removeImageURL(URL imageURL);
    
    /**
     * An event may have zero or more sponsors, each of which usually want to see their logo on the web page.
     * 
     * @return the sponsors' logos; always non-<code>null</code> but possibly empty
     */
    Iterable<URL> getSponsorImageURLs();
    
    void addSponsorImageURL(URL sponsorImageURL);
    
    void removeSponsorImageURL(URL sponsorImageURL);
    
    /**
     * Replaces the {@link #getSponsorImageURLs() current contents of the sponsorship image URL sequence} by the image URLs in
     * <code>sponsorImageURLs</code>.
     * 
     * @param sponsorImageURLs
     *            if <code>null</code>, the internal sequence of sponsorship image URLs is cleared but remains valid (non-
     *            <code>null</code>)
     */
    void setSponsorImageURLs(Iterable<URL> sponsorImageURLs);
    
    /**
     * An optional logo image; may return <code>null</code>.
     */
    URL getLogoImageURL();
    
    void setLogoImageURL(URL logoImageURL);
    
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
    
    /**
     * @return the URL of the event's official web site, or <code>null</code> if such a site does not exist or its URL
     *         is not known
     */
    URL getOfficialWebsiteURL();
    
    void setOfficialWebsiteURL(URL officialWebsiteURL);

    Iterable<? extends LeaderboardGroupBase> getLeaderboardGroups();

    /**
     * For the images references by the image URLs in {@link #getImageURLs()}, {@link #getSponsorImageURLs()} and {@link #getLogoImageURL()}
     * determines the image dimensions.
     */
    ImageSize getImageSize(URL imageURL) throws InterruptedException, ExecutionException;
}

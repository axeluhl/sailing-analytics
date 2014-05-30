package com.sap.sailing.domain.base;

import java.net.URL;
import java.util.UUID;

import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.common.Renamable;
import com.sap.sailing.domain.common.WithID;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;

/**
 * An event is a group of {@link Regatta regattas} carried out at a common venue within a common time frame. For
 * example, Kiel Week 2011 is an event, and the International German Championship 2011 held, e.g., in Travem�nde, is an event,
 * too.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface Event extends Named, Renamable, WithID, EventBase {
    Iterable<Regatta> getRegattas();
    
    void addRegatta(Regatta regatta);
    
    void removeRegatta(Regatta regatta);
    
    /**
     * For events, the ID is always a UUID.
     */
    UUID getId();
    
    /**
     * Returns a non-<code>null</code> live but unmodifiable collection of leaderboard groups that were previously
     * {@link #addLeaderboardGroup(LeaderboardGroup) added} to this event, in the order of their addition. Therefore, to
     * change the iteration order, {@link #removeLeaderboardGroup(LeaderboardGroup)} and
     * {@link #addLeaderboardGroup(LeaderboardGroup)} need to be used.
     */
    Iterable<LeaderboardGroup> getLeaderboardGroups();
    
    void addLeaderboardGroup(LeaderboardGroup leaderboardGroup);
    
    /**
     * @return <code>true</code> if and only if a leaderboard group equal to <code>leaderboardGroup</code> was part of
     *         {@link #getLeaderboardGroups()} and therefore was actually removed
     */
    boolean removeLeaderboardGroup(LeaderboardGroup leaderboardGroup);

    /**
     * Replaces the {@link #getLeaderboardGroups() current contents of the leaderboard groups sequence} by the
     * leaderboard groups in <code>leaderboardGroups</code>.
     */
    void setLeaderboardGroups(Iterable<LeaderboardGroup> leaderboardGroups);
    
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

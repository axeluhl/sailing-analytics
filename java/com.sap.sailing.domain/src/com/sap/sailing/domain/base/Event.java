package com.sap.sailing.domain.base;

import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sse.common.media.ImageDescriptor;
import com.sap.sse.common.media.ImageSize;
import com.sap.sse.common.media.MediaUtils;

/**
 * An event is a group of {@link Regatta regattas} carried out at a common venue within a common time frame. For
 * example, Kiel Week 2011 is an event, and the International German Championship 2011 held, e.g., in Travem�nde, is an event,
 * too.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface Event extends EventBase {
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
    @Override
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
     * For the images references by the image URLs in {@link #getImageURLs()}, {@link #getSponsorImageURLs()} and {@link #getLogoImageURL()}
     * determines the image dimensions. The retrieval of the image size is started when the URL is added to this event, and the result is
     * cached. Should the image change its size afterwards or change its availability status, this event won't notice unless the image is
     * removed from this event and added again.<p>
     * 
     * Note that exceptions may result should the image be unavailable. If the image URL leads to a document that is not an image,
     * <code>null</code> will result.
     * 
     * @deprecated Use {@link MediaUtils#getImageDimensions(URL)} or simply use the dimensions given by {@link ImageDescriptor}
     */
    @Deprecated
    ImageSize getImageSize(URL imageURL) throws InterruptedException, ExecutionException;
    
}

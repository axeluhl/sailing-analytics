package com.sap.sailing.domain.base;

import java.util.UUID;

import com.sap.sailing.domain.leaderboard.LeaderboardGroup;

/**
 * An event is a group of {@link Regatta regattas} carried out at a common venue within a common time frame. For
 * example, Kiel Week 2011 is an event, and the International German Championship 2011 held, e.g., in Travem�nde, is an event,
 * too.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface Event extends EventBase {
    
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
     * An event may happen in the vicinity of one or more WindFinder (https://www.windfinder.com) weather
     * stations. Which ones those are can be defined using {@link #setWindFinderReviewedSpotsCollection(Iterable)},
     * and this getter returns the IDs last set.
     */
    Iterable<String> getWindFinderReviewedSpotsCollectionIds();

    /**
     * Set the IDs of the reviewed WindFinder spot collections to consider during this event.
     * Setting this to a non-empty value shall lead to a corresponding display of a WindFinder
     * logo / link on the event's UI representation.
     */
    void setWindFinderReviewedSpotsCollection(Iterable<String> reviewedSpotsCollectionIds);
}

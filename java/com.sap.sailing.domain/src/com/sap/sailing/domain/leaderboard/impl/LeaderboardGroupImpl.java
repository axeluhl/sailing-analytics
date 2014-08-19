package com.sap.sailing.domain.leaderboard.impl;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.sap.sailing.domain.base.impl.LeaderboardGroupBaseImpl;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.LeaderboardGroupListener;

public class LeaderboardGroupImpl extends LeaderboardGroupBaseImpl implements LeaderboardGroup {
    
    private static final long serialVersionUID = 2035927369446736934L;
    private boolean displayGroupsInReverseOrder;
    private final List<Leaderboard> leaderboards;
    private final Set<LeaderboardGroupListener> listeners;
    
    /**
     * An optional meta-leaderboard that shows overall results computed from the {@link #leaderboards} aggregated by
     * this group. The meta-leaderboard currently has no own state that requires any persistence but can be re-constructed
     * entirely from this leaderboard group.
     */
    private Leaderboard overallLeaderboard;

    /**
     * Creates a new leaderboard group with a new UUID as its ID.
     */
    public LeaderboardGroupImpl(String name, String description, String displayName,
            boolean displayGroupsInReverseOrder, List<? extends Leaderboard> leaderboards) {
        this(UUID.randomUUID(), name, description, displayName, displayGroupsInReverseOrder, leaderboards);
    }
    
    private synchronized void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
    }

    /**
     * Use this constructor when loading or deserializing a leaderboard group and the ID is known and is provided to the constructor.
     */
    public LeaderboardGroupImpl(UUID id, String name, String description, String displayName,
            boolean displayGroupsInReverseOrder, List<? extends Leaderboard> leaderboards) {
        super(id, name, description, displayName);
        this.displayGroupsInReverseOrder = displayGroupsInReverseOrder;
        this.leaderboards = new ArrayList<Leaderboard>(leaderboards);
        this.listeners = new HashSet<LeaderboardGroupListener>();
    }
    
    @Override
    public void addLeaderboardGroupListener(LeaderboardGroupListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeLeaderboardGroupListener(LeaderboardGroupListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    private Set<LeaderboardGroupListener> getLeaderboardGroupListeners() {
        synchronized (listeners) {
            return new HashSet<LeaderboardGroupListener>(listeners);
        }
    }
    
    private void notifyLeaderboardGroupListenersAboutLeaderboardAdded(Leaderboard leaderboard) {
        for (LeaderboardGroupListener listener : getLeaderboardGroupListeners()) {
            listener.leaderboardAdded(this, leaderboard);
        }
    }

    private void notifyLeaderboardGroupListenersAboutLeaderboardRemoved(Leaderboard leaderboard) {
        for (LeaderboardGroupListener listener : getLeaderboardGroupListeners()) {
            listener.leaderboardRemoved(this, leaderboard);
        }
    }

    @Override
    public Leaderboard getOverallLeaderboard() {
        return overallLeaderboard;
    }

    @Override
    public boolean hasOverallLeaderboard() {
        return getOverallLeaderboard() != null;
    }

    @Override
    public void setOverallLeaderboard(Leaderboard overallLeaderboard) {
        this.overallLeaderboard = overallLeaderboard;
    }

    @Override
    public synchronized Iterable<Leaderboard> getLeaderboards() {
        return new ArrayList<Leaderboard>(leaderboards);
    }

    @Override
    public int getIndexOf(Leaderboard leaderboard) {
        return leaderboards.indexOf(leaderboard);
    }

    @Override
    public synchronized void addLeaderboard(Leaderboard leaderboard) {
        addLeaderboardAt(leaderboard, leaderboards.size());
    }
    
    @Override
    public synchronized void addLeaderboardAt(Leaderboard leaderboard, int index) {
        leaderboards.add(index, leaderboard);
        notifyLeaderboardGroupListenersAboutLeaderboardAdded(leaderboard);
    }

    @Override
    public synchronized void addAllLeaderboards(Collection<Leaderboard> leaderboards) {
        for (Leaderboard leaderboard : leaderboards) {
            addLeaderboard(leaderboard);
        }
    }

    @Override
    public synchronized void removeLeaderboard(Leaderboard leaderboard) {
        leaderboards.remove(leaderboard);
        notifyLeaderboardGroupListenersAboutLeaderboardRemoved(leaderboard);
    }

    @Override
    public synchronized void removeAllLeaderboards(Collection<Leaderboard> leaderboards) {
        for (Leaderboard leaderboard : leaderboards) {
            removeLeaderboard(leaderboard);
        }
    }

    @Override
    public synchronized void clearLeaderboards() {
        for (Leaderboard leaderboard : getLeaderboards()) {
            removeLeaderboard(leaderboard);
        }
    }

    @Override
    public boolean isDisplayGroupsInReverseOrder() {
        return displayGroupsInReverseOrder;
    }
    
    public String toString() {
        return getName() + " " + getDescription();
    }
}

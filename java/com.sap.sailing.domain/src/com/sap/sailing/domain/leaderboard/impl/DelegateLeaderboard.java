package com.sap.sailing.domain.leaderboard.impl;

import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;

public class DelegateLeaderboard implements Serializable {
    private static final long serialVersionUID = -1764036951368356044L;

    private RegattaLeaderboard delegateLeaderboard;
    
    private transient final Supplier<RegattaLeaderboard> delegateLeaderboardSupplier;
    
    /**
     * The particular use case for which this field is introduced is registering score correction
     * listeners at a point in time when the full leaderboard hasn't been resolved yet. Instead of
     * letting this listener registration attempt the resolution without success the request can
     * be queued here, and each time the {@link #getDelegateLeaderboard()} successfully resolves a
     * leaderboard, all consumers in this set will be triggered.
     */
    private final ConcurrentHashMap<Consumer<RegattaLeaderboard>, Boolean> triggerWhenDelegateLeaderboardIsResolved;
    
    public DelegateLeaderboard(Supplier<RegattaLeaderboard> delegateLeaderboardSupplier) {
        this.delegateLeaderboardSupplier = delegateLeaderboardSupplier;
        this.triggerWhenDelegateLeaderboardIsResolved = new ConcurrentHashMap<>();
    }
    
    public RegattaLeaderboard getDelegateLeaderboard() {
        // TODO make thread-safe; if multiple threads call concurrently, triggers from triggerWhenFullLeaderboardIsResolved may fire several times
        if (delegateLeaderboard == null) {
            if (delegateLeaderboardSupplier == null) {
                throw new NullPointerException("Internal error: Regatta leaderboard supplier is null; this can only happen upon premature serialization");
            }
            delegateLeaderboard = delegateLeaderboardSupplier.get();
            if (delegateLeaderboard != null) {
                for (Iterator<Consumer<RegattaLeaderboard>> i=triggerWhenDelegateLeaderboardIsResolved.keySet().iterator(); i.hasNext(); ) {
                    final Consumer<RegattaLeaderboard> toTrigger = i.next();
                    toTrigger.accept(delegateLeaderboard);
                    i.remove();
                }
            }
        }
        return delegateLeaderboard;
    }

    public void scheduleLeaderboardConsumer(final Consumer<RegattaLeaderboard> consumer) {
        triggerWhenDelegateLeaderboardIsResolved.put(consumer, true);
    }
    
    /**
     * If the {@link #getDelegateLeaderboard() delegate leaderboard} is already available, the {@code action} is
     * run with it right away; otherwise it is scheduled and run the next time that {@link #getDelegateLeaderboard()}
     * is invoked such that it returns a valid leaderboard.
     */
    public void runOrSchedule(final Consumer<RegattaLeaderboard> action) {
        if (getDelegateLeaderboard() != null) {
            action.accept(getDelegateLeaderboard());
        } else {
            scheduleLeaderboardConsumer(action);
        }
    }
}

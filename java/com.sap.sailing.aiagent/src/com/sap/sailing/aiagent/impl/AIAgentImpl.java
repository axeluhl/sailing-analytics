package com.sap.sailing.aiagent.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.client.ClientProtocolException;
import org.apache.shiro.authz.AuthorizationException;
import org.json.simple.parser.ParseException;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.aiagent.interfaces.AIAgent;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.EventListener;
import com.sap.sailing.domain.common.dto.TagDTO;
import com.sap.sailing.domain.common.tagging.RaceLogNotFoundException;
import com.sap.sailing.domain.common.tagging.ServiceNotFoundException;
import com.sap.sailing.domain.common.tagging.TagAlreadyExistsException;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.LeaderboardGroupListener;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.TaggingService;
import com.sap.sse.aicore.AICore;
import com.sap.sse.aicore.ChatSession;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Triple;

public class AIAgentImpl implements AIAgent {
    private static final String SAP_AI_CORE_TAG = "SAP AI Core on %s";
    
    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;
    
    private final AICore aiCore;
    
    private final String modelName;
    
    private final String systemPrompt;
    
    private final ConcurrentMap<Leaderboard, RaceColumnListener> raceColumnListeners;
    
    private final ConcurrentMap<Event, EventListener> eventListeners;
    
    /**
     * To be accessed only through the {@code synchronized} methods {@link #lockRaceForCommenting(String, String, String)} and
     * {@link #unlockRaceAfterCommenting(String, String, String)}.
     */
    private final Map<Triple<String, String, String>, ReentrantLock> locks;

    public AIAgentImpl(ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker, AICore aiCore, String modelName, String systemPrompt) {
        super();
        this.modelName = modelName;
        this.systemPrompt = systemPrompt;
        this.raceColumnListeners = new ConcurrentHashMap<>();
        this.eventListeners = new ConcurrentHashMap<>();
        this.racingEventServiceTracker = racingEventServiceTracker;
        this.aiCore = aiCore;
        this.locks = new HashMap<>();
    }
    
    private RacingEventService getRacingEventService() {
        return racingEventServiceTracker.getService();
    }
    
    /**
     * Checks if a tag with the {@code tagIdentifier} is already found on the race identified by
     * {@code leaderboardName}, {@code raceColumnName} and {@code fleetName}; if not, the prompt is sent to a new chat
     * session created with the LLM identified by {@link #modelName}, and a new tag is added to that race using the
     * response received.
     * 
     * @param tagIdentifier
     *            used to check if there already is a tag with equal identifier; this is encoded in a tag's
     *            {@link TagDTO#getHiddenInfo() hidden info}.
     */
    public void produceCommentFromPrompt(String tag, final String prompt, final String leaderboardName,
            String raceColumnName, String fleetName, TimePoint raceTimepoint, String tagIdentifier)
            throws UnsupportedOperationException, ClientProtocolException, URISyntaxException, IOException,
            ParseException, RaceLogNotFoundException, ServiceNotFoundException {
        lockRaceForCommenting(leaderboardName, raceColumnName, fleetName);
        try {
            if (!getRacingEventService().getTaggingService().getTags(leaderboardName, raceColumnName, fleetName, /* searchSince */ null, /* returnRevokedTags */ false)
                    .stream().anyMatch(existingTag->Util.equalsWithNull(existingTag.getHiddenInfo(), tagIdentifier))) {
                final Optional<ChatSession> optionalChatSession = aiCore.createChatSession(modelName);
                optionalChatSession.map(chatSession->{
                    String response;
                    try {
                        response = chatSession
                            .addSystemPrompt(systemPrompt)
                            .addPrompt(prompt)
                            .submit();
                        getRacingEventService().getTaggingService().addTag(leaderboardName, raceColumnName, fleetName,
                                String.format(SAP_AI_CORE_TAG, tag), response, tagIdentifier,
                                "/images/AI_generated_R_blk.png", /* resizedImageURL */ null, /* visibleForPublic */ true, raceTimepoint);
                        return null;
                    } catch (AuthorizationException | IllegalArgumentException | RaceLogNotFoundException
                            | ServiceNotFoundException | TagAlreadyExistsException | UnsupportedOperationException
                            | URISyntaxException | IOException | ParseException e) {
                        throw new RuntimeException(e);
                    }
                }).orElseThrow(()->new FileNotFoundException("Language model named "+modelName+" not found"));
            }
        } finally {
            unlockRaceAfterCommenting(leaderboardName, raceColumnName, fleetName);
        }
    }
    
    /**
     * To be called before starting to check the {@link TaggingService} for the presence of a tag with a
     * {@link TagDTO#getHiddenInfo() hidden info / identifier} because some concurrent attempt to comment may just be in
     * between checking and updating the tags on that race.
     * <p>
     * 
     * Callers <em>must</em> make sure to call {@link #unlockRaceAfterCommenting(String, String, String)} in a
     * <tt>finally</tt> clause to avoid deadlocks at all cost.
     * <p>
     * 
     * If another thread currently holds the lock for the race identified by {@code leaderboardName},
     * {@code raceColumnName}, and {@code fleetName}, this method will block; when the other thread then
     * {@link #unlockRaceAfterCommenting(String, String, String) releases its lock}, one other thread
     * waiting for the lock will be unblocked, etc.
     */
    private synchronized void lockRaceForCommenting(final String leaderboardName, String raceColumnName, String fleetName) {
        final Triple<String, String, String> lockKey = getLockKey(leaderboardName, raceColumnName, fleetName);
        final ReentrantLock lock = locks.computeIfAbsent(lockKey, k->new ReentrantLock());
        lock.lock();
    }

    private Triple<String, String, String> getLockKey(final String leaderboardName, String raceColumnName,
            String fleetName) {
        return new Triple<>(leaderboardName, raceColumnName, fleetName);
    }
    
    private synchronized void unlockRaceAfterCommenting(final String leaderboardName, String raceColumnName, String fleetName) {
        final ReentrantLock lock = locks.remove(getLockKey(leaderboardName, raceColumnName, fleetName));
        lock.unlock();
    }
    
    @Override
    public void startCommentingOnEvent(final Event event) {
        final LeaderboardGroupListener lgListener = new LeaderboardGroupListener() {
            private static final long serialVersionUID = -8928697982534761135L;

            @Override
            public void leaderboardAdded(LeaderboardGroup group, Leaderboard leaderboard) {
                addNewRaceColumnListenerToLeaderboard(leaderboard);
            }

            @Override
            public void leaderboardRemoved(LeaderboardGroup group, Leaderboard leaderboard) {
                removeRaceColumnListenerFromLeaderboard(leaderboard);
            }
        };
        final EventListener eventListener = new EventListener() {
            @Override
            public void leaderboardGroupAdded(Event event, LeaderboardGroup leaderboardGroup) {
                for (final Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
                    if (leaderboard.isPartOfEvent(event)) {
                        addNewRaceColumnListenerToLeaderboard(leaderboard);
                    }
                }
                leaderboardGroup.addLeaderboardGroupListener(lgListener);
            }

            @Override
            public void leaderboardGroupRemoved(Event event, LeaderboardGroup leaderboardGroup) {
                leaderboardGroup.removeLeaderboardGroupListener(lgListener);
                for (final Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
                    removeRaceColumnListenerFromLeaderboard(leaderboard);
                }
            }
        };
        event.addEventListener(eventListener);
        eventListeners.put(event, eventListener);
        for (final Leaderboard leaderboard : event.getLeaderboards()) {
            addNewRaceColumnListenerToLeaderboard(leaderboard);
        }
    }

    private void addNewRaceColumnListenerToLeaderboard(final Leaderboard leaderboard) {
        final RaceColumnListener raceColumnListenerForLeaderboard = new RaceColumnListener(leaderboard, this);
        raceColumnListeners.put(leaderboard, raceColumnListenerForLeaderboard);
        leaderboard.addRaceColumnListener(raceColumnListenerForLeaderboard);
    }
    
    private void removeRaceColumnListenerFromLeaderboard(final Leaderboard leaderboard) {
        final RaceColumnListener listener = raceColumnListeners.remove(leaderboard);
        if (listener != null) {
            listener.removeListener();
        }
    }
    
    @Override
    public void stopCommentingOnEvent(Event event) {
        final EventListener eventListener = eventListeners.remove(event);
        if (eventListener != null) {
            event.removeEventListener(eventListener);
            for (final Leaderboard leaderboard : event.getLeaderboards()) {
                final RaceColumnListener raceColumnListener = raceColumnListeners.get(leaderboard);
                if (raceColumnListener != null) {
                    raceColumnListener.removeListener();
                }
            }
        }
    }
    
    @Override
    public void stopCommentingOnAllEvents() {
        for (final Event event : new HashSet<>(eventListeners.keySet())) {
            stopCommentingOnEvent(event);
        }
    }

    @Override
    public Iterable<Event> getCommentingOnEvents() {
        return Collections.unmodifiableCollection(eventListeners.keySet());
    }
}

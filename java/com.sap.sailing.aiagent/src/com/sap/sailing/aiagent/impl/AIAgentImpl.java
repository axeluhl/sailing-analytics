package com.sap.sailing.aiagent.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.http.client.ClientProtocolException;
import org.apache.shiro.authz.AuthorizationException;
import org.json.simple.parser.ParseException;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.aiagent.interfaces.AIAgent;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.EventListener;
import com.sap.sailing.domain.common.tagging.RaceLogNotFoundException;
import com.sap.sailing.domain.common.tagging.ServiceNotFoundException;
import com.sap.sailing.domain.common.tagging.TagAlreadyExistsException;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.LeaderboardGroupListener;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.aicore.AICore;
import com.sap.sse.aicore.ChatSession;
import com.sap.sse.common.TimePoint;

public class AIAgentImpl implements AIAgent {
    private static final String SAP_AI_CORE_TAG = "SAP AI Core on %s";
    
    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;
    
    private final AICore aiCore;
    
    private final String modelName;
    
    private final String systemPrompt;
    
    private final ConcurrentMap<Leaderboard, RaceColumnListener> raceColumnListeners;
    private final ConcurrentMap<Event, EventListener> eventListeners;

    public AIAgentImpl(ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker, AICore aiCore, String modelName, String systemPrompt) {
        super();
        this.modelName = modelName;
        this.systemPrompt = systemPrompt;
        raceColumnListeners = new ConcurrentHashMap<>();
        eventListeners = new ConcurrentHashMap<>();
        this.racingEventServiceTracker = racingEventServiceTracker;
        this.aiCore = aiCore;
    }
    
    private RacingEventService getRacingEventService() {
        return racingEventServiceTracker.getService();
    }
    
    public void produceCommentFromPrompt(String tag, final String prompt, final String leaderboardName,
            String raceColumnName, String fleetName, TimePoint raceTimepoint) throws UnsupportedOperationException, ClientProtocolException,
            URISyntaxException, IOException, ParseException {
        final Optional<ChatSession> optionalChatSession = aiCore.createChatSession(modelName);
        optionalChatSession.map(chatSession->{
            String response;
            try {
                response = chatSession
                    .addSystemPrompt(systemPrompt)
                    .addPrompt(prompt)
                    .submit();
                getRacingEventService().getTaggingService().addTag(leaderboardName, raceColumnName, fleetName,
                        String.format(SAP_AI_CORE_TAG, tag), response, "https://www.sapsailing.com/gwt/images/home/logo-small@2x.png",
                        /* resizedImageURL */ null, /* visibleForPublic */ true, raceTimepoint);
                return null;
            } catch (AuthorizationException | IllegalArgumentException | RaceLogNotFoundException
                    | ServiceNotFoundException | TagAlreadyExistsException | UnsupportedOperationException
                    | URISyntaxException | IOException | ParseException e) {
                throw new RuntimeException(e);
            }
        }).orElseThrow(()->new FileNotFoundException("Language model named "+modelName+" not found"));
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
}

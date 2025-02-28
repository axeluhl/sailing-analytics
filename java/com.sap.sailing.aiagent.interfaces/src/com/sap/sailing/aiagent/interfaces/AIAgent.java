package com.sap.sailing.aiagent.interfaces;

import com.sap.sailing.domain.base.Event;

public interface AIAgent {

    void startCommentingOnEvent(Event event);

    void stopCommentingOnEvent(Event event);

    void stopCommentingOnAllEvents();
    
    void addListener(AIAgentListener listener);
    
    void removeListener(AIAgentListener listener);
    
    Iterable<Event> getCommentingOnEvents();

}

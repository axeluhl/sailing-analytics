package com.sap.sailing.aiagent.interfaces;

import com.sap.sailing.domain.base.Event;

public interface AIAgentListener {
    void stoppedCommentingOnEvent(Event e);
    void startedCommentingOnEvent(Event e);
}

package com.sap.sse.gwt.client.messaging;

import com.google.gwt.core.client.EntryPoint;

/**
 * An entry point for embedding in an {@code iframe} element and to which to then send messages. The message listener is
 * described by subclasses as the result of the {@code #getMessageListener()} method. The listener will be attached to
 * the entry point's global window (what GWT returns as {@code $wnd}) and will receive messages posted there. When the
 * module has loaded and listener has been registered, a message with the string described by the constant
 * {@link #READY_TOKEN} is posted as a message to the entry point's parent window (stepping through the intermediate
 * implicit GWT iframe element) to indicate the listener's readiness.
 * <p>
 * 
 * @author Axel Uhl (d043530)
 *
 * @param <T>
 */
public abstract class AbstractMessagingEntryPoint<T> implements EntryPoint {
    public static final String READY_TOKEN = "I'm ready to rock";
    
    private MessagePort messagePort;
    
    @Override
    public void onModuleLoad() {
        messagePort = MessagePort.getGlobalWindow();
        messagePort.addMessageListener(getMessageListener());
        messagePort.getParentWindow().postMessage(READY_TOKEN, "*");
    }

    abstract protected MessageListener<T> getMessageListener();
}

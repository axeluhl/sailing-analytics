package com.sap.sse.jettyextensions;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

public class SynchronizedContextHandlerCollection extends ContextHandlerCollection {
    public SynchronizedContextHandlerCollection() {
        super();
    }

    @Override
    public synchronized void setHandlers(Handler[] handlers) {
        super.setHandlers(handlers);
    }

    @Override
    public synchronized void addHandler(Handler handler) {
        super.addHandler(handler);
    }

    @Override
    public synchronized void prependHandler(Handler handler) {
        super.prependHandler(handler);
    }

    @Override
    public synchronized void removeHandler(Handler handler) {
        super.removeHandler(handler);
    }
}

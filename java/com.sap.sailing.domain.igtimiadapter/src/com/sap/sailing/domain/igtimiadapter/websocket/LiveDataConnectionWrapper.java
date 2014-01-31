package com.sap.sailing.domain.igtimiadapter.websocket;

import com.sap.sailing.domain.igtimiadapter.BulkFixReceiver;
import com.sap.sailing.domain.igtimiadapter.LiveDataConnection;

public class LiveDataConnectionWrapper implements LiveDataConnection {
    private final LiveDataConnectionFactoryImpl factory;
    
    private final LiveDataConnection actualConnection;
    
    private boolean stopCalled;
    
    protected LiveDataConnectionWrapper(LiveDataConnectionFactoryImpl factory, LiveDataConnection actualConnection) {
        super();
        this.factory = factory;
        this.actualConnection = actualConnection;
    }

    @Override
    public synchronized void stop() throws Exception {
        if (!stopCalled) {
            factory.stop(actualConnection);
            stopCalled = true;
        }
    }

    @Override
    public boolean waitForConnection(long timeoutInMillis) throws InterruptedException {
        return actualConnection.waitForConnection(timeoutInMillis);
    }

    @Override
    public void addListener(BulkFixReceiver listener) {
        actualConnection.addListener(listener);
    }

}

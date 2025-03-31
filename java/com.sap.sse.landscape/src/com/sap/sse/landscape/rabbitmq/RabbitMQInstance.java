package com.sap.sse.landscape.rabbitmq;

import java.net.MalformedURLException;
import java.net.URL;

import com.sap.sse.landscape.Process;
import com.sap.sse.landscape.RotatingFileBasedLog;

public interface RabbitMQInstance extends Process<RotatingFileBasedLog, RabbitMQMetrics>, RabbitMQEndpoint {
    default int getPort() {
        return DEFAULT_PORT;
    }
    
    default URL getAdminURL() throws MalformedURLException {
        return new URL("http", getHost().getPublicAddress().getCanonicalHostName(), 10000+getPort(), "/");
    }
}

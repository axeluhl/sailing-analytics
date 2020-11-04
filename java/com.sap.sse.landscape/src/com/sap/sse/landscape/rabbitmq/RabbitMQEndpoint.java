package com.sap.sse.landscape.rabbitmq;

/**
 * Shall allow a client to connect to a RabbitMQ service which may or may not be replicated. For now, we assume that the
 * connectivity information required consists of a port and a hostname which in Rabbit / Erlang terminology may be
 * called a "node name."
 * <p>
 * 
 * The interface can be implemented easily by a lambda providing the {@link #getNodeName() node name}, such as
 * {@code rabbit.internal.sapsailing.com} because the {@link #getPort()} method is defaulted to return the default
 * port {@code 5672}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
@FunctionalInterface
public interface RabbitMQEndpoint {
    int DEFAULT_PORT = 5672;
    
    default int getPort() {
        return DEFAULT_PORT;
    }
    
    String getNodeName();
}

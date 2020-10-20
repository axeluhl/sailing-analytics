package com.sap.sse.landscape.rabbitmq;

import java.util.Arrays;

/**
 * Shall allow a client to connect to a RabbitMQ service which may or may not be replicated. For now, we assume that the
 * connectivity information required consists of a port and a hostname which in Rabbit / Erlang terminology may be
 * called a "node name."
 * <p>
 * 
 * The interface can be implemented easily by a lambda providing the {@link #getNodeName() node name}, such as
 * {@code rabbit.internal.sapsailing.com}.
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
    
    /**
     * Renders the RabbitMQ configuration as a set of variable assignments usable in either an {@code env.sh} file or in the
     * AWS EC2 instance user data (which eventually get appended to an {@code env.sh} file).
     */
    default Iterable<String> getUserData() {
        return Arrays.asList("REPLICATION_HOST="+getNodeName(), "REPLICATE_MASTER_QUEUE_PORT="+getPort());
    }
}

package com.sap.sse.landscape.rabbitmq;

/**
 * Shall allow a client to connect to a RabbitMQ service which may or may not be replicated. For now, we assume that the
 * connectivity information required consists of a port and a hostname which in Rabbit / Erlang terminology may be
 * called a "node name."
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface RabbitMQEndpoint {
    int DEFAULT_PORT = 5672;
    
    int getPort();
    
    String getNodeName();
}

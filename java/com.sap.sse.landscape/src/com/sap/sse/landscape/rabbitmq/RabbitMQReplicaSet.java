package com.sap.sse.landscape.rabbitmq;

public interface RabbitMQReplicaSet extends RabbitMQEndpoint {
    Iterable<RabbitMQInstance> getInstances();
    
    Iterable<Exchange> getExchanges();
    
    Iterable<Queue> getQueues();
}

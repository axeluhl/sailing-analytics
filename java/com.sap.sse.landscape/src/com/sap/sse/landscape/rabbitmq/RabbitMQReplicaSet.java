package com.sap.sse.landscape.rabbitmq;

public interface RabbitMQReplicaSet {
    Iterable<RabbitMQInstance> getInstances();
    
    Iterable<Exchange> getExchanges();
    
    Iterable<Queue> getQueues();
}

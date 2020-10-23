package com.sap.sse.landscape.rabbitmq;

import com.sap.sse.landscape.Process;
import com.sap.sse.landscape.RotatingFileBasedLog;

public interface RabbitMQInstance extends Process<RotatingFileBasedLog, RabbitMQMetrics> {

}

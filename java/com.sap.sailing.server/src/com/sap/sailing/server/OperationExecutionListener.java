package com.sap.sailing.server;

public interface OperationExecutionListener {
    <T> void executed(RacingEventServiceOperation<T> operation);
}

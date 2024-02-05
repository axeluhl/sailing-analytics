package com.sap.sse.metering;

public interface HasCPUMeter extends HasExecutorWithCPUMeter {
    CPUMeter getCPUMeter();

    @Override
    default ExecutorWithCPUMeter getExecutorWithCPUMeter() {
        return getCPUMeter();
    }
}

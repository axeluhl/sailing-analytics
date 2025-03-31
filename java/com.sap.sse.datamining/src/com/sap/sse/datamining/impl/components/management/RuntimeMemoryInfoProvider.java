package com.sap.sse.datamining.impl.components.management;

import com.sap.sse.datamining.components.management.MemoryInfoProvider;

public class RuntimeMemoryInfoProvider implements MemoryInfoProvider {
    
    private Runtime runtime;

    public RuntimeMemoryInfoProvider(Runtime runtime) {
        this.runtime = runtime;
    }

    @Override
    public long freeMemory() {
        return runtime.freeMemory();
    }

    @Override
    public long totalMemory() {
        return runtime.totalMemory();
    }

}

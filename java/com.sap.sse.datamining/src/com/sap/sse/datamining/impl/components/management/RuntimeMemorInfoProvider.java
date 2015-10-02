package com.sap.sse.datamining.impl.components.management;

import com.sap.sse.datamining.components.management.MemoryInfoProvider;

public class RuntimeMemorInfoProvider implements MemoryInfoProvider {
    
    private Runtime runtime;

    public RuntimeMemorInfoProvider(Runtime runtime) {
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

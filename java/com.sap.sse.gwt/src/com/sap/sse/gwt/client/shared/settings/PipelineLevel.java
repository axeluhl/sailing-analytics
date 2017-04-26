package com.sap.sse.gwt.client.shared.settings;

import java.util.LinkedList;
import java.util.List;

public enum PipelineLevel {
    SYSTEM_DEFAULTS, GLOBAL_DEFAULTS, CONTEXT_SPECIFIC_DEFAULTS;
    
    public List<PipelineLevel> getSortedLevelsUntilCurrent() {
        List<PipelineLevel> levels = new LinkedList<>();
        switch(this) {
        case CONTEXT_SPECIFIC_DEFAULTS:
            levels.add(0, CONTEXT_SPECIFIC_DEFAULTS);
        case GLOBAL_DEFAULTS:
            levels.add(0, GLOBAL_DEFAULTS);
        case SYSTEM_DEFAULTS:
            levels.add(0, SYSTEM_DEFAULTS);
        }
        return levels;
    }
}
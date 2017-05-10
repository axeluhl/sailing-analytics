package com.sap.sse.gwt.client.shared.settings;

import java.util.LinkedList;
import java.util.List;

public enum PipelineLevel {
    SYSTEM_DEFAULTS, USER_DEFAULTS, DOCUMENT_DEFAULTS;

    public List<PipelineLevel> getSortedLevelsUntilCurrent() {
        List<PipelineLevel> levels = new LinkedList<>();
        switch (this) {
        case DOCUMENT_DEFAULTS:
            levels.add(0, DOCUMENT_DEFAULTS);
        case USER_DEFAULTS:
            levels.add(0, USER_DEFAULTS);
        case SYSTEM_DEFAULTS:
            levels.add(0, SYSTEM_DEFAULTS);
        }
        return levels;
    }
}
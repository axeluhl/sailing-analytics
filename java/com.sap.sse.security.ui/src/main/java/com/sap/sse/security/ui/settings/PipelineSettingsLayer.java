package com.sap.sse.security.ui.settings;

import com.sap.sse.gwt.client.shared.settings.PipelineLevel;

public enum PipelineSettingsLayer {
    AFTER_SYSTEM_DEFAULTS, AFTER_USER_DEFAULTS, AFTER_DOCUMENT_DEFAULTS;
    
    public PipelineLevel getPipelineLoadingPatchLevel() {
        switch(this) {
        case AFTER_DOCUMENT_DEFAULTS:
            return PipelineLevel.DOCUMENT_DEFAULTS;
        case AFTER_USER_DEFAULTS:
            return PipelineLevel.USER_DEFAULTS;
        case AFTER_SYSTEM_DEFAULTS:
            return PipelineLevel.SYSTEM_DEFAULTS;
        }
        throw new IllegalStateException("Unimplemented decision path for " + this.toString());
    }
    
    public PipelineLevel getPipelineStoringPatchLevel() {
        switch(this) {
        case AFTER_DOCUMENT_DEFAULTS:
            //after document defaults no settings are stored
            return null;
        case AFTER_USER_DEFAULTS:
            return PipelineLevel.DOCUMENT_DEFAULTS;
        case AFTER_SYSTEM_DEFAULTS:
            return PipelineLevel.SYSTEM_DEFAULTS;
        }
        throw new IllegalStateException("Unimplemented decision path for " + this.toString());
    }
}

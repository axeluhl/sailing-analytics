package com.sap.sailing.gwt.ui.shared.general;

import com.google.gwt.thirdparty.guava.common.annotations.GwtCompatible;
import com.sap.sailing.gwt.ui.client.StringMessages;

public enum LabelType {
    NONE(null) {
        @Override
        public String getLabel() {
            return null;
        }
    },
    LIVE("live") {
        @Override
        public String getLabel() {
            return StringMessages.INSTANCE.live();
        }
    },
    FINISHED("finished") {
        @Override
        public String getLabel() {
            return StringMessages.INSTANCE.finished();
        }
    },
    PROGRESS("progress") {
        @Override
        public String getLabel() {
//            return StringMessages.INSTANCE.;
            return null;
        }
    },
    UPCOMMING("upcomming") {
        @Override
        public String getLabel() {
//            return StringMessages.INSTANCE.;
            return null;
        }
    },
    NEW_("new") {
        @Override
        public String getLabel() {
//            return StringMessages.INSTANCE.;
            return null;
        }
    },
    UPDATED("updated") {
        @Override
        public String getLabel() {
//            return StringMessages.INSTANCE.;
            return null;
        }
    },
    ESS("ess") {
        @Override
        public String getLabel() {
//            return StringMessages.INSTANCE.;
            return null;
        }
    },
    WORLDCUP("worldcup") {
        @Override
        public String getLabel() {
//            return StringMessages.INSTANCE.;
            return null;
        }
    },
    BUNDESLIGA("bundesliga") {
        @Override
        public String getLabel() {
//            return StringMessages.INSTANCE.;
            return null;
        }
    };
    
    private final String labelType;
    
    private LabelType(String labelType) {
        this.labelType = labelType;
    }
    
    @GwtCompatible
    public abstract String getLabel();

    public String getLabelType() {
        return labelType;
    }
}

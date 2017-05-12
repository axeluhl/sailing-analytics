package com.sap.sailing.domain.common;

import com.sap.sailing.domain.common.i18n.CommonStringMessages;

public enum DataImportSubProgress {
    
    INIT {
        @Override
        public String getMessage(CommonStringMessages messages) {
            return messages.init();
        }
    },
    CONNECTION_SETUP {
        @Override
        public String getMessage(CommonStringMessages messages) {
            return messages.connectionSetup();
        }
    },
    CONNECTION_ESTABLISH {
        @Override
        public String getMessage(CommonStringMessages messages) {
            return messages.connectionEstablish();
        }
    },
    TRANSFER_STARTED {
        @Override
        public String getMessage(CommonStringMessages messages) {
            return messages.transferStarted();
        }
    },
    TRANSFER_COMPLETED {
        @Override
        public String getMessage(CommonStringMessages messages) {
            return messages.transferCompleted();
        }
    },
    IMPORT_INIT {
        @Override
        public String getMessage(CommonStringMessages messages) {
            return messages.importInit();
        }
    },
    IMPORT_WAIT {
        @Override
        public String getMessage(CommonStringMessages messages) {
            return messages.importWait();
        }
    },
    IMPORT_LEADERBOARD_GROUPS {
        @Override
        public String getMessage(CommonStringMessages messages) {
            return messages.importLeaderboardGroups();
        }
    },
    IMPORT_WIND_TRACKS {
        @Override
        public String getMessage(CommonStringMessages messages) {
            return messages.importWindTracks();
        }
    },
    IMPORT_SENSOR_FIXES {
        @Override
        public String getMessage(CommonStringMessages messages) {
            return messages.importSensorFixes();
        }
    },
    UPDATE_EVENT_LEADERBOARD_GROUP_LINKS {
        @Override
        public String getMessage(CommonStringMessages messages) {
            return messages.updateEventLeaderboardGroupLinks();
        }
    };
    
    public abstract String getMessage(CommonStringMessages messages);

}

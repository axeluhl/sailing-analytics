package com.sap.sailing.domain.common;

import java.io.Serializable;
import java.util.UUID;

public interface DataImportProgress extends Serializable {
    
    /**
     * Helper for i18n of import sub progresses messages. A method according to the messageKey is required
     * in com.sap.sailing.gwt.ui.masterdataimport.MasterDataImportSubProgressMessages interface.
     */
    enum SubProgress {
        CONNECTION_SETUP("connectionSetup"),
        CONNECTION_ESTABLISH("connectionEstablish"),
        TRANSFER_STARTED("transferStarted"),
        TRANSFER_COMPLETED("transferCompleted"),
        IMPORT_WAIT("importWait"),
        IMPORT_LEADERBOARD_GROUPS("importLeaderboardGroups"),
        IMPORT_WIND_TRACKS("importWindTracks"),
        UPDATE_EVENT_LEADERBOARD_GROUP_LINKS("updateEventLeaderboardGroupLinks");
        
        private final String messageKey;
        
        private SubProgress(String messageKey) {
            this.messageKey = messageKey;
        }
        
        public String getMessageKey() {
            return messageKey;
        }
    }
    
    double getOverallProgressPct();

    void setOverAllProgressPct(double pct);

    String getNameOfCurrentSubProgress();

    void setNameOfCurrentSubProgress(String name);

    double getCurrentSubProgressPct();

    void setCurrentSubProgressPct(double pct);

    /**
     * 
     * @return the result if the operation id done, null otherwise
     */
    MasterDataImportObjectCreationCount getResult();

    void setResult(MasterDataImportObjectCreationCount result);

    UUID getOperationId();

    boolean failed();

    void setFailed();

    String getErrorMessage();

    void setErrorMessage(String message);

}

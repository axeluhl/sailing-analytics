package com.sap.sailing.domain.common;

import java.io.Serializable;

public interface MasterDataImportObjectCreationCount extends Serializable{

    public abstract int getLeaderboardCount();

    public abstract int getLeaderboardGroupCount();

    public abstract int getEventCount();

    public abstract int getRegattaCount();

}
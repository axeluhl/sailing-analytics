package com.sap.sailing.gwt.ui.masterdataimport;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.ConstantsWithLookup;

public interface MasterDataImportSubProgressMessages extends ConstantsWithLookup {
    
    public static final MasterDataImportSubProgressMessages INSTANCE = GWT.create(MasterDataImportSubProgressMessages.class);
    
    String connectionSetup();
    String connectionEstablish();
    String transferStarted();
    String transferCompleted();
    String importInit();
    String importWait();
    String importLeaderboardGroups();
    String importWindTracks();
    String updateEventLeaderboardGroupLinks();

}

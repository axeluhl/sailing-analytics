package com.sap.sailing.landscape;

import java.net.MalformedURLException;
import java.net.URL;

import com.sap.sse.common.Named;
import com.sap.sse.common.TimePoint;

public interface Release extends Named {
    String RELEASE_NOTES_FILE_NAME = "release-notes.txt";
    String ARCHIVE_EXTENSION = ".tar.gz";
    
    ReleaseRepository getRepository();
    
    String getBaseName();
    
    TimePoint getCreationDate();
    
    default String getFolderURL() {
        return getRepository().getRepositoryBase()+"/"+getName()+"/";
    }
    
    default URL getReleaseNotesURL() throws MalformedURLException {
        return new URL(getFolderURL()+RELEASE_NOTES_FILE_NAME);
    }
    
    default URL getDeployableArchiveURL() throws MalformedURLException {
        return new URL(getFolderURL()+getName()+ARCHIVE_EXTENSION);
    }
}

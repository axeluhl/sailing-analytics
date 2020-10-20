package com.sap.sse.landscape;

import java.net.MalformedURLException;
import java.net.URL;

import com.sap.sse.common.Named;
import com.sap.sse.common.TimePoint;

/**
 * Obtain from a {@link ReleaseRepository}. A release has a name that is composed of a base name and a time stamp.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface Release extends Named {
    /**
     * The user data variable to use to specify the release to install and run on the host. See also
     * {@link ReleaseRepository} and {@link #getReleaseUserData}.
     */
    String INSTALL_FROM_RELEASE = "INSTALL_FROM_RELEASE";
    
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
    
    /**
     * @return the user data (or {@code env.sh} line) required to force the server process to use this release
     */
    Iterable<String> getUserData();
}

package com.sap.sailing.server.trackfiles;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.server.trackfiles.impl.ImportImpl;

/**
 * Import tracks from standard file formats into the Sailing Analytics Domain Model.
 * 
 * @author Fredrik Teschke
 * 
 */
public interface Import {
    Import INSTANCE = new ImportImpl();

    public List<String> getTrackNames(InputStream in) throws IOException;

    public Map<String, List<GPSFixMoving>> extractTracks(InputStream in) throws IOException;
}

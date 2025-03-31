package com.sap.sailing.domain.trackfiles;

import java.util.UUID;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sse.common.TimePoint;

/**
 * Identifies a device that generated fixes imported from a track file, such as a GPX file.
 * In addition to containing a unique {@link UUID} for identification purposes, the identifier
 * preserves the original file name, and (if present), also the name of the track for that fix.
 * 
 * @author Fredrik Teschke
 *
 */
public interface TrackFileImportDeviceIdentifier extends DeviceIdentifier {
    public static final String TYPE = "FILE";
    UUID getId();
    String getFileName();
    String getTrackName();
    TimePoint getUploadedAt();
}

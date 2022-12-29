package com.sap.sailing.domain.yellowbrickadapter.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifierImpl;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.domain.trackimport.GPSFixImporter;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

public class YellowBrickGPSFixImporter implements GPSFixImporter {
    private final ConcurrentMap<Pair<Number, String>, TrackFileImportDeviceIdentifier> deviceIdentifiersBySerialAndSourceName = new ConcurrentHashMap<>();
    
    @Override
    public boolean importFixes(InputStream inputStream, Charset charset, Callback callback,
            boolean inferSpeedAndBearing, String sourceName) throws FormatNotSupportedException, IOException, ParseException {
        final GetPositionsParser parser = new GetPositionsParser();
        final PositionsDocument positionsDocument = parser.parse(new InputStreamReader(inputStream, "UTF-8"), inferSpeedAndBearing);
        final String raceName = positionsDocument.getRaceUrl();
        for (final TeamPositions teamPositions : positionsDocument.getTeams()) {
            final String boatName = teamPositions.getCompetitorName();
            final int serialOfDevice = teamPositions.getDeviceSerialNumber();
            for (final TeamPosition boatFixObject : teamPositions.getPositions()) {
                final GPSFixMoving nextFix = boatFixObject.getGPSFixMoving();
                final TrackFileImportDeviceIdentifier deviceIdentifier = getDeviceIdentifier(sourceName, raceName, boatName, serialOfDevice);
                callback.addFix(nextFix, deviceIdentifier);
            }
        }
        return true;
    }

    private TrackFileImportDeviceIdentifier getDeviceIdentifier(String sourceName, String raceName, String boatName, Number serialOfDevice) {
        final Pair<Number, String> key = new Pair<>(serialOfDevice, raceName);
        return deviceIdentifiersBySerialAndSourceName.computeIfAbsent(key,
                k -> new TrackFileImportDeviceIdentifierImpl(UUID.randomUUID(), sourceName,
                        boatName + "-" + raceName + "-" + serialOfDevice, TimePoint.now()));
    }

    @Override
    public Iterable<String> getSupportedFileExtensions() {
        return Collections.singleton("json");
    }

    @Override
    public String getType() {
        return "YellowBrick GPS Fix Importer";
    }

}

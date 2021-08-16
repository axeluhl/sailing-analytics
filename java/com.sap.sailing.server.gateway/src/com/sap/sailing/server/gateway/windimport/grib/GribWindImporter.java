package com.sap.sailing.server.gateway.windimport.grib;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.common.impl.WindSourceWithAdditionalID;
import com.sap.sailing.grib.GribWindField;
import com.sap.sailing.grib.GribWindFieldFactory;
import com.sap.sailing.server.gateway.windimport.AbstractWindImporter;
import com.sap.sse.common.Util;

public class GribWindImporter extends AbstractWindImporter {
    private static final Logger logger = Logger.getLogger(GribWindImporter.class.getName());

    @Override
    protected WindSource getDefaultWindSource(UploadRequest uploadRequest) {
        WindSource windSource;
        if (uploadRequest.boatId == null) {
            windSource = new WindSourceImpl(WindSourceType.WEB);
        } else {
            windSource = new WindSourceWithAdditionalID(WindSourceType.WEB, uploadRequest.boatId);
        }
        return windSource;
    }

    @Override
    protected Map<WindSource, Iterable<Wind>> importWind(WindSource defaultWindSource, Map<InputStream, String> inputStreamsAndFilenames) throws IOException {
        final GribWindField windField = GribWindFieldFactory.INSTANCE.createGribWindFieldFromStreams(logger, Level.INFO, inputStreamsAndFilenames);
        final Map<WindSource, Iterable<Wind>> result = new HashMap<>();
        final Map<Position, WindSource> windSourcesByPosition = new HashMap<>();
        final Map<WindSource, Set<Wind>> writableResult = new HashMap<>();
        for (final Wind windFix : windField.getAllWindFixes()) {
            final WindSource windSourceForFix = getOrCreateWindSourceForPosition(windSourcesByPosition, windFix.getPosition(), defaultWindSource);
            Util.addToValueSet(writableResult, windSourceForFix, windFix);
        }
        result.putAll(writableResult);
        return result;
    }

    private WindSource getOrCreateWindSourceForPosition(Map<Position, WindSource> windSourcesByPosition, Position position, WindSource defaultWindSource) {
        return windSourcesByPosition.computeIfAbsent(position, p->
            new WindSourceWithAdditionalID(defaultWindSource.getType(),
                    defaultWindSource.getId()==null ? p.toString() : defaultWindSource.getId()+"@"+p.toString()));
    }
}

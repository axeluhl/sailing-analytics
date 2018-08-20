package com.sap.sailing.server.gateway.windimport.grib;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.common.impl.WindSourceWithAdditionalID;
import com.sap.sailing.grib.GribWindField;
import com.sap.sailing.grib.GribWindFieldFactory;
import com.sap.sailing.server.gateway.windimport.AbstractWindImporter;

public class GribWindImporter extends AbstractWindImporter {
    private static final Logger logger = Logger.getLogger(GribWindImporter.class.getName());

    @Override
    protected WindSource getWindSource(UploadRequest uploadRequest) {
        WindSource windSource;
        if (uploadRequest.boatId == null) {
            windSource = new WindSourceImpl(WindSourceType.WEB);
        } else {
            windSource = new WindSourceWithAdditionalID(WindSourceType.WEB, uploadRequest.boatId);
        }
        return windSource;
    }

    @Override
    protected Iterable<Wind> importWind(Map<InputStream, String> inputStreamsAndFilenames) throws IOException {
        final GribWindField windField = GribWindFieldFactory.INSTANCE.createGribWindFieldFromStreams(logger, Level.INFO, inputStreamsAndFilenames);
        return windField.getAllWindFixes();
    }
}

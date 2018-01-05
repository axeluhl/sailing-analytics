package com.sap.sailing.server.gateway.windimport.expedition;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.common.impl.WindSourceWithAdditionalID;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.server.gateway.windimport.AbstractWindImporter;
import com.sap.sailing.server.trackfiles.impl.CompressedStreamsUtil;
import com.sap.sailing.server.trackfiles.impl.ExpeditionImportFileHandler;
import com.sap.sse.common.Util;

public class WindImporter extends AbstractWindImporter {

    @Override
    protected WindSource getWindSource(UploadRequest uploadRequest) {
        WindSource windSource;
        if (uploadRequest.boatId == null) {
            windSource = new WindSourceImpl(WindSourceType.EXPEDITION);
        } else {
            windSource = new WindSourceWithAdditionalID(WindSourceType.EXPEDITION, uploadRequest.boatId);
        }
        return windSource;
    }

    @Override
    protected Iterable<Wind> importWind(Map<InputStream, String> streamsWithFilenames) throws IOException, FormatNotSupportedException {
        final List<Wind> result = new ArrayList<>();
        for (final Map.Entry<InputStream, String> entry : streamsWithFilenames.entrySet()) {
            CompressedStreamsUtil.handlePotentiallyCompressedFiles(entry.getValue(), entry.getKey(), new ExpeditionImportFileHandler() {
                @Override
                protected void handleExpeditionFile(String fileName, InputStream inputStream) throws IOException {
                    Util.addAll(WindLogParser.importWind(inputStream), result);
                }
            });
        }
        return result;
    }
}

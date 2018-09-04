package com.sap.sailing.windestimation.data.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.polars.impl.PolarDataServiceImpl;
import com.sap.sailing.windestimation.data.importer.PolarDataImporter;

public class PolarDataServiceAccessUtil {

    private PolarDataServiceAccessUtil() {
    }

    public static PolarDataService getPersistedPolarService()
            throws MalformedURLException, IOException, InterruptedException, ClassNotFoundException {
        PolarDataServiceImpl polarService = new PolarDataServiceImpl();
        polarService.clearReplicaState();
        FileInputStream fileInputStream = new FileInputStream(new File(PolarDataImporter.polarDataFilePath));
        polarService.initiallyFillFrom(fileInputStream);
        return polarService;
    }

}

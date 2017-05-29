package com.sap.sailing.grib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Formatter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.grib.impl.GribWindFieldFactoryImpl;

import ucar.nc2.ft.FeatureDataset;

/**
 * Factory interface used to create objects of type {@link GribWindField} based on a {@link FeatureDataset}.
 * 
 * Example Usage:
 * <pre>
 *      final Formatter errorLog = createLogFormatter(logger, Level.INFO);
 *      final ucar.nc2.util.CancelTask task = null;
 *      final String location = "resources/globalMarineNetCroatia.grb.bz2"; // or could be some URI / URL from where to retrieve it
 *      final FeatureDataset dataSet = FeatureDatasetFactoryManager.open(FeatureType.ANY, location, task, errorLog);
 *      final GribWindField windField = GribWindFieldFactory.INSTANCE.createGribWindField(dataSet);
 * </pre>
 * 
 * Or:
 * <pre>
 *      final GribWindField windField = GribWindFieldFactory.INSTANCE.createGribWindField(new File("/this/is/the/path/to/my/grib/file.grb"));
 * </pre>
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface GribWindFieldFactory {
    GribWindFieldFactory INSTANCE = new GribWindFieldFactoryImpl();
    
    GribWindField createGribWindField(FeatureDataset... dataSet);

    GribWindField createGribWindField(Iterable<String> locations) throws IOException;

    GribWindField createGribWindField(Logger logger, Level level, Iterable<String> locations) throws IOException;
    
    GribWindField createGribWindField(Formatter errorLog, Iterable<String> locations) throws IOException;

    GribWindField createGribWindFieldFromFiles(Iterable<File> files) throws IOException;
    
    GribWindField createGribWindFieldFromFiles(Formatter errorLog, Iterable<File> files) throws IOException;

    GribWindField createGribWindFieldFromFiles(Logger logger, Level level, Iterable<File> files) throws IOException;

    /**
     * Requires that the $TMP part of the file system can be accessed for writing because the files will temporarily be
     * stored there.
     */
    GribWindField createGribWindFieldFromStreams(Map<InputStream, String> inputStreamsAndFilenames) throws IOException;

    /**
     * Requires that the $TMP part of the file system can be accessed for writing because the files will temporarily be
     * stored there.
     */
    GribWindField createGribWindFieldFromStreams(Formatter errorLog, Map<InputStream, String> inputStreamsAndFilenames) throws IOException;

    /**
     * Requires that the $TMP part of the file system can be accessed for writing because the files will temporarily be
     * stored there.
     */
    GribWindField createGribWindFieldFromStreams(Logger logger, Level level, Map<InputStream, String> inputStreamsAndFilenames) throws IOException;

    /**
     * Creates a {@link Formatter} that writes its messages to the {@code logger} using the log {@code level} provided.
     * Use this method to create formatters for the {@code createGribWindField...} methods that require one.
     */
    Formatter createLogFormatter(Logger logger, Level level);
    
    /**
     * To be called when the bundle shuts down; all resources can be released now, and all persistent file
     * caches can be cleaned.
     */
    void shutdown();

}

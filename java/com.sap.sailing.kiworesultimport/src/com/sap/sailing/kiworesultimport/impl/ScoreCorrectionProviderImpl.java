package com.sap.sailing.kiworesultimport.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.kiworesultimport.ParserFactory;
import com.sap.sailing.kiworesultimport.RegattaSummary;
import com.sap.sailing.kiworesultimport.ZipFile;
import com.sap.sailing.kiworesultimport.ZipFileParser;

public class ScoreCorrectionProviderImpl implements ScoreCorrectionProvider {
    private static final long serialVersionUID = -4596215011753860781L;

    private static final String name = "Kieler Woche Official Results by b+m";
    
    /**
     * The directory that will be scanned for <code>.zip</code> files which will then be passed to
     * {@link ZipFileParser} for analysis.
     */
    private final File scanDir;
    
    public ScoreCorrectionProviderImpl(File scanDir) {
        super();
        if (!scanDir.isDirectory()) {
            throw new IllegalArgumentException("scanDir "+scanDir+" must be a directory");
        }
        this.scanDir = scanDir;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, Set<Pair<String, TimePoint>>> getHasResultsForBoatClassFromDateByEventName()
            throws IOException, SAXException, ParserConfigurationException {
        Map<String, Set<Pair<String, TimePoint>>> result = new HashMap<String, Set<Pair<String,TimePoint>>>();
        ZipFileParser zipFileParser = ParserFactory.INSTANCE.createZipFileParser();
        for (File file : scanDir.listFiles()) {
            String eventName = null;
            if (file.getName().toLowerCase().endsWith(".zip")) {
                ZipFile zipFile = zipFileParser.parse(new FileInputStream(file));
                Set<Pair<String, TimePoint>> resultTimesForBoatClassNames = new HashSet<Pair<String, TimePoint>>();
                for (RegattaSummary regattaSummary : zipFile.getRegattaSummaries()) {
                    resultTimesForBoatClassNames.add(new Pair<String, TimePoint>(regattaSummary.getBoatClassName(),
                            regattaSummary.getTimePointPublished()));
                    eventName = regattaSummary.getEventName();
                }
                if (eventName != null) {
                    result.put(eventName, resultTimesForBoatClassNames);
                }
            }
        }
        return result;
    }

}

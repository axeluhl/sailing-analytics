package com.sap.sailing.winregatta.resultimport.impl;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.resultimport.RegattaResults;
import com.sap.sailing.resultimport.UrlResultProvider;
import com.sap.sailing.resultimport.impl.RegattaScoreCorrectionsImpl;

public class ScoreCorrectionProviderImpl implements ScoreCorrectionProvider, UrlResultProvider {
	private static final long serialVersionUID = -5501186796881875686L;

	private static final String name = "'WinRegatta Plus' XLS Score Importer";

    private final Set<URL> allUrls;
    
    private String sheetName = "Erg_Drachen";

    public ScoreCorrectionProviderImpl() throws MalformedURLException {
        allUrls = new HashSet<URL>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, Set<Pair<String, TimePoint>>> getHasResultsForBoatClassFromDateByEventName() {
        Map<String, Set<Pair<String, TimePoint>>> result = new HashMap<String, Set<Pair<String,TimePoint>>>();
    	CompetitorResultsXlsImporter importer = new CompetitorResultsXlsImporter();
        for (URL url : getAllUrls()) {
            URLConnection conn;
            try {
                conn = url.openConnection();
                TimePoint lastModified = new MillisecondsTimePoint(conn.getLastModified());
                RegattaResults regattaResult = importer.getRegattaResults((InputStream) conn.getContent(),
                		CompetitorResultsXlsImporter.IMPORT_TEMPLATE_WITHOUT_RANKS_DRACHEN, sheetName);
                final String boatClassName = getBoatClassName(regattaResult);
                result.put(boatClassName, Collections.singleton(new Pair<String, TimePoint>(boatClassName, lastModified)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private String getBoatClassName(RegattaResults regattaResult) {
        Map<String, String> metadata = regattaResult.getMetadata();
        return metadata.get("boatClass");
    }

    @Override
    public Iterable<URL> getAllUrls() {
        return Collections.unmodifiableSet(allUrls);
    }

    @Override
    public RegattaScoreCorrections getScoreCorrections(String eventName, String boatClassName,
            TimePoint timePoint) throws Exception {
    	CompetitorResultsXlsImporter importer = new CompetitorResultsXlsImporter();
        for (URL url : getAllUrls()) {
            final URLConnection conn = url.openConnection();
            RegattaResults regattaResult = importer.getRegattaResults((InputStream) conn.getContent(), 
            		CompetitorResultsXlsImporter.IMPORT_TEMPLATE_WITHOUT_RANKS_DRACHEN, sheetName);
            if ((boatClassName == null && getBoatClassName(regattaResult) == null) ||
                    boatClassName.equals(getBoatClassName(regattaResult))) {
                return new RegattaScoreCorrectionsImpl(this, regattaResult);
            }
        }
        return null;
    }

    @Override
    public void registerResultUrl(URL url) {
        allUrls.add(url);
    }

    @Override
    public void removeResultUrl(URL url) {
        allUrls.remove(url);
    }
}

package com.sap.sailing.freg.resultimport.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.freg.resultimport.FregResultProvider;
import com.sap.sailing.freg.resultimport.RegattaResults;

public class ScoreCorrectionProviderImpl implements ScoreCorrectionProvider, FregResultProvider {
    private static final long serialVersionUID = 5853404150107387702L;
    
    private final Set<URL> allUrls;

    public ScoreCorrectionProviderImpl() throws MalformedURLException {
        allUrls = new HashSet<URL>();
        // add test URLs
        allUrls.add(new URL("http://www.axel-uhl.de/freg/freg_html_export_sample.html"));
        allUrls.add(new URL("http://www.axel-uhl.de/freg/eurocup_29er_29e.htm"));
    }

    @Override
    public String getName() {
        return "FREG HTML Score Importer";
    }
    
    @Override
    public Map<String, Set<Pair<String, TimePoint>>> getHasResultsForBoatClassFromDateByEventName() {
        Map<String, Set<Pair<String, TimePoint>>> result = new HashMap<String, Set<Pair<String,TimePoint>>>();
        FregHtmlParser parser = new FregHtmlParser();
        for (URL url : getAllUrls()) {
            URLConnection conn;
            try {
                conn = url.openConnection();
                TimePoint lastModified = new MillisecondsTimePoint(conn.getLastModified());
                RegattaResults regattaResult = parser.getRegattaResults((InputStream) conn.getContent());
                final String boatClassName = getBoatClassName(regattaResult);
                result.put(boatClassName, Collections.singleton(new Pair<String, TimePoint>(boatClassName, lastModified)));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * @return the first non-empty string of the list of metadata, hoping it's something pointing at the boat class at least...
     */
    private String getBoatClassName(RegattaResults regattaResult) {
        List<String> metadata = regattaResult.getMetadata();
        for (String metadatum : metadata) {
            if (metadatum != null && metadatum.length() > 0) {
                return metadatum;
            }
        }
        return null;
    }

    @Override
    public Iterable<URL> getAllUrls() {
        return Collections.unmodifiableSet(allUrls);
    }

    @Override
    public RegattaScoreCorrections getScoreCorrections(String eventName, String boatClassName,
            TimePoint timePoint) throws Exception {
        FregHtmlParser parser = new FregHtmlParser();
        for (URL url : getAllUrls()) {
            final URLConnection conn = url.openConnection();
            RegattaResults regattaResult = parser.getRegattaResults((InputStream) conn.getContent());
            if (boatClassName.equals(getBoatClassName(regattaResult))) {
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

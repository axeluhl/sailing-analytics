package com.sap.sailing.freg.resultimport.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.resultimport.RegattaResults;
import com.sap.sailing.resultimport.ResultDocumentProvider;
import com.sap.sailing.resultimport.UrlResultProvider;
import com.sap.sailing.resultimport.impl.AbstractDocumentBasedScoreCorrectionProvider;
import com.sap.sailing.resultimport.impl.RegattaScoreCorrectionsImpl;

public class ScoreCorrectionProviderImpl extends AbstractDocumentBasedScoreCorrectionProvider implements UrlResultProvider {
    private static final long serialVersionUID = 5853404150107387702L;
    
    private final Set<URL> allUrls;

    public ScoreCorrectionProviderImpl() throws MalformedURLException {
        this(new HashSet<URL>());
        /*
         * For testing, consider using the following URLs:
         *   allUrls.add(new URL("http://www.axel-uhl.de/freg/freg_html_export_sample.html"));
         *   allUrls.add(new URL("http://www.axel-uhl.de/freg/eurocup_29er_29e.htm"));
         */
    }

    private ScoreCorrectionProviderImpl(final Set<URL> allUrls) {
        super(new ResultDocumentProvider() {
            @Override
            public Iterable<Triple<InputStream, String, TimePoint>> getDocumentsAndNamesAndLastModified() throws IOException {
                List<Triple<InputStream, String, TimePoint>> result = new ArrayList<>();
                for (URL url : allUrls) {
                    URLConnection conn;
                    conn = url.openConnection();
                    result.add(new Triple<InputStream, String, TimePoint>((InputStream) conn.getContent(), url.toString(),
                            new MillisecondsTimePoint(conn.getLastModified())));
                }
                return result;
            }
        });
        this.allUrls = allUrls;
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
        Map<String, String> metadata = regattaResult.getMetadata();
        for (String metadatum : metadata.values()) {
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

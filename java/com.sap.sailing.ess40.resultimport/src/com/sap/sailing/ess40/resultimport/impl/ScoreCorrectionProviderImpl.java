package com.sap.sailing.ess40.resultimport.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class ScoreCorrectionProviderImpl implements ScoreCorrectionProvider {
    private static final long serialVersionUID = -4870646572106575667L;
    
    private static final String EXTREME_40_CLASS_NAME = "Extreme40";

    @Override
    public String getName() {
        return "Extreme Sailing Series 40 Scores from SailRacer.org";
    }
    
    private List<URL> getCsvUrls(String... actNames) throws MalformedURLException {
        List<URL> result = new ArrayList<URL>();
        for (String actName : actNames) {
            result.add(new URL("https://www.extremesailingseries.com/app/results/csv_uploads/"+actName+".csv"));
        }
        return result;
    }
    
    /**
     * @return A pair whose first component is the time point of the last modification to the act results, the second
     *         element is a map whose keys are the sail IDs (in the Extreme Sailing Series we'll use the team names as
     *         the sail IDs, such as "SAP Extreme Sailing Team" or just "SAP" for short); values with be a list
     *         representing the act's races, from first to last, where each {@link Util.Pair} holds in its first component a
     *         string describing the rank, which could be an integer number formatted as a string, or a three-letter
     *         disqualification reason such as "DNC", "DNF" or "DNS"; the second component is the points the competitor
     *         scored in that race. Usually, if the first component is a number, the score can be expected to be
     *         <code>#competitors+1 - rank</code>. A disqualification gets 0 points.
     */
    private Util.Pair<TimePoint, Map<String, List<Util.Pair<String, Double>>>> getActResults(URL actUrl) throws IOException {
        Pattern quotedCompetitorNameAndAllTheRest = Pattern.compile("^\"([^\"]*)\",(.*)$");
        Map<String, List<Util.Pair<String, Double>>> result = new HashMap<String, List<Util.Pair<String, Double>>>();
        HttpURLConnection conn = (HttpURLConnection) actUrl.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
        TimePoint lastModified = new MillisecondsTimePoint(conn.getLastModified());
        BufferedReader br = new BufferedReader(new InputStreamReader((InputStream) conn.getContent()));
        String line = br.readLine();
        while (line != null) {
            String[] split;
            String sailID;
            Matcher quotedCompetitorNameAndAllTheRestMatcher = quotedCompetitorNameAndAllTheRest.matcher(line);
            if (quotedCompetitorNameAndAllTheRestMatcher.matches()) {
                sailID = quotedCompetitorNameAndAllTheRestMatcher.group(1);
                split = quotedCompetitorNameAndAllTheRestMatcher.group(2).split(",");
            } else {
                String[] preSplit = line.split(",");
                sailID = preSplit[0].trim();
                if (sailID.startsWith("\"") && sailID.endsWith("\"")) {
                    sailID = sailID.substring(1, sailID.length()-1);
                }
                split = new String[preSplit.length-1];
                System.arraycopy(preSplit, 1, split, 0, split.length);
            }
            List<Util.Pair<String, Double>> competitorEntry = new ArrayList<Util.Pair<String, Double>>();
            result.put(sailID, competitorEntry);
            for (int i=0; i<split.length-1; i+=2) {
                String rankOrMaxPointsReason = split[i];
                Double points = Double.valueOf(split[i+1]);
                competitorEntry.add(new Util.Pair<String, Double>(rankOrMaxPointsReason, points));
            }
            line = br.readLine();
        }
        return new Util.Pair<TimePoint, Map<String, List<Util.Pair<String, Double>>>>(lastModified, result);
    }

    @Override
    public Map<String, Set<Util.Pair<String, TimePoint>>> getHasResultsForBoatClassFromDateByEventName() throws Exception {
        Map<String, Set<Util.Pair<String, TimePoint>>> result = new HashMap<String, Set<Util.Pair<String, TimePoint>>>();
        for (String actName : getAvailableActNames()) {
            URL actUrl = getCsvUrls(actName).iterator().next();
            Util.Pair<TimePoint, Map<String, List<Util.Pair<String, Double>>>> actResults = getActResults(actUrl);
            result.put(actName, Collections.singleton(new Util.Pair<String, TimePoint>(EXTREME_40_CLASS_NAME, actResults.getA())));
        }
        return result;
    }

    private Iterable<String> getAvailableActNames() throws IOException {
        List<String> result = new ArrayList<String>();
        URL url = new URL("https://www.extremesailingseries.com/app/results/csv_uploads/");
        Pattern p = Pattern.compile("<a href=\"([^\"]*)\\.csv\">");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
        conn.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        conn.setRequestProperty("accept-language", "en-US,en;q=0.8,de;q=0.6,da;q=0.4");

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String readLine;
        while ((readLine = br.readLine()) != null) {
            Matcher m = p.matcher(readLine);
            if (m.find()) {
                result.add(m.group(1));
            }
        }
        return result;
    }

    @Override
    public RegattaScoreCorrections getScoreCorrections(String actName, String boatClassName,
            TimePoint timePoint) throws Exception {
        URL actUrl = getCsvUrls(actName).iterator().next();
        Util.Pair<TimePoint, Map<String, List<Util.Pair<String, Double>>>> actResults = getActResults(actUrl);
        return new RegattaScoreCorrectionsImpl(this, actResults.getB());
    }

}

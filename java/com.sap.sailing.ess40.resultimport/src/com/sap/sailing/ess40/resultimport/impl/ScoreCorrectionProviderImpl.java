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

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Base64Utils;
import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;

public class ScoreCorrectionProviderImpl implements ScoreCorrectionProvider {
    private static final long serialVersionUID = -4870646572106575667L;
    
    private static final String[] ACT_NAMES = { "muscat", "qingdao", "istanbul", "porto" /*, "cardiff" */};

    private static final String EXTREME_40_CLASS_NAME = "extreme 40";

    @Override
    public String getName() {
        return "Extreme Sailing Series 40 Scores from SailRacer.org";
    }
    
    private List<URL> getCsvUrls(String... actNames) throws MalformedURLException {
        List<URL> result = new ArrayList<URL>();
        for (String actName : actNames) {
            result.add(new URL("http://www.extremesailingseries.com/app/results/csv_uploads/"+actName+".csv"));
        }
        return result;
    }
    
    /**
     * @return A pair whose first component is the time point of the last modification to the act results, the second
     *         element is a map whose keys are the sail IDs (in the Extreme Sailing Series we'll use the team names as
     *         the sail IDs, such as "SAP Extreme Sailing Team" or just "SAP" for short); values with be a list
     *         representing the act's races, from first to last, where each {@link Pair} holds in its first component a
     *         string describing the rank, which could be an integer number formatted as a string, or a three-letter
     *         disqualification reason such as "DNC", "DNF" or "DNS"; the second component is the points the competitor
     *         scored in that race. Usually, if the first component is a number, the score can be expected to be
     *         <code>#competitors+1 - rank</code>. A disqualification gets 0 points.
     */
    private Pair<TimePoint, Map<String, List<Pair<String, Integer>>>> getActResults(URL actUrl) throws IOException {
        Map<String, List<Pair<String, Integer>>> result = new HashMap<String, List<Pair<String,Integer>>>();
        HttpURLConnection conn = (HttpURLConnection) actUrl.openConnection();
        String authStringEnc = new String(Base64Utils.toBase64("tempuser:ocspwd07".getBytes()));
        conn.setRequestProperty("Authorization", "Basic "+authStringEnc);
        TimePoint lastModified = new MillisecondsTimePoint(conn.getLastModified());
        BufferedReader br = new BufferedReader(new InputStreamReader((InputStream) conn.getContent()));
        String line = br.readLine();
        while (line != null) {
            String[] split = line.split(",");
            String sailID = split[0].trim();
            if (sailID.startsWith("\"") && sailID.endsWith("\"")) {
                sailID = sailID.substring(1, sailID.length()-1);
            }
            List<Pair<String, Integer>> competitorEntry = new ArrayList<Pair<String, Integer>>();
            result.put(sailID, competitorEntry);
            for (int i=1; i<split.length-1; i+=2) {
                String rankOrMaxPointsReason = split[i];
                Integer points = Integer.valueOf(split[i+1]);
                competitorEntry.add(new Pair<String, Integer>(rankOrMaxPointsReason, points));
            }
            line = br.readLine();
        }
        return new Pair<TimePoint, Map<String, List<Pair<String, Integer>>>>(lastModified, result);
    }

    @Override
    public Map<String, Set<Pair<String, TimePoint>>> getHasResultsForBoatClassFromDateByEventName() throws Exception {
        Map<String, Set<Pair<String, TimePoint>>> result = new HashMap<String, Set<Pair<String, TimePoint>>>();
        for (String actName : ACT_NAMES) {
            URL actUrl = getCsvUrls(actName).iterator().next();
            Pair<TimePoint, Map<String, List<Pair<String, Integer>>>> actResults = getActResults(actUrl);
            result.put(actName, Collections.singleton(new Pair<String, TimePoint>(EXTREME_40_CLASS_NAME, actResults.getA())));
        }
        return result;
    }

    @Override
    public RegattaScoreCorrections getScoreCorrections(String actName, String boatClassName,
            TimePoint millisecondsTimePoint) throws Exception {
        URL actUrl = getCsvUrls(actName).iterator().next();
        Pair<TimePoint, Map<String, List<Pair<String, Integer>>>> actResults = getActResults(actUrl);
        return new RegattaScoreCorrectionsImpl(this, actResults.getB());
    }

}

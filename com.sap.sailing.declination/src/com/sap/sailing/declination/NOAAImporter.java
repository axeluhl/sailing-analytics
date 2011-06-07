package com.sap.sailing.declination;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sap.sailing.declination.impl.DeclinationRecordImpl;
import com.sap.sailing.declination.impl.DeclinationStore;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;

/**
 * Imports magnetic declination data for earth from NOAA (http://www.ngdc.noaa.gov)
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class NOAAImporter {
    private static final String QUERY_URL = "http://www.ngdc.noaa.gov/geomagmodels/struts/calcDeclination";
    private static final String REGEXP_DECLINATION = "<p class=\"indent\"><b>Declination</b> = ([0-9]*)&deg; ([0-9]*)' *([EW])";
    private static final String REGEXP_ANNUAL_CHANGE = "changing by *([0-9]*)&deg; *([0-9]*)' ([EW])/year *</p>";
    
    private final Pattern declinationPattern;
    private final Pattern annualChangePattern;
    private final DeclinationStore declinationStore;

    public NOAAImporter() {
        super();
        this.declinationPattern = Pattern.compile(REGEXP_DECLINATION);
        this.annualChangePattern = Pattern.compile(REGEXP_ANNUAL_CHANGE);
        declinationStore = new DeclinationStore();
    }

    protected Pattern getDeclinationPattern() {
        return declinationPattern;
    }

    protected Pattern getAnnualChangePattern() {
        return annualChangePattern;
    }

    public DeclinationRecord importRecord(Position position, TimePoint timePoint) throws IOException {
        DeclinationRecord result = null;
        URL url = new URL(QUERY_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        DataOutputStream out = new DataOutputStream(conn.getOutputStream());
        Date date = timePoint.asDate();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        String content = "minLatStr=" + position.getLatDeg() + "&minLatHemisphere="
                + (position.getLatDeg() >= 0 ? "n" : "s") + "&minLonStr=" + position.getLngDeg() + "&minLonHemisphere="
                + (position.getLngDeg() >= 0 ? "e" : "w") + "&minYear=" + calendar.get(Calendar.YEAR) + "&minMonth="
                + (calendar.get(Calendar.MONTH) + 1) + "&minDay=" + calendar.get(Calendar.DAY_OF_MONTH);
        out.writeBytes(content);
        out.flush();
        out.close();
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        int declinationDeg = 0;
        int declinationMin = 0;
        boolean foundDeclination = false;
        int annualChangeDeg = 0;
        int annualChangeMin = 0;
        boolean foundAnnualChange = false;
        StringBuilder page = new StringBuilder();
        while ((!foundDeclination || !foundAnnualChange) && (line = in.readLine()) != null) {
            page.append(line);
            page.append('\n');
            if (!foundDeclination) {
                Matcher declinationMatcher = declinationPattern.matcher(line);
                if (declinationMatcher.find()) {
                    foundDeclination = true;
                    declinationDeg = Integer.valueOf(declinationMatcher.group(1));
                    declinationMin = Integer.valueOf(declinationMatcher.group(2));
                    char ew = declinationMatcher.group(3).charAt(0);
                    if (ew == 'W') {
                        declinationDeg = -declinationDeg;
                        declinationMin = -declinationMin;
                    }
                }
            }
            if (!foundAnnualChange) {
                Matcher annualChangeMatcher = annualChangePattern.matcher(line);
                if (annualChangeMatcher.find()) {
                    foundAnnualChange = true;
                    annualChangeDeg = Integer.valueOf(annualChangeMatcher.group(1));
                    annualChangeMin = Integer.valueOf(annualChangeMatcher.group(2));
                    char ew = annualChangeMatcher.group(3).charAt(0);
                    if (ew == 'W') {
                        annualChangeDeg = -annualChangeDeg;
                        annualChangeMin = -annualChangeMin;
                    }
                }
            }
        }
        in.close();
        if (foundDeclination && foundAnnualChange) {
            result = new DeclinationRecordImpl(position, timePoint, new DegreeBearingImpl(((double) declinationDeg)
                    + ((double) declinationMin) / 60.), new DegreeBearingImpl(((double) annualChangeDeg)
                    + ((double) annualChangeMin) / 60.));
        }
        return result;
    }

    /**
     * Tries two things in parallel: fetch a more or less precise response from the online service and load
     * the requested year's declination values from a stored resource to look up a value that comes close.
     * The online lookup will be given preference. However, should it take longer than
     * <code>timeoutForOnlineFetchInMilliseconds</code>, then the method will return whatever it found
     * in the stored file, or <code>null</code> if no file exists for the year of <code>timePoint</code>.
     * 
     * @param timeoutForOnlineFetchInMilliseconds if 0, this means wait forever for the online result
     * @throws ParseException 
     * @throws ClassNotFoundException 
     * @throws IOException 
     */
    public DeclinationRecord getDeclination(final Position position, final TimePoint timePoint,
            long timeoutForOnlineFetchInMilliseconds) throws IOException, ClassNotFoundException, ParseException {
        final DeclinationRecord[] result = new DeclinationRecord[1];
        Thread fetcher = new Thread("Declination fetcher for "+position+"@"+timePoint) {
            @Override
            public void run() {
                try {
                    DeclinationRecord fetched = importRecord(position, timePoint);
                    synchronized (result) {
                        result[0] = fetched;
                        result.notifyAll();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        fetcher.start();
        Calendar cal = new GregorianCalendar();
        cal.setTime(timePoint.asDate());
        Set<DeclinationRecord> set = declinationStore.getStoredDeclinations(cal.get(Calendar.YEAR));
        if (set != null) {
            double meanTimeAndPositionDistance = Double.MAX_VALUE;
            DeclinationRecord nearest = null;
            for (DeclinationRecord r : set) {
                if (result[0] != null) {
                    break; // result obtained online; don't need to search in stored declinations anymore
                }
                if (nearest == null) {
                    nearest = r;
                } else {
                    double newDistance = getMeanTimeAndPositionDistance(r, position, timePoint);
                    if (newDistance < meanTimeAndPositionDistance) {
                        nearest = r;
                        meanTimeAndPositionDistance = newDistance;
                    }
                }
            }
            // now wait for the background thread as long as specified
            synchronized (result) {
                if (result[0] == null) {
                    try {
                        result.wait(timeoutForOnlineFetchInMilliseconds);
                    } catch (InterruptedException e) {
                        // ignore; simply use value from file in this case
                    }
                }
            }
            if (result[0] == null && nearest != null) {
                result[0] = nearest;
            }
        }
        return result[0];
    }

    /**
     * Computes a measure for a "distance" based on time and space, between two declination records. Being six months
     * off is deemed to be as bad as being sixty nautical miles off.
     */
    private double getMeanTimeAndPositionDistance(DeclinationRecord r, Position position, TimePoint timePoint) {
        double nauticalMileDistance = position.getDistance(r.getPosition()).getNauticalMiles();
        long millisDistance = Math.abs(timePoint.asMillis()-r.getTimePoint().asMillis());
        return ((double) millisDistance)/1000. /*s*/ / 3600. /*h*/ / 24. /*days*/ / 186. /*six months*/ +
                nauticalMileDistance/60.;
    }

}

package com.sap.sailing.declination;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sap.sailing.declination.impl.DeclinationRecordImpl;
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
    
    public NOAAImporter() {
        super();
        this.declinationPattern = Pattern.compile(REGEXP_DECLINATION);
        this.annualChangePattern = Pattern.compile(REGEXP_ANNUAL_CHANGE);
    }

    public Pattern getDeclinationPattern() {
        return declinationPattern;
    }

    public Pattern getAnnualChangePattern() {
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
        String content = "minLatStr="+position.getLatDeg()+"&minLatHemisphere="+
                (position.getLatDeg() >= 0 ? "n" : "s")+"&minLonStr="+position.getLngDeg()+
                "&minLonHemisphere="+(position.getLngDeg() >= 0 ? "e" : "w")+
                "&minYear="+calendar.get(Calendar.YEAR)+
                "&minMonth="+(calendar.get(Calendar.MONTH)+1)+
                "&minDay="+calendar.get(Calendar.DAY_OF_MONTH);
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
        while ((!foundDeclination || !foundAnnualChange) && (line=in.readLine()) != null) {
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

}

package com.sap.sailing.declination;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sap.sailing.declination.impl.DeclinationRecordImpl;
import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;

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
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    
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
     * Launches the importer, writing to resources/declination-year.txt (where "year" represents the year for which the
     * values are stored in the file) the declinations downloaded online for the years <code>args[0]</code> to
     * <code>args[1]</code> (inclusive) for all positions with a grid of <code>args[2]</code> degrees each, starting at
     * 0&deg;0.0'N and 0&deg;0.0'E.
     */
    public static void main(String[] args) throws IOException {
        NOAAImporter importer = new NOAAImporter();
        importer.run(args);
    }
    
    private void run(String[] args) throws FileNotFoundException, IOException {
        if (args.length == 0) {
            usage();
        } else {
            int fromYear = Integer.valueOf(args[0]);
            int toYear = Integer.valueOf(args[1]);
            if (toYear < fromYear) {
                usage();
            } else {
                double grid = Double.valueOf(args[2]);
                NOAAImporter importer = new NOAAImporter();
                for (int year = fromYear; year <= toYear; year++) {
                    ObjectOutput out = new ObjectOutputStream(new FileOutputStream("resources/declination-"+year+".txt"));
                    for (int month = 0; month < 12; month++) {
                        for (double lat = 0; lat < 90; lat += grid) {
                            System.out.println("Date: "+year+"/"+month+", Latitude: "+lat);
                            for (double lng = 0; lng < 180; lng += grid) {
                                fetchAndAppendDeclination(year, month, lat, lng, importer, out);
                            }
                            for (double lng = -grid; lng > -180; lng -= grid) {
                                fetchAndAppendDeclination(year, month, lat, lng, importer, out);
                            }
                        }
                        for (double lat = -grid; lat > -90; lat -= grid) {
                            for (double lng = 0; lng < 180; lng += grid) {
                                fetchAndAppendDeclination(year, month, lat, lng, importer, out);
                            }
                            for (double lng = -grid; lng > -180; lng -= grid) {
                                fetchAndAppendDeclination(year, month, lat, lng, importer, out);
                            }
                        }
                    }
                    out.close();
                }
            }
        }
    }

    private void fetchAndAppendDeclination(int year, int month, double lat, double lng, NOAAImporter importer,
            ObjectOutput out) throws IOException {
        Position position = new DegreePosition(lat, lng);
        Calendar cal = new GregorianCalendar(year, month, /* dayOfMonth */ 1);
        TimePoint timePoint = new MillisecondsTimePoint(cal.getTimeInMillis());
        DeclinationRecord declination = importer.importRecord(position, timePoint);
        if (declination != null) {
            writeExternal(declination, out);
        }
    }


    public void writeExternal(DeclinationRecord record, ObjectOutput out) throws IOException {
        out.writeUTF(dateFormatter.format(record.getTimePoint().asDate()));
        out.writeDouble(record.getPosition().getLatDeg());
        out.writeDouble(record.getPosition().getLngDeg());
        out.writeDouble(record.getBearing().getDegrees());
        out.writeDouble(record.getAnnualChange().getDegrees());
    }

    public DeclinationRecord readExternal(ObjectInput in) throws IOException, ClassNotFoundException, ParseException {
        TimePoint timePoint = new MillisecondsTimePoint(dateFormatter.parse(in.readUTF()).getTime());
        double lat = in.readDouble();
        double lng = in.readDouble();
        Position position = new DegreePosition(lat, lng);
        Bearing bearing = new DegreeBearingImpl(in.readDouble());
        Bearing annualChange = new DegreeBearingImpl(in.readDouble());
        return new DeclinationRecordImpl(position, timePoint, bearing, annualChange);
    }

    private void usage() {
        System.out.println("java " + NOAAImporter.class.getName() + " <fromYear> <toYear> <gridSizeInDegrees>");
    }
}

package com.sap.sailing.domain.orc.impl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;

import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.domain.common.orc.impl.ORCCertificateImpl;
import com.sap.sailing.domain.orc.ORCCertificateImporter;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.impl.SecondsDurationImpl;

/**
 * Represents a file in format {@code .rms} which is a simple ASCII file format, column-based, with fixed-width columns,
 * defined by a header line that defines the names and for the first columns up to the column labeled
 * {@link #NAME_OF_LAST_LEFT_ALIGNED_COLUMN_HEADER} also the width of the columns. The header uses column names that do
 * not contain spaces, separated by one or more spaces. For the columns up to and including the column
 * {@link #NAME_OF_LAST_LEFT_ALIGNED_COLUMN_HEADER} the column names are formatted left-aligned. For subsequent columns
 * which contain all numeric values without spaces the column names may also be centered. For those columns parsing can
 * simply split by space characters.
 * <p>
 * 
 * The result of successfully parsing a {@code .rms} file is a map keyed by the sailnumber, with values being
 * equal-sized maps from the column names to the {@link String} values. Additionally, the column names corresponding to
 * the array indices can be queried. A further step involves the creation of {@link ORCCertificate}s from the values of
 * the map.
 * 
 * @author Axel Uhl (d043530)
 * @author Daniel Lisunkin (i505543)
 *
 */

public class ORCCertificateImporterRMS implements ORCCertificateImporter {
    //Codes used in the RMS document as column names for different information
    private static final String TWA_COURSES = "R";
    private static final String CDL = "CDL";
    private static final String GPH = "GPH";
    private static final String LENGTH = "LOA";
    private static final String BOATCLASS = "TYPE";
    private static final String RUN_ALLOWANCE = "D";
    private static final String RUN_ANGLE = "DA";
    private static final String BEAT_ALLOWANCE = "UP";
    private static final String BEAT_ANGLE = "UA";

    private static final String NAME_OF_LAST_LEFT_ALIGNED_COLUMN_HEADER = "HH:MM:SS";

    private final LinkedHashMap<String, Integer> columnNamesAndWidths;

    private final Map<String, Map<String, String>> certificateValuesBySailnumber;

    public class ORCCertificateValues {
        private final String sailnumber;

        public ORCCertificateValues(String sailnumber) {
            super();
            this.sailnumber = sailnumber;
        }

        public String getValue(String columnName) {
            assert columnNamesAndWidths.containsKey(columnName);
            return certificateValuesBySailnumber.get(sailnumber).get(columnName);
        }
    }
    
    public ORCCertificateImporterRMS(Reader reader) throws IOException {
        final BufferedReader br = new BufferedReader(reader);
        columnNamesAndWidths = readColumnWidthsFromFirstLine(br.readLine());
        final String fileIdColumnName = columnNamesAndWidths.keySet().iterator().next();
        final String sailnumberColumnName = columnNamesAndWidths.keySet().iterator().next();
        certificateValuesBySailnumber = new HashMap<>();
        String line;
        
        while ((line = br.readLine()) != null) {
            final Map<String, String> parsedLine = parseLine(line);
            certificateValuesBySailnumber.put(parsedLine.get(sailnumberColumnName).replaceAll(" ", "").toUpperCase(), parsedLine);
        }
    }
    
    public ORCCertificateImporterRMS(InputStream in) throws IOException {
        String defaultEncoding = "UTF-8";
        BOMInputStream bomInputStream = new BOMInputStream(in);
        ByteOrderMark bom = bomInputStream.getBOM();
        String charsetName = bom == null ? defaultEncoding : bom.getCharsetName();
        InputStreamReader reader = new InputStreamReader(new BufferedInputStream(bomInputStream), charsetName);
        BufferedReader br = new BufferedReader(reader);
        columnNamesAndWidths = readColumnWidthsFromFirstLine(br.readLine());
        Iterator<String> iterator = columnNamesAndWidths.keySet().iterator();
        final String fileIdColumnName = iterator.next();
        final String sailnumberColumnName = iterator.next();
        certificateValuesBySailnumber = new HashMap<>();
        String line;
        
        while ((line = br.readLine()) != null) {
            final Map<String, String> parsedLine = parseLine(line);
            certificateValuesBySailnumber.put(parsedLine.get(sailnumberColumnName).replaceAll(" ", "").toUpperCase(), parsedLine);
        }
        
        br.close();
    }
    
    private Map<String, String> parseLine(final String line) {
        assert columnNamesAndWidths != null;
        final Map<String, String> result = new LinkedHashMap<>();
        int start=0;
        boolean splitBySpaceMode = false; // start using column names; switch to splitting by spaces when NAME_OF_LAST_LEFT_ALIGNED_COLUMN_HEADER has been found
        for (final Entry<String, Integer> columnNameAndWidth : columnNamesAndWidths.entrySet()) {
            final int end;
            if (splitBySpaceMode) {
                boolean foundSpace = false;
                int i;
                for (i=start; i<line.length() && (!foundSpace || line.charAt(i) == ' '); i++) {
                    if (!foundSpace) {
                        foundSpace = line.charAt(i) == ' ';
                    }
                }
                end = i;
            } else {
                end = start+columnNameAndWidth.getValue();
            }
            result.put(columnNameAndWidth.getKey(), line.substring(start, end).trim());
            if (columnNameAndWidth.getKey().equals(NAME_OF_LAST_LEFT_ALIGNED_COLUMN_HEADER)) {
                splitBySpaceMode = true;
            }
            start = end;
            
        }
        return result;
    }

    private LinkedHashMap<String, Integer> readColumnWidthsFromFirstLine(final String readLine) {
        final LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        final Pattern p = Pattern.compile("([^ ]+ *)");
        final Matcher m = p.matcher(readLine);
        int start = 0;
        while (m.find(start)) {
            result.put(m.group(1).trim(), m.group(1).length());
            start += m.group(1).length();
        }
        return result;
    }

    public Set<String> getColumnNames() {
        return Collections.unmodifiableSet(columnNamesAndWidths.keySet());
    }
    
    public Set<String> getSailnumbers() {
        return Collections.unmodifiableSet(certificateValuesBySailnumber.keySet());
    }

    public ORCCertificateValues getValuesForSailnumber(String sailnumber) {
        String searchString = sailnumber.replaceAll(" ", "").toUpperCase();
        return certificateValuesBySailnumber.containsKey(searchString) ? new ORCCertificateValues(searchString) : null;
    }
    
    @Override
    public ORCCertificate getCertificate(String sailnumber) {
        String searchString = sailnumber.replaceAll(" ", "").toUpperCase();
        ORCCertificateValues certificateValues = getValuesForSailnumber(searchString);
        String boatclass = certificateValues.getValue(BOATCLASS);
        Distance length  = new MeterDistance(Double.parseDouble(certificateValues.getValue(LENGTH)));
        Duration gph     = new SecondsDurationImpl(Double.parseDouble(certificateValues.getValue(GPH)));
        Double cdl       = Double.parseDouble(certificateValues.getValue(CDL));
        Map<Speed, Map<Bearing, Speed>> velocityPredictionsPerTrueWindSpeedAndAngle = new HashMap<>();
        Map<Speed, Bearing> beatAngles = new HashMap<>();
        Map<Speed, Speed> beatVMGPredictionPerTrueWindSpeed = new HashMap<>();
        Map<Speed, Duration> beatAllowancePerTrueWindSpeed = new HashMap<>();
        Map<Speed, Bearing> runAngles = new HashMap<>();
        Map<Speed, Speed> runVMGPredictionPerTrueWindSpeed = new HashMap<>();
        Map<Speed, Duration> runAllowancePerTrueWindSpeed = new HashMap<>();
        
        for (Speed tws : ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS) {
            String windSpeed = Integer.toString((int) tws.getKnots());
            String beatAngleKey = BEAT_ANGLE + windSpeed;
            String beatAllowanceKey = BEAT_ALLOWANCE + windSpeed;
            String runAngleKey = RUN_ANGLE + windSpeed;
            String runAllowanceKey = RUN_ALLOWANCE + windSpeed;
            beatAngles.put(tws, new DegreeBearingImpl(Double.parseDouble(certificateValues.getValue(beatAngleKey))));
            beatAllowancePerTrueWindSpeed.put(tws, new SecondsDurationImpl(Double.parseDouble(certificateValues.getValue(beatAllowanceKey))));
            beatVMGPredictionPerTrueWindSpeed.put(tws, ORCCertificateImpl.NAUTICAL_MILE.inTime(beatAllowancePerTrueWindSpeed.get(tws)));
            runAngles.put(tws, new DegreeBearingImpl(Double.parseDouble(certificateValues.getValue(runAngleKey))));
            runAllowancePerTrueWindSpeed.put(tws, new SecondsDurationImpl(Double.parseDouble(certificateValues.getValue(runAllowanceKey))));
            runVMGPredictionPerTrueWindSpeed.put(tws, ORCCertificateImpl.NAUTICAL_MILE.inTime(runAllowancePerTrueWindSpeed.get(tws)));
            Map<Bearing, Speed> velocityPredictionPerTrueWindAngle = new HashMap<>();
            for (Bearing twa : ORCCertificateImpl.ALLOWANCES_TRUE_WIND_ANGLES ) {
                String twaCoursesKey = TWA_COURSES + Integer.toString((int) twa.getDegrees()) + windSpeed;
                velocityPredictionPerTrueWindAngle.put(twa, ORCCertificateImpl.NAUTICAL_MILE.inTime(new SecondsDurationImpl(Double.parseDouble(certificateValues.getValue(twaCoursesKey)))));
            }
            velocityPredictionsPerTrueWindSpeedAndAngle.put(tws, velocityPredictionPerTrueWindAngle);
        }
        
        return new ORCCertificateImpl(searchString, boatclass, length, gph, cdl,
                velocityPredictionsPerTrueWindSpeedAndAngle, beatAngles, beatVMGPredictionPerTrueWindSpeed,
                beatAllowancePerTrueWindSpeed, runAngles, runVMGPredictionPerTrueWindSpeed,
                runAllowancePerTrueWindSpeed);
    }

    @Override
    public Map<String, ORCCertificate> getCertificates(String[] sailnumbers) {
        Map<String, ORCCertificate> result = new HashMap<>();

        for (String sailnumber : sailnumbers) {
            result.put(sailnumber, getCertificate(sailnumber));
        }

        return result;
    }
}

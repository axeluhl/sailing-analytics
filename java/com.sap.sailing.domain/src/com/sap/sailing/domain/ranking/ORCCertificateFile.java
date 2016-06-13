package com.sap.sailing.domain.ranking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a file in format {@code .rms} which is a simple ASCII file format, column-based, with fixed-width columns,
 * defined by a header line that defines the names and width of the columns by using column names that do not contain
 * spaces, separated by one or more spaces.
 * <p>
 * 
 * The result of successfully parsing a {@code .rms} file is a map keyed by the so-called national certificate file ID,
 * with values being equal-sized maps from the column names to the {@link String} values. Additionally, the column names
 * corresponding to the array indices can be queried.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class ORCCertificateFile {
    private final LinkedHashMap<String, Integer> columnWidths;
    
    private final Map<String, Map<String, String>> certificateValuesByFileId;
    
    public class ORCCertificateValues {
        private final String fileId;

        public ORCCertificateValues(String fileId) {
            super();
            this.fileId = fileId;
        }
        
        public String getValue(String columnName) {
            assert columnWidths.containsKey(columnName);
            return certificateValuesByFileId.get(fileId).get(columnName);
        }
    }
    
    public ORCCertificateFile(Reader reader) throws IOException {
        final BufferedReader br = new BufferedReader(reader);
        columnWidths = readColumnWidthsFromFirstLine(br.readLine());
        final String fileIdColumnName = columnWidths.keySet().iterator().next();
        certificateValuesByFileId = new HashMap<>();
        String line = br.readLine();
        while (line != null) {
            final Map<String, String> parsedLine = parseLine(line);
            certificateValuesByFileId.put(parsedLine.get(fileIdColumnName), parsedLine);
        }
    }
    
    private Map<String, String> parseLine(final String line) {
        assert columnWidths != null;
        final Map<String, String> result = new HashMap<>();
        int start=0;
        for (Entry<String, Integer> columnNameAndWidth : columnWidths.entrySet()) {
            final int end = start+columnNameAndWidth.getValue();
            result.put(columnNameAndWidth.getKey(), line.substring(start, end));
            start = end;
        }
        return result;
    }

    private LinkedHashMap<String, Integer> readColumnWidthsFromFirstLine(final String readLine) {
        final LinkedHashMap<String, Integer> result;
        final Pattern p = Pattern.compile("([^ ]+ +)*");
        final Matcher m = p.matcher(readLine);
        if (m.matches()) {
            result = new LinkedHashMap<>();
            for (int i=1; i<=m.groupCount(); i++) {
                result.put(m.group(i).trim(), m.group(i).length());
            }
        } else {
            result = null;
        }
        return result;
    }

    public Set<String> getColumnNames() {
        return Collections.unmodifiableSet(columnWidths.keySet());
    }
    
    public Set<String> getFileIds() {
        return Collections.unmodifiableSet(certificateValuesByFileId.keySet());
    }
    
    public ORCCertificateValues getValuesForFileId(String fileId) {
        return certificateValuesByFileId.containsKey(fileId) ? new ORCCertificateValues(fileId) : null;
    }
}

package com.sap.sailing.domain.orc.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sse.common.Util.Pair;

/**
 * Represents a file in format {@code .rms} which is a strange ASCII file format, column-based, with fixed-width columns,
 * "defined" by a header line that gives the names and for the first columns up to the column labeled
 * {@link #NAME_OF_COLUMN_HEADER_WHERE_THINGS_GET_WEIRD} also the width of the columns. Starting with the column following
 * {@link #NAME_OF_COLUMN_HEADER_WHERE_THINGS_GET_WEIRD}, field names may be "centered" over their character columns; for example,
 * the "LOA" field is represented as " LOA   " with the last space marking the separating space to the next column,
 * and the values are placed somewhere within those first six "payload" columns; they may appear centered, left-aligned
 * or right-aligned, and there doesn't seem to be a recognizable pattern to it.<p>
 * 
 * To make matters worse, some columns do not contain values at all. In this case, no alternation between space and non-space
 * characters can be used to determine field boundaries. At least, after the {@link #NAME_OF_COLUMN_HEADER_WHERE_THINGS_GET_WEIRD} no
 * columns can contain space characters unless they are entirely empty. And in all cases do those column values after
 * {@link #NAME_OF_COLUMN_HEADER_WHERE_THINGS_GET_WEIRD} have a non-space character in at least one of the columns used by the corresponding
 * field name in the header, unless the field value is left blank.<p>
 * 
 * Therefore, a good strategy seems to look for non-space characters in the columns used for the field name in the header.
 * If none are found, the field is assumed to be left empty. Otherwise, from the first non-blank character found in the columns
 * used by the field name in the header, all characters to the left and to the right that are no space are added to the
 * field value.<p>
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

public class ORCCertificatesRmsImporter extends AbstractORCCertificatesImporter {
    private static final String NAME_OF_COLUMN_HEADER_WHERE_THINGS_GET_WEIRD = ORCCertificatesCollectionRMS.ISSUEDATE;

    public ORCCertificatesCollectionRMS read(Reader reader) throws IOException {
        final BufferedReader br = new BufferedReader(reader);
        final String header = br.readLine();
        final LinkedHashMap<String, Pair<Integer, Integer>> columnNamesAndFieldNameStartInHeader = readColumnHeaderFieldNameStartColumnsAndLengthFromFirstLine(header);
        final Map<String, Map<String, String>> certificateValuesByCertificateId = new HashMap<>();
        String line;
        while ((line = br.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                final Map<String, String> parsedLine = parseLine(line, columnNamesAndFieldNameStartInHeader);
                certificateValuesByCertificateId.put(parsedLine.get(ORCCertificatesCollectionRMS.REFERENCE_NUMBER), parsedLine);
            }
        }
        return new ORCCertificatesCollectionRMS(certificateValuesByCertificateId);
    }
    
    public ORCCertificatesCollectionRMS read(InputStream in) throws IOException {
        return read(getReaderForInputStream(in));
    }

    private Map<String, String> parseLine(final String line, LinkedHashMap<String, Pair<Integer, Integer>> columnNamesAndFieldNameStartAndLengthInHeader) {
        assert columnNamesAndFieldNameStartAndLengthInHeader != null;
        final Map<String, String> result = new LinkedHashMap<>();
        boolean leftAligned = true;
        for (final Entry<String, Pair<Integer, Integer>> columnNameAndFieldNameStartInHeader : columnNamesAndFieldNameStartAndLengthInHeader.entrySet()) {
            final int start = columnNameAndFieldNameStartInHeader.getValue().getA();
            final int end;
            final String value;
            if (leftAligned) {
                end = start+columnNameAndFieldNameStartInHeader.getValue().getB();
                value = line.substring(start, end).trim();
            } else {
                end = start+columnNameAndFieldNameStartInHeader.getKey().length();
                value = expandFromNonSpaceCharacterInRange(line, start, end);
            }
            result.put(columnNameAndFieldNameStartInHeader.getKey(), value);
            if (columnNameAndFieldNameStartInHeader.getKey().equals(NAME_OF_COLUMN_HEADER_WHERE_THINGS_GET_WEIRD)) {
                leftAligned = false;
            }
        }
        return result;
    }

    /**
     * If only space characters are found in {@code line} starting at index {@code start} and ending before index {@code end},
     * the result is {@code null}. Otherwise, starting at any non-space character in the range, expansion to the left and right is
     * performed, collecting all contiguous non-space characters.
     */
    private String expandFromNonSpaceCharacterInRange(String line, int start, int end) {
        final String result;
        if (start >= line.length()) {
            result = null;
        } else {
            int left = start;
            // search for first non-whitespace from start looking right:
            while (left<line.length() && left < end && left < line.length() && Character.isWhitespace(line.charAt(left))) {
                left++;
            }
            if (left >= end) {
                result = null;
            } else {
                int right = left+1;
                // expand left:
                while (left > 0 && !Character.isWhitespace(line.charAt(left-1))) {
                    left--;
                }
                // expand right:
                while (right < line.length() && !Character.isWhitespace(line.charAt(right))) {
                    right++;
                }
                result = line.substring(left, right);
            }
        }
        return result;
    }

    private LinkedHashMap<String, Pair<Integer, Integer>> readColumnHeaderFieldNameStartColumnsAndLengthFromFirstLine(final String readLine) {
        final LinkedHashMap<String, Pair<Integer, Integer>> result = new LinkedHashMap<>();
        final Pattern p = Pattern.compile("([^ ]+ *)");
        final Matcher m = p.matcher(readLine);
        int start = 0;
        while (m.find(start)) {
            final String fieldNameTrimmed = m.group(1).trim();
            result.put(fieldNameTrimmed, new Pair<>(m.start(1), m.group(1).length()));
            start += m.group(1).length();
        }
        return result;
    }
}

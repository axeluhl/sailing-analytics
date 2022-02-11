package com.sap.sse.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.sap.sse.common.Util.Pair;

/**
 * Reads lines from a .csv file with fields separated by a separator character, by default ",".
 * When a field starts and ends with a double-quote character ("), those are omitted from the
 * field's value, and all double-double-quotes ("") within that string are replaced by a single
 * double-quote. Commas within double-quoted strings are not considered separators.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class CSVParser {
    private final String separator;
    
    public CSVParser() {
        this(",");
    }
    
    public CSVParser(String separator) {
        this.separator = separator;
    }

    private List<String> parseLine(String line) {
        assert line.indexOf("\n") < 0;
        final List<String> result = new ArrayList<>();
        final StringBuilder token = new StringBuilder();
        boolean atStartOfToken = true;
        boolean inQuotedToken = false;
        for (int i=0; i<line.length(); i++) {
            final char c = line.charAt(i);
            if (atStartOfToken && c == '"') {
                inQuotedToken = true;
                atStartOfToken = false;
            } else {
                if (inQuotedToken) {
                    if (c == '"') {
                        if (i == line.length()-1) {
                            // end of line and hence end of quoted token reached
                            inQuotedToken = false;
                            atStartOfToken = true;
                            result.add(token.toString());
                        } else if (line.substring(i+1).startsWith(separator)) {
                            // end of quoted token reached, but iteration will continue regularly and find separator
                            inQuotedToken = false;
                        } else if (line.charAt(i+1) == '"') {
                            // a quoted quote
                            token.append(line.charAt(++i));
                        } else {
                            throw new IllegalArgumentException("Non-doubled quote found in quoted string");
                        }
                    } else {
                        // accept anything else within the quotes:
                        token.append(c);
                    }
                } else if (line.substring(i).startsWith(separator)) {
                    atStartOfToken = true;
                    result.add(token.toString());
                    token.delete(0, token.length());
                    i += separator.length()-1; // in case separators longer than one character are used
                } else {
                    token.append(c);
                    atStartOfToken = false;
                    if (i == line.length()-1) {
                        result.add(token.toString());
                    }
                }
            }
        }
        return result;
    }
    
    public Iterable<List<String>> parseWithoutHeader(BufferedReader reader) throws IOException {
        final List<List<String>> content = new ArrayList<>();
        String contentLine;
        while ((contentLine=reader.readLine()) != null) {
            content.add(parseLine(contentLine));
        }
        return content;
    }
    
    public Pair<List<String>, Iterable<List<String>>> parseWithHeader(Reader reader) throws IOException {
        final BufferedReader br = new BufferedReader(reader);
        final List<String> header = parseLine(br.readLine());
        final Iterable<List<String>> content = parseWithoutHeader(br);
        return new Pair<>(header, content);
    }
}

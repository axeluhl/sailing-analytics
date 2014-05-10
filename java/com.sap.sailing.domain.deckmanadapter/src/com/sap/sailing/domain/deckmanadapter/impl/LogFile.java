package com.sap.sailing.domain.deckmanadapter.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A log file as produced by Deckman, in CSV format. The first line is expected to contain the column headers.
 * Column headers not found in {@link FieldType} are ignored with a warning being logged.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class LogFile implements Iterator<Record> {
    private final FieldType[] fieldTypes;
    private final BufferedReader bufferedReader;
    private String nextLine;
    
    /**
     * Reads the header from the reader to initialize {@link #fieldTypes}. Afterwards, using the {@link #iterator},
     * the records can be read one by one.
     */
    public LogFile(Reader r) throws IOException {
        bufferedReader = new BufferedReader(r);
        String header = bufferedReader.readLine();
        String[] fieldNames = header.split(",");
        fieldTypes = new FieldType[fieldNames.length];
        for (int i=0; i<fieldNames.length; i++) {
            fieldTypes[i] = FieldType.valueOf(fieldNames[i]);
        }
        nextLine = bufferedReader.readLine();
    }

    @Override
    public boolean hasNext() {
        return nextLine != null;
    }

    @Override
    public Record next() {
        final Map<FieldType, String> fieldsAsStrings = new HashMap<>();
        final String[] fieldValues = nextLine.split(",");
        try {
            nextLine = bufferedReader.readLine();
            for (int i = 0; i < fieldValues.length; i++) {
                fieldsAsStrings.put(fieldTypes[i], fieldValues[i]);
            }
            final Record result = new Record(fieldsAsStrings);
            return result;
        } catch (ParseException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove() is not supported on a Deckman log file");
    }

}

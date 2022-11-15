package com.sap.sailing.domain.orc.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Represents a file in format {@code .json} which is a simple ASCII file format. The {@code .json} file contains an
 * array of maps, each map representing one measurement certificate. The maps have the specific measurement values
 * accessible with a {@link String} for each boat.
 * <p>
 * 
 * The result of successfully parsing a {@code .json} file is a map keyed by the sailing number, with values being
 * equal-sized maps from the key names to the {@link String} values.
 * 
 * @author Daniel Lisunkin (i505543)
 *
 */
public class ORCCertificatesJsonImporter extends AbstractORCCertificatesImporter {
    /**
     * Receives an {@link InputStream} from different possible sources (web, local file, ...) and does parse the
     * {@code .json} file.
     */
    @Override
    public ORCCertificatesCollectionJSON read(InputStream inputStream) throws IOException, ParseException {
        return read(getReaderForInputStream(inputStream));
    }
    
    @Override
    public ORCCertificatesCollectionJSON read(Reader reader) throws IOException, ParseException {
        List<JSONObject> result = new LinkedList<>();
        JSONObject parsedJson = (JSONObject) new JSONParser().parse(reader);
        JSONArray dataArray = (JSONArray) parsedJson.get("rms");
        for (int i = 0; i < dataArray.size(); i++) {
            JSONObject object = (JSONObject) dataArray.get(i);
            result.add(object);
        }
        return new ORCCertificatesCollectionJSON(result);
    }
}

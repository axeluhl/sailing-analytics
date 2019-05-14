package com.sap.sailing.domain.orc.impl;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.orc.ORCCertificate;
import com.sap.sailing.domain.orc.ORCCertificateImporter;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.impl.DegreeBearingImpl;

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
public class ORCCertificateImporterJSON implements ORCCertificateImporter {

    private Map<String, Object> data;
    private static final int[] twsDistances = new int[] { 6, 8, 10, 12, 14, 16, 20 };
    private static final String RUN = "Run";
    private static final String BEAT = "Beat";

    /**
     * Receives an {@link InputStream} from different possible sources (web, local file, ...) and does parse the
     * {@code .json} file.
     * 
     * @param in
     * @throws IOException
     * @throws ParseException
     */
    public ORCCertificateImporterJSON(InputStream in) throws IOException, ParseException {
        Map<String, Object> result = new HashMap<>();

        String defaultEncoding = "UTF-8";
        BOMInputStream bOMInputStream = new BOMInputStream(in);
        ByteOrderMark bom = bOMInputStream.getBOM();
        String charsetName = bom == null ? defaultEncoding : bom.getCharsetName();
        InputStreamReader reader = new InputStreamReader(new BufferedInputStream(bOMInputStream), charsetName);

        JSONObject parsedJson = (JSONObject) new JSONParser().parse(reader);

        JSONArray dataArray = (JSONArray) parsedJson.get("rms");
        for (int i = 0; i < dataArray.size(); i++) {
            JSONObject object = (JSONObject) dataArray.get(i);
            result.put(((String) object.get("SailNo")).replaceAll(" ", ""), object);
        }

        data = result;
    }

    /**
     * Returns an {@link ORCCertificateImpl} object for a given {@link String} key. If there is no map value for the
     * given key, the method returns {@link null}.
     */
    @Override
    public ORCCertificate getCertificate(String sailnumber) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Returns a {@link Map} of {@link ORCCertificateImpl} keyed by the {@link String} sailnumbers, which were given as
     * an input inside an array.
     */
    @Override
    public Map<String, ORCCertificate> getCertificates(String[] sailnumbers) {
        Map<String, ORCCertificate> result = new HashMap<>();

        for (String sailnumber : sailnumbers) {
            result.put(sailnumber, getCertificate(sailnumber));
        }

        return result;
    }
}

package com.sap.sailing.domain.windfinderadapter.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.windfinderadapter.ReviewedSpotsCollection;
import com.sap.sailing.domain.windfinderadapter.Spot;

public class ReviewedSpotsCollectionImpl implements ReviewedSpotsCollection {
    /**
     * What needs to be appended to the spot collection {@link #id} in order
     * to obtain the file name of the document that has the JSON array with
     * the spot descriptions.
     */
    private final static String SPOT_LIST_DOCUMENT_SUFFIX = "_nearby.json";
    
    private final String id;
    private final WindFinderReportParser parser;
    
    public ReviewedSpotsCollectionImpl(String id) {
        this.id = id;
        this.parser = new WindFinderReportParser();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReviewedSpotsCollectionImpl other = (ReviewedSpotsCollectionImpl) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Iterable<Spot> getSpots() throws MalformedURLException, IOException, ParseException {
        JSONArray spotsAsJson = (JSONArray) new JSONParser().parse(new InputStreamReader(
                (InputStream) new URL(Activator.BASE_URL_FOR_JSON_DOCUMENTS+"/"+getId()+SPOT_LIST_DOCUMENT_SUFFIX).getContent()));
        return parser.parseSpots(spotsAsJson, this);
    }

    @Override
    public String toString() {
        return "WindFinder Reviewed Spots Collection [id=" + id + "]";
    }
}

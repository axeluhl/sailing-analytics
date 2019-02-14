package com.sap.sailing.domain.windfinderadapter.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.windfinder.ReviewedSpotsCollection;
import com.sap.sailing.domain.windfinder.Spot;
import com.sap.sse.util.ThreadPoolUtil;

public class ReviewedSpotsCollectionImpl implements ReviewedSpotsCollection {
    /**
     * What needs to be appended to the spot collection {@link #id} in order
     * to obtain the file name of the document that has the JSON array with
     * the spot descriptions.
     */
    private final static String SPOT_LIST_DOCUMENT_SUFFIX = "_nearby.json";
    
    private final String id;
    private final WindFinderReportParser parser;
    
    /**
     * Initialized during construction time with a future that is passed to the default foreground executor
     * (foreground because shortly a UI request may be depending on it). The task scheduled will create a
     * map initialized with the result of calling {@link #loadSpots()}.
     */
    private Future<ConcurrentMap<String, Spot>> spotsByIdCache;
    
    public ReviewedSpotsCollectionImpl(String id) {
        this.id = id;
        this.parser = new WindFinderReportParser();
        this.spotsByIdCache = ThreadPoolUtil.INSTANCE.getDefaultForegroundTaskThreadPoolExecutor().schedule(()->{
            final ConcurrentMap<String, Spot> result = new ConcurrentHashMap<>();
            for (final Spot spot : loadSpots()) {
                result.put(spot.getId(), spot);
            }
            return result;
        }, /* delay */ 0, TimeUnit.MILLISECONDS);
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
    public Iterable<Spot> getSpots(boolean cached) throws MalformedURLException, IOException, ParseException, InterruptedException, ExecutionException {
        final Iterable<Spot> result;
        if (cached) {
            result = new HashSet<>(spotsByIdCache.get().values());
        } else {
            result = loadSpots();
            for (final Spot spot : result) {
                spotsByIdCache.get().put(spot.getId(), spot);
            }
        }
        return result;
    }
    
    private Iterable<Spot> loadSpots() throws IOException, ParseException, MalformedURLException {
        final Iterable<Spot> result;
        JSONArray spotsAsJson = (JSONArray) new JSONParser().parse(new InputStreamReader(
                            (InputStream) new URL(Activator.BASE_URL_FOR_JSON_DOCUMENTS+"/"+getId()+SPOT_LIST_DOCUMENT_SUFFIX).getContent()));
                    result = parser.parseSpots(spotsAsJson, this);
        return result;
    }

    @Override
    public String toString() {
        return "WindFinder Reviewed Spots Collection [id=" + id + "]";
    }
}

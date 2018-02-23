package com.sap.sailing.domain.windfinder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;

import org.json.simple.parser.ParseException;

import com.sap.sse.common.WithID;

/**
 * A set of {@link Spot}s that WindFinder has reviewed and selected for a dedicated request for
 * a location, such as a sailing club or an event venue. The collection has a {@link String} as
 * a dedicated ID which is used in URL construction to obtain the {@link Spot}s in the collection
 * as well as the reports from those spots.<p>
 * 
 * Equality and hash code are based only on the {@link #getId() ID}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface ReviewedSpotsCollection extends WithID {
    @Override
    String getId();
    
    /**
     * @param cached
     *            if {@code true}, only those spots collections are returned that this factory has previously obtained;
     *            this is useful for a very fast lookup but does not guarantee that all spots collections added recently
     *            will be considered
     */
    Iterable<Spot> getSpots(boolean cached) throws MalformedURLException, IOException, ParseException, InterruptedException, ExecutionException;
}

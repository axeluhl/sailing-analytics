package com.sap.sailing.domain.windfinderadapter;

import java.io.IOException;
import java.net.MalformedURLException;

import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.tracking.WindTrackerFactory;

public interface WindFinderTrackerFactory extends WindTrackerFactory {

    Iterable<ReviewedSpotsCollection> getReviewedSpotsCollections();

    Spot getSpotById(String spotId) throws MalformedURLException, IOException, ParseException;

}

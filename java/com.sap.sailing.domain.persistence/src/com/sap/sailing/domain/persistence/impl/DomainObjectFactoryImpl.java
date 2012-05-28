package com.sap.sailing.domain.persistence.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.domain.base.impl.CourseAreaImpl;
import com.sap.sailing.domain.base.impl.EventImpl;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.base.impl.VenueImpl;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.common.impl.WindSourceWithAdditionalID;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.impl.LeaderboardGroupImpl;
import com.sap.sailing.domain.leaderboard.impl.LowerScoreIsBetter;
import com.sap.sailing.domain.leaderboard.impl.ResultDiscardingRuleImpl;
import com.sap.sailing.domain.leaderboard.impl.ScoreCorrectionImpl;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tracking.impl.WindTrackImpl;

public class DomainObjectFactoryImpl implements DomainObjectFactory {
    private static final Logger logger = Logger.getLogger(DomainObjectFactoryImpl.class.getName());

    private final DB database;
    
    public DomainObjectFactoryImpl(DB db) {
        super();
        this.database = db;
    }

    public Wind loadWind(DBObject object) {
        return new WindImpl(loadPosition(object), loadTimePoint(object), loadSpeedWithBearing(object));
    }

    private Position loadPosition(DBObject object) {
        Number latNumber = (Number) object.get(FieldNames.LAT_DEG.name());
        Double lat = latNumber == null ? null : latNumber.doubleValue();
        Number lngNumber = (Number) object.get(FieldNames.LNG_DEG.name());
        Double lng = lngNumber == null ? null : lngNumber.doubleValue();
        if (lat != null && lng != null) {
            return new DegreePosition(lat, lng);
        } else {
            return null;
        }
    }

    private TimePoint loadTimePoint(DBObject object) {
        return new MillisecondsTimePoint((Long) object.get(FieldNames.TIME_AS_MILLIS.name()));
    }

    private SpeedWithBearing loadSpeedWithBearing(DBObject object) {
        return new KnotSpeedWithBearingImpl(((Number) object.get(FieldNames.KNOT_SPEED.name())).doubleValue(),
                new DegreeBearingImpl(((Number) object.get(FieldNames.DEGREE_BEARING.name())).doubleValue()));
    }

    @Override
    public RaceIdentifier loadRaceIdentifier(DBObject dbObject) {
        RaceIdentifier result = null;
        String regattaName = (String) dbObject.get(FieldNames.EVENT_NAME.name());
        String raceName = (String) dbObject.get(FieldNames.RACE_NAME.name());
        if (regattaName != null && raceName != null) {
            result = new RegattaNameAndRaceName(regattaName, raceName);
        }
        return result;
    }
    
    @Override
    public WindTrack loadWindTrack(Regatta regatta, RaceDefinition race, WindSource windSource, long millisecondsOverWhichToAverage) {
        WindTrack result = new WindTrackImpl(millisecondsOverWhichToAverage, windSource.getType().getBaseConfidence(), windSource.getType().useSpeed());
        try {
            BasicDBObject query = new BasicDBObject();
            query.put(FieldNames.EVENT_NAME.name(), regatta.getName());
            query.put(FieldNames.RACE_NAME.name(), race.getName());
            query.put(FieldNames.WIND_SOURCE_NAME.name(), windSource.name());
            DBCollection windTracks = database.getCollection(CollectionNames.WIND_TRACKS.name());
            for (DBObject o : windTracks.find(query)) {
                Wind wind = loadWind((DBObject) o.get(FieldNames.WIND.name()));
                result.add(wind);
            }
        } catch (Throwable t) {
             // something went wrong during DB access; report, then use empty new wind track
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load recorded wind data. Check MongoDB settings.");
            logger.throwing(DomainObjectFactoryImpl.class.getName(), "loadWindTrack", t);
        }
        return result;
    }

    @Override
    public Leaderboard loadLeaderboard(String name) {
        DBCollection leaderboardCollection = database.getCollection(CollectionNames.LEADERBOARDS.name());
        Leaderboard result = null;
        try {
            BasicDBObject query = new BasicDBObject();
            query.put(FieldNames.LEADERBOARD_NAME.name(), name);
            for (DBObject o : leaderboardCollection.find(query)) {
                result = loadLeaderboard(o);
            }
        } catch (Throwable t) {
             // something went wrong during DB access; report, then use empty new wind track
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load leaderboard "+name+".");
            logger.throwing(DomainObjectFactoryImpl.class.getName(), "loadLeaderboard", t);
        }
        return result;
    }
    
    @Override
    public Iterable<Leaderboard> getAllLeaderboards() {
        DBCollection leaderboardCollection = database.getCollection(CollectionNames.LEADERBOARDS.name());
        Set<Leaderboard> result = new HashSet<Leaderboard>();
        try {
            for (DBObject o : leaderboardCollection.find()) {
                result.add(loadLeaderboard(o));
            }
        } catch (Throwable t) {
             // something went wrong during DB access; report, then use empty new wind track
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load leaderboards.");
            logger.throwing(DomainObjectFactoryImpl.class.getName(), "getAllLeaderboards", t);
        }
        return result;
    }

    private Leaderboard loadLeaderboard(DBObject o) {
        SettableScoreCorrection scoreCorrection = new ScoreCorrectionImpl();
        BasicDBList dbDiscardIndexResultsStartingWithHowManyRaces = (BasicDBList) o.get(FieldNames.LEADERBOARD_DISCARDING_THRESHOLDS.name());
        int[] discardIndexResultsStartingWithHowManyRaces = new int[dbDiscardIndexResultsStartingWithHowManyRaces.size()];
        int i=0;
        for (Object discardingThresholdAsObject : dbDiscardIndexResultsStartingWithHowManyRaces) {
            discardIndexResultsStartingWithHowManyRaces[i++] = (Integer) discardingThresholdAsObject;
        }
        ThresholdBasedResultDiscardingRule resultDiscardingRule = new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces);
        FlexibleLeaderboardImplWithDelayedCarriedPoints result = new FlexibleLeaderboardImplWithDelayedCarriedPoints(
                (String) o.get(FieldNames.LEADERBOARD_NAME.name()), scoreCorrection, resultDiscardingRule, new LowerScoreIsBetter());
        BasicDBList dbRaceColumns = (BasicDBList) o.get(FieldNames.LEADERBOARD_COLUMNS.name());
        // For a FlexibleLeaderboard, fleets are owned by the leaderboard's RaceColumn objects. We need to manage them here:
        Map<String, Fleet> fleetsByName = new HashMap<String, Fleet>();
        for (Object dbRaceColumnAsObject : dbRaceColumns) {
            BasicDBObject dbRaceColumn = (BasicDBObject) dbRaceColumnAsObject;
            Map<String, RaceIdentifier> raceIdentifiers = loadRaceIdentifiers(dbRaceColumn);
            RaceIdentifier defaultFleetRaceIdentifier = raceIdentifiers.get(null);
            if (defaultFleetRaceIdentifier != null) {
                Fleet defaultFleet = result.getFleet(null);
                if (defaultFleet != null) {
                    raceIdentifiers.put(defaultFleet.getName(), defaultFleetRaceIdentifier);
                } else {
                    // leaderboard has no default fleet; don't know what to do with default RaceIdentifier
                    logger.warning("Discarding RaceIdentifier "+defaultFleetRaceIdentifier+" for default fleet for leaderboard "+result.getName()+
                            " because no default fleet was found in leaderboard");
                }
                raceIdentifiers.remove(null);
            }
            List<Fleet> fleets = new ArrayList<Fleet>();
            for (String fleetName : raceIdentifiers.keySet()) {
                Fleet fleet = fleetsByName.get(fleetName);
                if (fleet == null) {
                    fleet = new FleetImpl(fleetName);
                    fleetsByName.put(fleetName, fleet);
                }
                fleets.add(fleet);
            }
            if (fleets.isEmpty()) {
                fleets.add(result.getFleet(null));
            }
            RaceColumn raceColumn = result.addRaceColumn((String) dbRaceColumn.get(FieldNames.LEADERBOARD_COLUMN_NAME.name()),
                    (Boolean) dbRaceColumn.get(FieldNames.LEADERBOARD_IS_MEDAL_RACE_COLUMN.name()), fleets.toArray(new Fleet[0]));
            for (Map.Entry<String, RaceIdentifier> e : raceIdentifiers.entrySet()) {
                raceColumn.setRaceIdentifier(fleetsByName.get(e.getKey()), e.getValue());
            }
        }
        DBObject carriedPoints = (DBObject) o.get(FieldNames.LEADERBOARD_CARRIED_POINTS.name());
        if (carriedPoints != null) {
            for (String competitorName : carriedPoints.keySet()) {
                Integer carriedPointsForCompetitor = (Integer) carriedPoints.get(competitorName);
                if (carriedPointsForCompetitor != null) {
                    result.setCarriedPoints(MongoUtils.unescapeDollarAndDot(competitorName), carriedPointsForCompetitor);
                }
            }
        }
        DBObject dbScoreCorrection = (DBObject) o.get(FieldNames.LEADERBOARD_SCORE_CORRECTIONS.name());
        for (String raceName : dbScoreCorrection.keySet()) {
            DBObject dbScoreCorrectionForRace = (DBObject) dbScoreCorrection.get(raceName);
            for (String competitorName : dbScoreCorrectionForRace.keySet()) {
                RaceColumn raceColumn = result.getRaceColumnByName(raceName);
                DBObject dbScoreCorrectionForCompetitorInRace = (DBObject) dbScoreCorrectionForRace.get(competitorName);
                if (dbScoreCorrectionForCompetitorInRace.containsField(FieldNames.LEADERBOARD_SCORE_CORRECTION_MAX_POINTS_REASON.name())) {
                    result.setMaxPointsReason(MongoUtils.unescapeDollarAndDot(competitorName), raceColumn, MaxPointsReason
                            .valueOf((String) dbScoreCorrectionForCompetitorInRace
                                    .get(FieldNames.LEADERBOARD_SCORE_CORRECTION_MAX_POINTS_REASON.name())));
                }
                if (dbScoreCorrectionForCompetitorInRace.containsField(FieldNames.LEADERBOARD_CORRECTED_SCORE.name())) {
                    result.correctScore(MongoUtils.unescapeDollarAndDot(competitorName), raceColumn, (Integer) dbScoreCorrectionForCompetitorInRace
                                    .get(FieldNames.LEADERBOARD_CORRECTED_SCORE.name()));
                }
            }
        }
        DBObject competitorDisplayNames = (DBObject) o.get(FieldNames.LEADERBOARD_COMPETITOR_DISPLAY_NAMES.name());
        if (competitorDisplayNames != null) {
            for (String escapedCompetitorName : competitorDisplayNames.keySet()) {
                result.setDisplayName(MongoUtils.unescapeDollarAndDot(escapedCompetitorName), (String) competitorDisplayNames.get(escapedCompetitorName));
            }
        }
        return result;
    }

    /**
     * Expects a DBObject under the key {@link FieldNames#RACE_IDENTIFIERS} whose keys are the fleet names and whose
     * values are the race identifiers as DBObjects (see {@link #loadRaceIdentifier(DBObject)}). If legacy DB instances
     * have a {@link RaceIdentifier} that is not associated with a fleet name, it may be stored directly in the
     * <code>dbRaceColumn</code>. In this case, it is returned with <code>null</code> as the fleet name key.
     * 
     * @return a map with fleet names as key and the corresponding fleet's race identifier as value; the special
     *         <code>null</code> key is used to identify a "default fleet" for backward compatibility with stored
     *         leaderboards which don't know about fleets yet; this key should be mapped to the leaderboard's default
     *         fleet.
     */
    private Map<String, RaceIdentifier> loadRaceIdentifiers(DBObject dbRaceColumn) {
        Map<String, RaceIdentifier> result = new HashMap<String, RaceIdentifier>();
        // try to load a deprecated single race identifier to associate with the default fleet:
        RaceIdentifier singleLegacyRaceIdentifier = loadRaceIdentifier(dbRaceColumn);
        if (singleLegacyRaceIdentifier != null) {
            result.put(null, singleLegacyRaceIdentifier);
        }
        DBObject raceIdentifiersPerFleet = (DBObject) dbRaceColumn.get(FieldNames.RACE_IDENTIFIERS.name());
        if (raceIdentifiersPerFleet != null) {
            for (String fleetName : raceIdentifiersPerFleet.keySet()) {
                result.put(fleetName, loadRaceIdentifier((DBObject) raceIdentifiersPerFleet.get(fleetName)));
            }
        }
        return result;
    }

    @Override
    public LeaderboardGroup loadLeaderboardGroup(String name) {
        DBCollection leaderboardGroupCollection = database.getCollection(CollectionNames.LEADERBOARD_GROUPS.name());
        LeaderboardGroup leaderboardGroup = null;
        
        try {
            BasicDBObject query = new BasicDBObject();
            query.put(FieldNames.LEADERBOARD_GROUP_NAME.name(), name);
            leaderboardGroup = loadLeaderboardGroup(leaderboardGroupCollection.findOne(query));
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load leaderboard group "+name+".");
            logger.throwing(DomainObjectFactoryImpl.class.getName(), "loadLeaderboardGroup", t);
        }
        
        return leaderboardGroup;
    }

    @Override
    public Iterable<LeaderboardGroup> getAllLeaderboardGroups() {
        DBCollection leaderboardGroupCollection = database.getCollection(CollectionNames.LEADERBOARD_GROUPS.name());
        Set<LeaderboardGroup> leaderboardGroups = new HashSet<LeaderboardGroup>();
        
        try {
            for (DBObject o : leaderboardGroupCollection.find()) {
                leaderboardGroups.add(loadLeaderboardGroup(o));
            }
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load leaderboard groups.");
            logger.throwing(DomainObjectFactoryImpl.class.getName(), "loadLeaderboardGroup", t);
        }
        
        return leaderboardGroups;
    }
    
    private LeaderboardGroup loadLeaderboardGroup(DBObject o) {
        DBCollection leaderboardCollection = database.getCollection(CollectionNames.LEADERBOARDS.name());
        
        String name = (String) o.get(FieldNames.LEADERBOARD_GROUP_NAME.name());
        String description = (String) o.get(FieldNames.LEADERBOARD_GROUP_DESCRIPTION.name());
        ArrayList<Leaderboard> leaderboards = new ArrayList<Leaderboard>();
        
        BasicDBList dbLeaderboardIds = (BasicDBList) o.get(FieldNames.LEADERBOARD_GROUP_LEADERBOARDS.name());
        for (Object object : dbLeaderboardIds) {
            ObjectId dbLeaderboardId = (ObjectId) object;
            DBObject dbLeaderboard = leaderboardCollection.findOne(dbLeaderboardId);
            leaderboards.add(loadLeaderboard(dbLeaderboard));
        }
        
        return new LeaderboardGroupImpl(name, description, leaderboards);
    }
    
    @Override
    public Iterable<Leaderboard> getLeaderboardsNotInGroup() {
        DBCollection leaderboardCollection = database.getCollection(CollectionNames.LEADERBOARDS.name());
        Set<Leaderboard> result = new HashSet<Leaderboard>();
        try {
            //Don't change the query object, unless you know what you're doing
            BasicDBObject query = new BasicDBObject("$where", "function() { return db." + CollectionNames.LEADERBOARD_GROUPS.name() + ".find({ "
                    + FieldNames.LEADERBOARD_GROUP_LEADERBOARDS.name() + ": this._id }).count() == 0; }");
            for (DBObject o : leaderboardCollection.find(query)) {
                result.add(loadLeaderboard(o));
            }
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load leaderboards.");
            logger.throwing(DomainObjectFactoryImpl.class.getName(), "getAllLeaderboards", t);
        }
        return result;
    }

    @Override
    public Map<? extends WindSource, ? extends WindTrack> loadWindTracks(Regatta regatta, RaceDefinition race,
            long millisecondsOverWhichToAverageWind) {
        Map<WindSource, WindTrack> result = new HashMap<WindSource, WindTrack>();
        try {
            BasicDBObject query = new BasicDBObject();
            // TODO EVENT_NAME has to become REGATTA_NAME, but we'd need a DB migration script for this for legacy instances
            query.put(FieldNames.EVENT_NAME.name(), regatta.getName());
            query.put(FieldNames.RACE_NAME.name(), race.getName());
            DBCollection windTracks = database.getCollection(CollectionNames.WIND_TRACKS.name());
            for (DBObject o : windTracks.find(query)) {
                Wind wind = loadWind((DBObject) o.get(FieldNames.WIND.name()));
                WindSourceType windSourceType = WindSourceType.valueOf((String) o.get(FieldNames.WIND_SOURCE_NAME.name()));
                WindSource windSource;
                if (o.containsField(FieldNames.WIND_SOURCE_ID.name())) {
                    windSource = new WindSourceWithAdditionalID(windSourceType, (String) o.get(FieldNames.WIND_SOURCE_ID.name()));
                } else {
                    windSource = new WindSourceImpl(windSourceType);
                }
                WindTrack track = result.get(windSource);
                if (track == null) {
                    track = new WindTrackImpl(millisecondsOverWhichToAverageWind, windSource.getType().getBaseConfidence(),
                            windSource.getType().useSpeed());
                    result.put(windSource, track);
                }
                track.add(wind);
            }
        } catch (Throwable t) {
             // something went wrong during DB access; report, then use empty new wind track
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load recorded wind data. Check MongoDB settings.");
            logger.throwing(DomainObjectFactoryImpl.class.getName(), "loadWindTrack", t);
        }
        return result;
    }

    @Override
    public Event loadEvent(String name) {
        Event result;
        BasicDBObject query = new BasicDBObject();
        query.put(FieldNames.EVENT_NAME.name(), name);
        DBCollection eventCollection = database.getCollection(CollectionNames.EVENTS.name());
        DBObject eventDBObject = eventCollection.findOne(query);
        if (eventDBObject != null) {
            result = loadEvent(eventDBObject);
        } else {
            result = null;
        }
        return result;
    }

    /**
     * An event doesn't store its regattas; it's the regatta that stores a reference to its event; the regatta
     * needs to add itself to the event when loaded or instantiated.
     */
    private Event loadEvent(DBObject eventDBObject) {
        String name = (String) eventDBObject.get(FieldNames.EVENT_NAME.name());
        Venue venue = loadVenue((DBObject) eventDBObject.get(FieldNames.VENUE.name()));
        Event result = new EventImpl(name, venue);
        return result;
    }

    private Venue loadVenue(DBObject dbObject) {
        String name = (String) dbObject.get(FieldNames.VENUE_NAME.name());
        BasicDBList dbCourseAreas = (BasicDBList) dbObject.get(FieldNames.COURSE_AREAS.name());
        Venue result = new VenueImpl(name);
        for (Object courseAreaDBObject : dbCourseAreas) {
            CourseArea courseArea = loadCourseArea((DBObject) courseAreaDBObject);
            result.addCourseArea(courseArea);
        }
        return result;
    }

    private CourseArea loadCourseArea(DBObject courseAreaDBObject) {
        String name = (String) courseAreaDBObject.get(FieldNames.COURSE_AREA_NAME.name());
        return new CourseAreaImpl(name);
    }

    @Override
    public Regatta loadRegatta(String name) {
        DBObject query = new BasicDBObject(FieldNames.REGATTA_NAME.name(), name);
        DBCollection regattaCollection = database.getCollection(CollectionNames.REGATTAS.name());
        DBObject dbRegatta = regattaCollection.findOne(query);
        Regatta result = null;
        if (dbRegatta != null) {
            String regattaName = (String) dbRegatta.get(FieldNames.REGATTA_NAME.name());
            String baseName = (String) dbRegatta.get(FieldNames.REGATTA_BASE_NAME.name());
            assert regattaName.equals(name);
            String boatClassName = (String) dbRegatta.get(FieldNames.BOAT_CLASS_NAME.name());
            BoatClass boatClass = null;
            if (boatClassName != null) {
                boolean typicallyStartsUpwind = (Boolean) dbRegatta.get(FieldNames.BOAT_CLASS_TYPICALLY_STARTS_UPWIND.name());
                boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass(boatClassName, typicallyStartsUpwind);
            }
            BasicDBList dbSeries = (BasicDBList) dbRegatta.get(FieldNames.REGATTA_SERIES.name());
            Iterable<Series> series = loadSeries(dbSeries);
            result = new RegattaImpl(baseName, boatClass, series);
        }
        return result;
    }

    private Iterable<Series> loadSeries(BasicDBList dbSeries) {
        List<Series> result = new ArrayList<Series>();
        for (Object o : dbSeries) {
            DBObject oneDBSeries = (DBObject) o;
            Series series = loadSeries(oneDBSeries);
            result.add(series);
        }
        return result;
    }

    private Series loadSeries(DBObject dbSeries) {
        String name = (String) dbSeries.get(FieldNames.SERIES_NAME.name());
        boolean isMedal = (Boolean) dbSeries.get(FieldNames.SERIES_IS_MEDAL.name());
        final BasicDBList dbFleets = (BasicDBList) dbSeries.get(FieldNames.SERIES_FLEETS.name());
        Map<String, Fleet> fleetsByName = loadFleets(dbFleets);
        BasicDBList dbRaceColumns = (BasicDBList) dbSeries.get(FieldNames.SERIES_RACE_COLUMNS.name());
        Iterable<String> raceColumnNames = loadRaceColumnNames(dbRaceColumns, fleetsByName);
        Series series = new SeriesImpl(name, isMedal, fleetsByName.values(), raceColumnNames);
        loadRaceColumnRaceLinks(dbRaceColumns, series);
        return series;
    }

    /**
     * @param fleetsByName used to ensure the {@link RaceColumn#getFleets()} points to the same {@link Fleet} objects also
     * used in the {@link Series#getFleets()} collection.
     */
    private Iterable<String> loadRaceColumnNames(BasicDBList dbRaceColumns, Map<String, Fleet> fleetsByName) {
        List<String> result = new ArrayList<String>();
        for (Object o : dbRaceColumns) {
            DBObject dbRaceColumn = (DBObject) o;
            result.add((String) dbRaceColumn.get(FieldNames.LEADERBOARD_COLUMN_NAME.name()));
        }
        return result;
    }

    private void loadRaceColumnRaceLinks(BasicDBList dbRaceColumns, Series series) {
        for (Object o : dbRaceColumns) {
            DBObject dbRaceColumn = (DBObject) o;
            String name = (String) dbRaceColumn.get(FieldNames.LEADERBOARD_COLUMN_NAME.name());
            Map<String, RaceIdentifier> raceIdentifiersPerFleetName = loadRaceIdentifiers(dbRaceColumn);
            for (Map.Entry<String, RaceIdentifier> e : raceIdentifiersPerFleetName.entrySet()) {
                // null key for "default" fleet is not acceptable here
                if (e.getKey() == null) {
                    logger.warning("Ignoring null fleet name while loading RaceColumn " + name);
                } else {
                    series.getRaceColumnByName(name).setRaceIdentifier(series.getFleetByName(e.getKey()), e.getValue());
                }
            }
        }
    }

    private Map<String, Fleet> loadFleets(BasicDBList dbFleets) {
        Map<String, Fleet> result = new HashMap<String, Fleet>();
        for (Object o : dbFleets) {
            DBObject dbFleet = (DBObject) o;
            Fleet fleet = loadFleet(dbFleet);
            result.put(fleet.getName(), fleet);
        }
        return result;
    }

    private Fleet loadFleet(DBObject dbFleet) {
        String name = (String) dbFleet.get(FieldNames.FLEET_NAME.name());
        Integer ordering = (Integer) dbFleet.get(FieldNames.FLEET_ORDERING.name());
        Fleet result;
        if (ordering != null) {
            result = new FleetImpl(name, ordering);
        } else {
            result = new FleetImpl(name);
        }
        return result;
    }

}

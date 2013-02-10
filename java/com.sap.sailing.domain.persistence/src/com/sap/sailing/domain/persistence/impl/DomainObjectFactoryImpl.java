package com.sap.sailing.domain.persistence.impl;

import java.io.Serializable;
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
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.RegattaRegistry;
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
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.RGBColor;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.common.impl.WindSourceWithAdditionalID;
import com.sap.sailing.domain.leaderboard.DelayedLeaderboardCorrections;
import com.sap.sailing.domain.leaderboard.DelayedLeaderboardCorrections.LeaderboardCorrectionsResolvedListener;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.LeaderboardRegistry;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.impl.FlexibleLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.LeaderboardGroupImpl;
import com.sap.sailing.domain.leaderboard.impl.RegattaLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.ResultDiscardingRuleImpl;
import com.sap.sailing.domain.leaderboard.impl.ScoreCorrectionImpl;
import com.sap.sailing.domain.leaderboard.meta.LeaderboardGroupMetaLeaderboard;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.racelog.Flags;
import com.sap.sailing.domain.racelog.RaceColumnIdentifier;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.domain.racelog.impl.RaceLogImpl;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
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
    
    private void ensureIndicesOnWindTracks(DBCollection windTracks) {
        windTracks.ensureIndex(FieldNames.RACE_ID.name()); // for new programmatic access
        windTracks.ensureIndex(FieldNames.REGATTA_NAME.name()); // for export or human look-up
        // for legacy access to not yet migrated fixes
        windTracks.ensureIndex(new BasicDBObjectBuilder().add(FieldNames.EVENT_NAME.name(), 1).add(FieldNames.RACE_NAME.name(), 1).get());
    }

    @Override
    public Leaderboard loadLeaderboard(String name, RegattaRegistry regattaRegistry) {
        DBCollection leaderboardCollection = database.getCollection(CollectionNames.LEADERBOARDS.name());
        Leaderboard result = null;
        try {
            BasicDBObject query = new BasicDBObject();
            query.put(FieldNames.LEADERBOARD_NAME.name(), name);
            for (DBObject o : leaderboardCollection.find(query)) {
                result = loadLeaderboard(o, regattaRegistry, /* leaderboardRegistry */ null, /* groupForMetaLeaderboard */ null);
            }
        } catch (Exception e) {
             // something went wrong during DB access; report, then use empty new wind track
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load leaderboard "+name+".");
            logger.throwing(DomainObjectFactoryImpl.class.getName(), "loadLeaderboard", e);
        }
        return result;
    }
    
    @Override
    public Iterable<Leaderboard> getAllLeaderboards(RegattaRegistry regattaRegistry, LeaderboardRegistry leaderboardRegistry) {
        DBCollection leaderboardCollection = database.getCollection(CollectionNames.LEADERBOARDS.name());
        Set<Leaderboard> result = new HashSet<Leaderboard>();
        try {
            for (DBObject o : leaderboardCollection.find()) {
                final Leaderboard loadedLeaderboard = loadLeaderboard(o, regattaRegistry, leaderboardRegistry, /* groupForMetaLeaderboard */ null);
                if (loadedLeaderboard != null) {
                    result.add(loadedLeaderboard);
                }
            }
        } catch (Exception e) {
             // something went wrong during DB access; report, then use empty new wind track
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load leaderboards.");
            logger.throwing(DomainObjectFactoryImpl.class.getName(), "getAllLeaderboards", e);
        }
        return result;
    }

    /**
     * If the DBObject has a field {@link FieldNames#REGATTA_NAME} then the object represents a
     * {@link RegattaLeaderboard}. Otherwise, a {@link FlexibleLeaderboard} will be loaded.
     * 
     * @param leaderboardRegistry
     *            if not <code>null</code>, then before creating and loading the leaderboard it is looked up in this
     *            registry and only loaded if not found there. If <code>leaderboardRegistry</code> is <code>null</code>,
     *            the leaderboard is loaded in any case. If the leaderboard is loaded and
     *            <code>leaderboardRegistry</code> is not <code>null</code>, the leaderboard loaded is
     *            {@link LeaderboardRegistry#addLeaderboard(Leaderboard) added to the registry}.
     * @param groupForMetaLeaderboard
     *            if not <code>null</code>, a {@link LeaderboardGroupMetaLeaderboard} instance is created and set as the
     *            group's {@link LeaderboardGroup#setOverallLeaderboard(Leaderboard) overall leaderboard}
     * 
     * @return <code>null</code> in case the leaderboard couldn't be loaded, e.g., because the regatta referenced by a
     *         {@link RegattaLeaderboard} cannot be found; the leaderboard loaded or found in
     *         <code>leaderboardRegistry</code>, otherwise
     */
    private Leaderboard loadLeaderboard(DBObject dbLeaderboard, RegattaRegistry regattaRegistry, LeaderboardRegistry leaderboardRegistry,
            LeaderboardGroup groupForMetaLeaderboard) {
        Leaderboard result = null;
        String leaderboardName = (String) dbLeaderboard.get(FieldNames.LEADERBOARD_NAME.name());
        if (leaderboardRegistry != null) {
            result = leaderboardRegistry.getLeaderboardByName(leaderboardName);
        }
        if (result == null) {
            SettableScoreCorrection scoreCorrection = new ScoreCorrectionImpl();
            ThresholdBasedResultDiscardingRule resultDiscardingRule = loadResultDiscardingRule(dbLeaderboard);
            String regattaName = (String) dbLeaderboard.get(FieldNames.REGATTA_NAME.name());
            CourseArea courseArea = loadCourseAreaFromEvents(dbLeaderboard);
            if (groupForMetaLeaderboard != null) {
                result = new LeaderboardGroupMetaLeaderboard(groupForMetaLeaderboard, loadScoringScheme(dbLeaderboard), resultDiscardingRule);
                groupForMetaLeaderboard.setOverallLeaderboard(result);
            } else if (regattaName == null) {
                result = loadFlexibleLeaderboard(dbLeaderboard, scoreCorrection, resultDiscardingRule, courseArea);
            } else {
                result = loadRegattaLeaderboard(leaderboardName, regattaName, dbLeaderboard, scoreCorrection, resultDiscardingRule, regattaRegistry, courseArea);
            }
            if (result != null) {
                final Leaderboard finalResult = result;
                DelayedLeaderboardCorrections loadedLeaderboardCorrections = new DelayedLeaderboardCorrectionsImpl(result);
                final boolean[] needsMigration = new boolean[1];
                loadedLeaderboardCorrections.addLeaderboardCorrectionsResolvedListener(new LeaderboardCorrectionsResolvedListener() {
                    @Override
                    public void correctionsResolved(DelayedLeaderboardCorrections delayedLeaderboardCorrections) {
                        if (needsMigration[0]) {
                            new MongoObjectFactoryImpl(database).storeLeaderboard(finalResult);
                        }
                    }
                });
                needsMigration[0] = loadLeaderboardCorrections(dbLeaderboard, loadedLeaderboardCorrections, scoreCorrection) || needsMigration[0];
                needsMigration[0] = loadSuppressedCompetitors(dbLeaderboard, loadedLeaderboardCorrections) || needsMigration[0];
                loadColumnFactors(dbLeaderboard, result);
                // add the leaderboard to the registry
                if (leaderboardRegistry != null) {
                    leaderboardRegistry.addLeaderboard(result);
                    logger.info("loaded leaderboard "+result.getName()+" into "+leaderboardRegistry);
                }
            }
        }
        return result;
    }

    private void loadColumnFactors(DBObject dbLeaderboard, Leaderboard result) {
        DBObject dbColumnFactors = (DBObject) dbLeaderboard.get(FieldNames.LEADERBOARD_COLUMN_FACTORS.name());
        if (dbColumnFactors != null) {
            for (String encodedRaceColumnName : dbColumnFactors.keySet()) {
                double factor = ((Number) dbColumnFactors.get(encodedRaceColumnName)).doubleValue();
                String raceColumnName = MongoUtils.unescapeDollarAndDot(encodedRaceColumnName);
                result.getRaceColumnByName(raceColumnName).setFactor(factor);
            }
        }
    }

    private boolean loadSuppressedCompetitors(DBObject dbLeaderboard,
            DelayedLeaderboardCorrections loadedLeaderboardCorrections) {
        final boolean needsMigration;
        BasicDBList dbSuppressedCompetitorNames = (BasicDBList) dbLeaderboard.get(FieldNames.LEADERBOARD_SUPPRESSED_COMPETITORS.name());
        if (dbSuppressedCompetitorNames != null) {
            needsMigration = true;
            for (Object escapedCompetitorName : dbSuppressedCompetitorNames) {
                loadedLeaderboardCorrections.suppressCompetitorByName(MongoUtils.unescapeDollarAndDot((String) escapedCompetitorName));
            }
        } else {
            needsMigration = false;
        }
        BasicDBList dbSuppressedCompetitorIDs = (BasicDBList) dbLeaderboard.get(FieldNames.LEADERBOARD_SUPPRESSED_COMPETITOR_IDS.name());
        if (dbSuppressedCompetitorIDs != null) {
            for (Object competitorId : dbSuppressedCompetitorIDs) {
                loadedLeaderboardCorrections.suppressCompetitorById((Serializable) competitorId);
            }
        }
        return needsMigration;
    }

    /**
     * @param dbLeaderboard expects to find a field named {@link FieldNames#LEADERBOARD_DISCARDING_THRESHOLDS}
     */
    private ThresholdBasedResultDiscardingRule loadResultDiscardingRule(DBObject dbLeaderboard) {
        BasicDBList dbDiscardIndexResultsStartingWithHowManyRaces = (BasicDBList) dbLeaderboard
                .get(FieldNames.LEADERBOARD_DISCARDING_THRESHOLDS.name());
        int[] discardIndexResultsStartingWithHowManyRaces = new int[dbDiscardIndexResultsStartingWithHowManyRaces.size()];
        int i = 0;
        for (Object discardingThresholdAsObject : dbDiscardIndexResultsStartingWithHowManyRaces) {
            discardIndexResultsStartingWithHowManyRaces[i++] = (Integer) discardingThresholdAsObject;
        }
        ThresholdBasedResultDiscardingRule resultDiscardingRule = new ResultDiscardingRuleImpl(
                discardIndexResultsStartingWithHowManyRaces);
        return resultDiscardingRule;
    }
    
    private CourseArea loadCourseAreaFromEvents(DBObject dbLeaderboard) {
    	Serializable courseAreaId = (Serializable) dbLeaderboard.get(FieldNames.COURSE_AREA_ID.name());
    	if (courseAreaId == null)
    		return null;
    	
    	Iterable<Event> allEvents = loadAllEvents();
    	for (Event event : allEvents) {
    		for (CourseArea courseArea : event.getVenue().getCourseAreas()) {
    			if (courseArea.getId().equals(courseAreaId)) {
    				return courseArea;
    			}
    		}
    	}
    	return null;
    }

    /**
     * @return <code>null</code> if the regatta cannot be resolved; otherwise the leaderboard for the regatta specified
     */
    private RegattaLeaderboard loadRegattaLeaderboard(String leaderboardName, String regattaName, DBObject dbLeaderboard,
            SettableScoreCorrection scoreCorrection, ThresholdBasedResultDiscardingRule resultDiscardingRule, RegattaRegistry regattaRegistry,
            CourseArea courseArea) {
        RegattaLeaderboard result = null;
        Regatta regatta = regattaRegistry.getRegatta(new RegattaName(regattaName));
        if (regatta == null) {
            logger.info("Couldn't find regatta "+regattaName+" for corresponding regatta leaderboard. Not loading regatta leaderboard.");
        } else {
            result = new RegattaLeaderboardImpl(regatta, scoreCorrection, resultDiscardingRule, courseArea);
            result.setName(leaderboardName);
        }
        return result;
    }

    private FlexibleLeaderboard loadFlexibleLeaderboard(DBObject dbLeaderboard,
            SettableScoreCorrection scoreCorrection, ThresholdBasedResultDiscardingRule resultDiscardingRule, CourseArea courseArea) {
        final ScoringScheme scoringScheme = loadScoringScheme(dbLeaderboard);
        
        FlexibleLeaderboardImpl result = new FlexibleLeaderboardImpl(
                (String) dbLeaderboard.get(FieldNames.LEADERBOARD_NAME.name()), scoreCorrection, resultDiscardingRule,
                scoringScheme, courseArea);
        BasicDBList dbRaceColumns = (BasicDBList) dbLeaderboard.get(FieldNames.LEADERBOARD_COLUMNS.name());
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
        return result;
    }

    private ScoringScheme loadScoringScheme(DBObject dbLeaderboard) {
        ScoringSchemeType scoringSchemeType = getScoringSchemeType(dbLeaderboard);
        final ScoringScheme scoringScheme = DomainFactory.INSTANCE.createScoringScheme(scoringSchemeType);
        return scoringScheme;
    }

    private boolean loadLeaderboardCorrections(DBObject dbLeaderboard, DelayedLeaderboardCorrections correctionsToUpdate,
            SettableScoreCorrection scoreCorrectionToUpdate) {
        boolean needsMigration = false;
        DBObject carriedPoints = (DBObject) dbLeaderboard.get(FieldNames.LEADERBOARD_CARRIED_POINTS.name());
        if (carriedPoints != null) {
            needsMigration = true;
            for (String competitorName : carriedPoints.keySet()) {
                Double carriedPointsForCompetitor = ((Number) carriedPoints.get(competitorName)).doubleValue();
                if (carriedPointsForCompetitor != null) {
                    correctionsToUpdate.setCarriedPointsByName(MongoUtils.unescapeDollarAndDot(competitorName), carriedPointsForCompetitor);
                }
            }
        }
        BasicDBList carriedPointsById = (BasicDBList) dbLeaderboard.get(FieldNames.LEADERBOARD_CARRIED_POINTS_BY_ID.name());
        if (carriedPointsById != null) {
            for (Object o : carriedPointsById) {
                DBObject competitorIdAndCarriedPoints = (DBObject) o;
                Serializable competitorId = (Serializable) competitorIdAndCarriedPoints.get(FieldNames.COMPETITOR_ID.name());
                Double carriedPointsForCompetitor = ((Number) competitorIdAndCarriedPoints
                        .get(FieldNames.LEADERBOARD_CARRIED_POINTS.name())).doubleValue();
                if (carriedPointsForCompetitor != null) {
                    correctionsToUpdate.setCarriedPointsByID(competitorId, carriedPointsForCompetitor);
                }
            }
        }
        DBObject dbScoreCorrection = (DBObject) dbLeaderboard.get(FieldNames.LEADERBOARD_SCORE_CORRECTIONS.name());
        if (dbScoreCorrection.containsField(FieldNames.LEADERBOARD_SCORE_CORRECTION_TIMESTAMP.name())) {
            scoreCorrectionToUpdate.setTimePointOfLastCorrectionsValidity(
                    new MillisecondsTimePoint((Long) dbScoreCorrection.get(FieldNames.LEADERBOARD_SCORE_CORRECTION_TIMESTAMP.name())));
            dbScoreCorrection.removeField(FieldNames.LEADERBOARD_SCORE_CORRECTION_TIMESTAMP.name());
        }
        if (dbScoreCorrection.containsField(FieldNames.LEADERBOARD_SCORE_CORRECTION_COMMENT.name())) {
            scoreCorrectionToUpdate.setComment((String) dbScoreCorrection.get(FieldNames.LEADERBOARD_SCORE_CORRECTION_COMMENT.name()));
            dbScoreCorrection.removeField(FieldNames.LEADERBOARD_SCORE_CORRECTION_COMMENT.name());
        }
        for (String raceName : dbScoreCorrection.keySet()) {
            // deprecated style: a DBObject per race where the keys are the escaped competitor names
            // new style: a BasicDBList per race where each entry is a DBObject with COMPETITOR_ID and
            //            LEADERBOARD_SCORE_CORRECTION_MAX_POINTS_REASON and LEADERBOARD_CORRECTED_SCORE fields each
            DBObject dbScoreCorrectionForRace = (DBObject) dbScoreCorrection.get(raceName);
            final RaceColumn raceColumn = correctionsToUpdate.getLeaderboard().getRaceColumnByName(raceName);
            if (dbScoreCorrectionForRace instanceof BasicDBList) {
                for (Object o : (BasicDBList) dbScoreCorrectionForRace) {
                    DBObject dbScoreCorrectionForCompetitorInRace = (DBObject) o;
                    Serializable competitorId = (Serializable) dbScoreCorrectionForCompetitorInRace.get(FieldNames.COMPETITOR_ID.name());
                    if (dbScoreCorrectionForCompetitorInRace
                            .containsField(FieldNames.LEADERBOARD_SCORE_CORRECTION_MAX_POINTS_REASON.name())) {
                        correctionsToUpdate.setMaxPointsReasonByID(competitorId,
                                raceColumn, MaxPointsReason.valueOf((String) dbScoreCorrectionForCompetitorInRace
                                        .get(FieldNames.LEADERBOARD_SCORE_CORRECTION_MAX_POINTS_REASON.name())));
                    }
                    if (dbScoreCorrectionForCompetitorInRace.containsField(FieldNames.LEADERBOARD_CORRECTED_SCORE.name())) {
                        final Double leaderboardCorrectedScore = ((Number) dbScoreCorrectionForCompetitorInRace
                                .get(FieldNames.LEADERBOARD_CORRECTED_SCORE.name())).doubleValue();
                        correctionsToUpdate.correctScoreByID(competitorId, raceColumn, (Double) leaderboardCorrectedScore);
                    }
                }
            } else {
                needsMigration = true;
                for (String competitorName : dbScoreCorrectionForRace.keySet()) {
                    DBObject dbScoreCorrectionForCompetitorInRace = (DBObject) dbScoreCorrectionForRace.get(competitorName);
                    if (dbScoreCorrectionForCompetitorInRace
                            .containsField(FieldNames.LEADERBOARD_SCORE_CORRECTION_MAX_POINTS_REASON.name())) {
                        correctionsToUpdate.setMaxPointsReasonByName(MongoUtils.unescapeDollarAndDot(competitorName),
                                raceColumn, MaxPointsReason.valueOf((String) dbScoreCorrectionForCompetitorInRace
                                        .get(FieldNames.LEADERBOARD_SCORE_CORRECTION_MAX_POINTS_REASON.name())));
                    }
                    if (dbScoreCorrectionForCompetitorInRace.containsField(FieldNames.LEADERBOARD_CORRECTED_SCORE.name())) {
                        final Double leaderboardCorrectedScore = ((Number) dbScoreCorrectionForCompetitorInRace
                                .get(FieldNames.LEADERBOARD_CORRECTED_SCORE.name())).doubleValue();
                        correctionsToUpdate.correctScoreByName(MongoUtils.unescapeDollarAndDot(competitorName),
                                raceColumn, (Double) leaderboardCorrectedScore);
                    }
                }
            }
        }
        DBObject competitorDisplayNames = (DBObject) dbLeaderboard.get(FieldNames.LEADERBOARD_COMPETITOR_DISPLAY_NAMES.name());
        // deprecated style: a DBObject whose keys are the escaped competitor names
        // new style: a BasicDBList whose entries are DBObjects with COMPETITOR_ID and COMPETITOR_DISPLAY_NAME fields
        if (competitorDisplayNames != null) {
            if (competitorDisplayNames instanceof BasicDBList) {
                for (Object o : (BasicDBList) competitorDisplayNames) {
                    DBObject competitorDisplayName = (DBObject) o;
                    final Serializable competitorId = (Serializable) competitorDisplayName.get(FieldNames.COMPETITOR_ID.name());
                    final String displayName = (String) competitorDisplayName.get(FieldNames.COMPETITOR_DISPLAY_NAME.name());
                    correctionsToUpdate.setDisplayNameByID(competitorId, displayName);
                }
            } else {
                needsMigration = true;
                for (String escapedCompetitorName : competitorDisplayNames.keySet()) {
                    correctionsToUpdate.setDisplayNameByName(MongoUtils.unescapeDollarAndDot(escapedCompetitorName),
                            (String) competitorDisplayNames.get(escapedCompetitorName));
                }
            }
        }
        return needsMigration;
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
    public LeaderboardGroup loadLeaderboardGroup(String name, RegattaRegistry regattaRegistry, LeaderboardRegistry leaderboardRegistry) {
        DBCollection leaderboardGroupCollection = database.getCollection(CollectionNames.LEADERBOARD_GROUPS.name());
        LeaderboardGroup leaderboardGroup = null;
        try {
            BasicDBObject query = new BasicDBObject();
            query.put(FieldNames.LEADERBOARD_GROUP_NAME.name(), name);
            leaderboardGroup = loadLeaderboardGroup(leaderboardGroupCollection.findOne(query), regattaRegistry, leaderboardRegistry);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load leaderboard group "+name+".");
            logger.throwing(DomainObjectFactoryImpl.class.getName(), "loadLeaderboardGroup", e);
        }
        
        return leaderboardGroup;
    }

    @Override
    public Iterable<LeaderboardGroup> getAllLeaderboardGroups(RegattaRegistry regattaRegistry, LeaderboardRegistry leaderboardRegistry) {
        DBCollection leaderboardGroupCollection = database.getCollection(CollectionNames.LEADERBOARD_GROUPS.name());
        Set<LeaderboardGroup> leaderboardGroups = new HashSet<LeaderboardGroup>();
        try {
            for (DBObject o : leaderboardGroupCollection.find()) {
                leaderboardGroups.add(loadLeaderboardGroup(o, regattaRegistry, leaderboardRegistry));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load leaderboard groups.");
            logger.throwing(DomainObjectFactoryImpl.class.getName(), "loadLeaderboardGroup", e);
        }
        
        return leaderboardGroups;
    }
    
    private LeaderboardGroup loadLeaderboardGroup(DBObject o, RegattaRegistry regattaRegistry, LeaderboardRegistry leaderboardRegistry) {
        DBCollection leaderboardCollection = database.getCollection(CollectionNames.LEADERBOARDS.name());
        String name = (String) o.get(FieldNames.LEADERBOARD_GROUP_NAME.name());
        String description = (String) o.get(FieldNames.LEADERBOARD_GROUP_DESCRIPTION.name());
        boolean displayGroupsInReverseOrder = false; // default value 
        Object displayGroupsInReverseOrderObj = o.get(FieldNames.LEADERBOARD_GROUP_DISPLAY_IN_REVERSE_ORDER.name());
        if (displayGroupsInReverseOrderObj != null) {
            displayGroupsInReverseOrder = (Boolean) displayGroupsInReverseOrderObj; 
        }
        ArrayList<Leaderboard> leaderboards = new ArrayList<Leaderboard>();
        BasicDBList dbLeaderboardIds = (BasicDBList) o.get(FieldNames.LEADERBOARD_GROUP_LEADERBOARDS.name());
        for (Object object : dbLeaderboardIds) {
            ObjectId dbLeaderboardId = (ObjectId) object;
            DBObject dbLeaderboard = leaderboardCollection.findOne(dbLeaderboardId);
            final Leaderboard loadedLeaderboard = loadLeaderboard(dbLeaderboard, regattaRegistry,
                    leaderboardRegistry, /* groupForMetaLeaderboard */ null);
            if (loadedLeaderboard != null) {
                leaderboards.add(loadedLeaderboard);
            }
        }
        logger.info("loaded leaderboard group "+name);
        LeaderboardGroupImpl result = new LeaderboardGroupImpl(name, description, displayGroupsInReverseOrder, leaderboards);
        Object overallLeaderboardIdOrName = o.get(FieldNames.LEADERBOARD_GROUP_OVERALL_LEADERBOARD.name());
        if (overallLeaderboardIdOrName != null) {
            final DBObject dbOverallLeaderboard;
            if (overallLeaderboardIdOrName instanceof ObjectId) {
                dbOverallLeaderboard = leaderboardCollection.findOne(overallLeaderboardIdOrName);
            } else {
                dbOverallLeaderboard = (DBObject) overallLeaderboardIdOrName;
            }
            if (dbOverallLeaderboard != null) {
                // the loadLeaderboard call adds the overall leaderboard to the leaderboard registry and sets it as the
                // overall leaderboard of the leaderboard group
                loadLeaderboard(dbOverallLeaderboard, regattaRegistry, leaderboardRegistry, /* groupForMetaLeaderboard */ result);
            }
        }
        return result;
    }
    
    @Override
    public Iterable<Leaderboard> getLeaderboardsNotInGroup(RegattaRegistry regattaRegistry, LeaderboardRegistry leaderboardRegistry) {
        DBCollection leaderboardCollection = database.getCollection(CollectionNames.LEADERBOARDS.name());
        Set<Leaderboard> result = new HashSet<Leaderboard>();
        try {
            // Don't change the query object, unless you know what you're doing
            BasicDBObject query = new BasicDBObject("$where", "function() { return db." + CollectionNames.LEADERBOARD_GROUPS.name() + ".find({ "
                    + FieldNames.LEADERBOARD_GROUP_LEADERBOARDS.name() + ": this._id }).count() == 0 && "
                            + "db."+CollectionNames.LEADERBOARD_GROUPS.name()+".find({ "+FieldNames.LEADERBOARD_GROUP_OVERALL_LEADERBOARD.name() + ": this._id }).count() == 0 && "
                            + "db."+CollectionNames.LEADERBOARD_GROUPS.name()+".find({ "+FieldNames.LEADERBOARD_GROUP_OVERALL_LEADERBOARD.name()+
                            ": this."+FieldNames.LEADERBOARD_NAME.name()+" }).count() == 0; }}");
            for (DBObject o : leaderboardCollection.find(query)) {
                final Leaderboard loadedLeaderboard = loadLeaderboard(o, regattaRegistry, leaderboardRegistry, /* groupForMetaLeaderboard */ null);
                if (loadedLeaderboard != null) {
                    result.add(loadedLeaderboard);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load leaderboards.");
            logger.throwing(DomainObjectFactoryImpl.class.getName(), "getAllLeaderboards", e);
        }
        return result;
    }

    @Override
    public WindTrack loadWindTrack(Regatta regatta, RaceDefinition race, WindSource windSource, long millisecondsOverWhichToAverage) {
        final WindTrack result;
        Map<WindSource, WindTrack> resultMap = loadWindTracks(regatta, race, windSource, millisecondsOverWhichToAverage);
        if (resultMap.containsKey(windSource)) {
            result = resultMap.get(windSource);
        } else {
            // create an empty wind track as result if no fixes were found in store for the wind source requested
            result = new WindTrackImpl(millisecondsOverWhichToAverage, windSource.getType().getBaseConfidence(),
                windSource.getType().useSpeed(),
                /* nameForReadWriteLock */ WindTrackImpl.class.getSimpleName()+" for source "+windSource.toString());
        }
        return result;
    }

    @Override
    public Map<? extends WindSource, ? extends WindTrack> loadWindTracks(Regatta regatta, RaceDefinition race,
            long millisecondsOverWhichToAverageWind) {
        Map<WindSource, WindTrack> result = loadWindTracks(regatta, race, /* constrain wind source */ null, millisecondsOverWhichToAverageWind);
        return result;
    }

    /**
     * @param constrainToWindSource
     *            if <code>null</code>, wind for all sources will be loaded; otherwise, only wind data for the wind
     *            source specified by this argument will be loaded
     */
    private Map<WindSource, WindTrack> loadWindTracks(Regatta regatta, RaceDefinition race,
            WindSource constrainToWindSource, long millisecondsOverWhichToAverageWind) {
        Map<WindSource, WindTrack> result = new HashMap<WindSource, WindTrack>();
        try {
            DBCollection windTracks = database.getCollection(CollectionNames.WIND_TRACKS.name());
            ensureIndicesOnWindTracks(windTracks);
            BasicDBObject queryById = new BasicDBObject();
            queryById.put(FieldNames.RACE_ID.name(), race.getId());
            if (constrainToWindSource != null) {
                queryById.put(FieldNames.WIND_SOURCE_NAME.name(), constrainToWindSource.name());
            }
            for (DBObject dbWind : windTracks.find(queryById)) {
                loadWindFix(result, dbWind, millisecondsOverWhichToAverageWind);
            }
            BasicDBObject queryByName = new BasicDBObject();
            queryByName.put(FieldNames.EVENT_NAME.name(), regatta.getName());
            queryByName.put(FieldNames.RACE_NAME.name(), race.getName());
            if (constrainToWindSource != null) {
                queryByName.put(FieldNames.WIND_SOURCE_NAME.name(), constrainToWindSource.name());
            }
            final DBCursor windFixesFoundByName = windTracks.find(queryByName);
            if (windFixesFoundByName.hasNext()) {
                List<DBObject> windFixesToMigrate = new ArrayList<DBObject>();
                for (DBObject dbWind : windFixesFoundByName) {
                    Pair<Wind, WindSource> wind = loadWindFix(result, dbWind, millisecondsOverWhichToAverageWind);
                    // write the wind fix with the new ID-based key and remove the legacy wind fix from the DB
                    windFixesToMigrate.add(new MongoObjectFactoryImpl(database).storeWindTrackEntry(race, regatta.getName(),
                            wind.getB(), wind.getA()));
                }
                logger.info("Migrating "+windFixesFoundByName.size()+" wind fixes of regatta "+regatta.getName()+
                        " and race "+race.getName()+" to ID-based keys");
                windTracks.insert(windFixesToMigrate.toArray(new DBObject[windFixesToMigrate.size()]));
                logger.info("Removing "+windFixesFoundByName.size()+" wind fixes that were keyed by the names of regatta "+regatta.getName()+
                        " and race "+race.getName());
                windTracks.remove(queryByName);
            }
        } catch (Exception e) {
             // something went wrong during DB access; report, then use empty new wind track
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load recorded wind data. Check MongoDB settings.");
            logger.throwing(DomainObjectFactoryImpl.class.getName(), "loadWindTrack", e);
        }
        return result;
    }

    private Pair<Wind, WindSource> loadWindFix(Map<WindSource, WindTrack> result, DBObject dbWind, long millisecondsOverWhichToAverageWind) {
        Wind wind = loadWind((DBObject) dbWind.get(FieldNames.WIND.name()));
        WindSourceType windSourceType = WindSourceType.valueOf((String) dbWind.get(FieldNames.WIND_SOURCE_NAME.name()));
        WindSource windSource;
        if (dbWind.containsField(FieldNames.WIND_SOURCE_ID.name())) {
            windSource = new WindSourceWithAdditionalID(windSourceType, (String) dbWind.get(FieldNames.WIND_SOURCE_ID.name()));
        } else {
            windSource = new WindSourceImpl(windSourceType);
        }
        WindTrack track = result.get(windSource);
        if (track == null) {
            track = new WindTrackImpl(millisecondsOverWhichToAverageWind, windSource.getType().getBaseConfidence(),
                    windSource.getType().useSpeed(),
                    /* nameForReadWriteLock */ WindTrackImpl.class.getSimpleName()+" for source "+windSource.toString());
            result.put(windSource, track);
        }
        track.add(wind);
        return new Pair<Wind, WindSource>(wind, windSource);
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

    @Override
    public Iterable<Event> loadAllEvents() {
        ArrayList<Event> result = new ArrayList<Event>();
        DBCollection eventCollection = database.getCollection(CollectionNames.EVENTS.name());
        
        try {
            for (DBObject o : eventCollection.find()) {
                result.add(loadEvent(o));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load events.");
            logger.throwing(DomainObjectFactoryImpl.class.getName(), "loadEvents", e);
        }
        
        return result;
    }
    
    /**
     * An event doesn't store its regattas; it's the regatta that stores a reference to its event; the regatta
     * needs to add itself to the event when loaded or instantiated.
     */
    private Event loadEvent(DBObject eventDBObject) {
        String name = (String) eventDBObject.get(FieldNames.EVENT_NAME.name());
        Serializable id = (Serializable) eventDBObject.get(FieldNames.EVENT_ID.name());
        String publicationUrl = (String) eventDBObject.get(FieldNames.EVENT_PUBLICATION_URL.name());
        boolean isPublic = eventDBObject.get(FieldNames.EVENT_IS_PUBLIC.name()) != null ? (Boolean) eventDBObject.get(FieldNames.EVENT_IS_PUBLIC.name()) : false;
        Venue venue = loadVenue((DBObject) eventDBObject.get(FieldNames.VENUE.name()));
        Event result = new EventImpl(name, venue, publicationUrl, isPublic, id);
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
        Serializable id = (Serializable) courseAreaDBObject.get(FieldNames.COURSE_AREA_ID.name());
        return new CourseAreaImpl(name, id);
    }
    
    @Override
    public Iterable<Regatta> loadAllRegattas(TrackedRegattaRegistry trackedRegattaRegistry) {
        List<Regatta> result = new ArrayList<Regatta>();
        DBCollection regattaCollection = database.getCollection(CollectionNames.REGATTAS.name());
        for (DBObject dbRegatta : regattaCollection.find()) {
            result.add(loadRegatta(dbRegatta, trackedRegattaRegistry));
        }
        return result;
    }

    @Override
    public Regatta loadRegatta(String name, TrackedRegattaRegistry trackedRegattaRegistry) {
        DBObject query = new BasicDBObject(FieldNames.REGATTA_NAME.name(), name);
        DBCollection regattaCollection = database.getCollection(CollectionNames.REGATTAS.name());
        DBObject dbRegatta = regattaCollection.findOne(query);
        Regatta result = loadRegatta(dbRegatta, trackedRegattaRegistry);
        assert result == null || result.getName().equals(name);
        return result;
    }

    private Regatta loadRegatta(DBObject dbRegatta, TrackedRegattaRegistry trackedRegattaRegistry) {
        Regatta result = null;
        if (dbRegatta != null) {
            String baseName = (String) dbRegatta.get(FieldNames.REGATTA_BASE_NAME.name());
            String boatClassName = (String) dbRegatta.get(FieldNames.BOAT_CLASS_NAME.name());
            Serializable id = (Serializable) dbRegatta.get(FieldNames.REGATTA_ID.name());
            if (id == null) {
                id = baseName + "("+boatClassName+")";
            }
            BoatClass boatClass = null;
            if (boatClassName != null) {
                boolean typicallyStartsUpwind = (Boolean) dbRegatta.get(FieldNames.BOAT_CLASS_TYPICALLY_STARTS_UPWIND.name());
                boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass(boatClassName, typicallyStartsUpwind);
            }
            BasicDBList dbSeries = (BasicDBList) dbRegatta.get(FieldNames.REGATTA_SERIES.name());
            Iterable<Series> series = loadSeries(dbSeries, trackedRegattaRegistry);
            result = new RegattaImpl(baseName, boatClass, series, /* persistent */ true, loadScoringScheme(dbRegatta), id);
        }
        return result;
    }

    private ScoringSchemeType getScoringSchemeType(DBObject dbObject) {
        String scoringSchemeTypeName = (String) dbObject.get(FieldNames.SCORING_SCHEME_TYPE.name());
        ScoringSchemeType scoringSchemeType;
        if (scoringSchemeTypeName == null) {
            scoringSchemeType = ScoringSchemeType.LOW_POINT; // the default
        } else {
            scoringSchemeType = ScoringSchemeType.valueOf(scoringSchemeTypeName);
        }
        return scoringSchemeType;
    }

    private Iterable<Series> loadSeries(BasicDBList dbSeries, TrackedRegattaRegistry trackedRegattaRegistry) {
        List<Series> result = new ArrayList<Series>();
        for (Object o : dbSeries) {
            DBObject oneDBSeries = (DBObject) o;
            Series series = loadSeries(oneDBSeries, trackedRegattaRegistry);
            result.add(series);
        }
        return result;
    }

    private Series loadSeries(DBObject dbSeries, TrackedRegattaRegistry trackedRegattaRegistry) {
        String name = (String) dbSeries.get(FieldNames.SERIES_NAME.name());
        boolean isMedal = (Boolean) dbSeries.get(FieldNames.SERIES_IS_MEDAL.name());
        final BasicDBList dbFleets = (BasicDBList) dbSeries.get(FieldNames.SERIES_FLEETS.name());
        Map<String, Fleet> fleetsByName = loadFleets(dbFleets);
        BasicDBList dbRaceColumns = (BasicDBList) dbSeries.get(FieldNames.SERIES_RACE_COLUMNS.name());
        Iterable<String> raceColumnNames = loadRaceColumnNames(dbRaceColumns, fleetsByName);
        Series series = new SeriesImpl(name, isMedal, fleetsByName.values(), raceColumnNames, trackedRegattaRegistry);
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
        Fleet result;
        String name = (String) dbFleet.get(FieldNames.FLEET_NAME.name());
        Integer ordering = (Integer) dbFleet.get(FieldNames.FLEET_ORDERING.name());
        if (ordering == null) {
            ordering = 0;
        }
        Integer colorAsInt = (Integer) dbFleet.get(FieldNames.FLEET_COLOR.name());
        Color color = null;
        if (colorAsInt != null) {
            int r = colorAsInt % 256;
            int g = (colorAsInt / 256 ) % 256;
            int b = (colorAsInt / 256 / 256) % 256;
            color = new RGBColor(r, g, b);
        }
        result = new FleetImpl(name, ordering, color);
        return result;
    }
    
    @Override
    public Map<String, Regatta> loadRaceIDToRegattaAssociations(RegattaRegistry regattaRegistry) {
        DBCollection raceIDToRegattaCollection = database.getCollection(CollectionNames.REGATTA_FOR_RACE_ID.name());
        Map<String, Regatta> result = new HashMap<String, Regatta>();
        for (DBObject o : raceIDToRegattaCollection.find()) {
            Regatta regatta = regattaRegistry.getRegattaByName((String) o.get(FieldNames.REGATTA_NAME.name()));
            if (regatta != null) {
                result.put((String) o.get(FieldNames.RACE_ID_AS_STRING.name()), regatta);
            } else {
                logger.warning("Couldn't find regatta " + o.get(FieldNames.REGATTA_NAME.name())
                        + ". Cannot restore race associations with this regatta.");
            }
        }
        return result;
    }

	@Override
	public RaceLog loadRaceLog(RaceColumnIdentifier identifier) {
		RaceLog result = new RaceLogImpl(RaceLogImpl.class.getSimpleName());
        try {
            BasicDBObject query = new BasicDBObject();
            query.put(FieldNames.RACE_COLUMN_IDENTIFIER.name(), identifier.getIdentifier());
            DBCollection raceLog = database.getCollection(CollectionNames.RACE_LOGS.name());
            for (DBObject o : raceLog.find(query)) {
                RaceLogEvent raceLogEvent = loadRaceLogEvent((DBObject) o.get(FieldNames.RACE_LOG_EVENT.name()));
                
                if (raceLogEvent != null)
                	result.add(raceLogEvent);
            }
        } catch (Throwable t) {
             // something went wrong during DB access; report, then use empty new race log
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load recorded race log data. Check MongoDB settings.");
            logger.throwing(DomainObjectFactoryImpl.class.getName(), "loadRaceLog", t);
        }
        return result;
	}

	public RaceLogEvent loadRaceLogEvent(DBObject dbObject) {
		TimePoint timePoint = loadTimePoint(dbObject);
		Serializable id = (Serializable) dbObject.get(FieldNames.RACE_LOG_EVENT_ID.name());
		Integer passId = (Integer) dbObject.get(FieldNames.RACE_LOG_EVENT_PASS_ID.name());
		//BasicDBList dbList = (BasicDBList) dbObject.get(FieldNames.RC_EVENT_INVOLVED_BOATS.name());
		List<Competitor> competitors = new ArrayList<Competitor>();
		
		String eventClass = (String) dbObject.get(FieldNames.RACE_LOG_EVENT_CLASS.name());
		if (eventClass.equals(RaceLogStartTimeEvent.class.getSimpleName())) {
			return loadRaceLogStartTimeEvent(timePoint, id, passId, competitors, dbObject);
		} else if (eventClass.equals(RaceLogFlagEvent.class.getSimpleName())) {
			return loadRaceLogFlagEvent(timePoint, id, passId, competitors, dbObject);
		}
		
		return null;
	}

	private RaceLogEvent loadRaceLogFlagEvent(TimePoint timePoint, Serializable id, Integer passId, List<Competitor> competitors, DBObject dbObject) {
		Flags upperFlag = Flags.valueOf((String) dbObject.get(FieldNames.RACE_LOG_EVENT_FLAG_UPPER.name()));
		Flags lowerFlag = Flags.valueOf((String) dbObject.get(FieldNames.RACE_LOG_EVENT_FLAG_LOWER.name()));
		Boolean displayed = Boolean.valueOf((String) dbObject.get(FieldNames.RACE_LOG_EVENT_FLAG_DISPLAYED.name()));
		
		if (upperFlag == null || lowerFlag == null || displayed == null) {
			return null;
		}
		
		return RaceLogEventFactory.INSTANCE.createFlagEvent(timePoint, id, competitors, passId, upperFlag, lowerFlag, displayed);
	}

	private RaceLogStartTimeEvent loadRaceLogStartTimeEvent(TimePoint timePoint, Serializable id, Integer passId, List<Competitor> competitors, DBObject dbObject) {
		Long startTimeInMillis = (Long) dbObject.get(FieldNames.RACE_LOG_EVENT_START_TIME.name());
		if (startTimeInMillis == null) {
			return null;
		}
		TimePoint startTime = new MillisecondsTimePoint(startTimeInMillis);
		return RaceLogEventFactory.INSTANCE.createStartTimeEvent(timePoint, passId, competitors, passId, startTime);
	}
}

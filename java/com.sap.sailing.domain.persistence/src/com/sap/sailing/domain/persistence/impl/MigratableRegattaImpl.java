package com.sap.sailing.domain.persistence.impl;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.MigratableRegatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.ranking.RankingMetricConstructor;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class MigratableRegattaImpl extends RegattaImpl implements MigratableRegatta {
    private static final long serialVersionUID = -3545488249832218320L;
    private static final Logger logger = Logger.getLogger(MigratableRegattaImpl.class.getName());
    
    /**
     * To be used for storing a migrated regatta
     */
    private transient final MongoObjectFactory mongoObjectFactory;

    public <S extends Series> MigratableRegattaImpl(RaceLogStore raceLogStore, RegattaLogStore regattaLogStore, String name,
            BoatClass boatClass, boolean canBoatsOfCompetitorsChangePerRace, CompetitorRegistrationType competitorRegistrationType,
            TimePoint startDate, TimePoint endDate,
            Iterable<S> series, boolean persistent, ScoringScheme scoringScheme, Serializable id, CourseArea courseArea,
            Double buoyZoneRadiusInHullLengths, boolean useStartTimeInference,
            boolean controlTrackingFromStartAndFinishTimes, RankingMetricConstructor rankingMetricConstructor,
            MongoObjectFactory mongoObjectFactory, String registrationLinkSecret) {
        super(raceLogStore, regattaLogStore, name, boatClass, canBoatsOfCompetitorsChangePerRace, competitorRegistrationType, startDate, endDate, series,
                persistent, scoringScheme, id, courseArea, buoyZoneRadiusInHullLengths, useStartTimeInference,
                controlTrackingFromStartAndFinishTimes, rankingMetricConstructor,
                registrationLinkSecret);
        this.mongoObjectFactory = mongoObjectFactory;
        mongoObjectFactory.storeRegatta(this); // make sure the canBoatsOfCompetitorsChangePerRace flag makes it into the DB
    }
    
    public synchronized void migrateCanBoatsOfCompetitorsChangePerRace() {
        // It should not be possible to call this after races have been already added to this regatta.
        assert !canBoatsOfCompetitorsChangePerRace() && Util.size(getAllRaces()) == 0;
        super.setCanBoatsOfCompetitorsChangePerRace(true);
        if (mongoObjectFactory != null) {
            logger.log(Level.INFO, "Bug2822 DB-Migration: Store migrated regatta '" + getName() + "' having now canBoatsOfCompetitorsChangePerRace=true.");
            mongoObjectFactory.storeRegatta(this);
        }
    }
}

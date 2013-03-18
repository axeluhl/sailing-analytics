package com.sap.sailing.racecommittee.app.domain.impl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import android.annotation.SuppressLint;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Gate;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.GateImpl;
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.NauticalSide;

public enum DomainFactoryImpl implements SharedDomainFactory {
    INSTANCE;
    
    private final Map<String, Nationality> nationalityCache = new HashMap<String, Nationality>();
    private final Map<Serializable, Mark> markCache = new HashMap<Serializable, Mark>();
    private final Map<String, Serializable> markIdCache = new HashMap<String, Serializable>();
    private final Map<String, BoatClass> boatClassCache = new HashMap<String, BoatClass>();
    private final ConcurrentHashMap<Serializable, Waypoint> waypointCache = new ConcurrentHashMap<Serializable, Waypoint>();
    private final Set<String> mayStartWithNoUpwindLeg = new HashSet<String>(Arrays.asList(new String[] { "extreme40", "ess", "ess40" }));
    private final Map<Serializable, Competitor> competitorCache = new HashMap<Serializable, Competitor>();

    public Nationality getOrCreateNationality(String threeLetterIOCCode) {
        synchronized (nationalityCache) {
            Nationality result = nationalityCache.get(threeLetterIOCCode);
            if (result == null) {
                result = new NationalityImpl(threeLetterIOCCode);
                nationalityCache.put(threeLetterIOCCode, result);
            }
            return result;
        }
    }

    public Mark getOrCreateMark(String name) {
        return getOrCreateMark(name, name);
    }

    public Mark getOrCreateMark(String toStringRepresentationOfID, String name) {
        final Mark result;
        if (markIdCache.containsKey(toStringRepresentationOfID)) {
            Serializable id = markIdCache.get(toStringRepresentationOfID);
            result = getOrCreateMark(id, name);
        } else {
            result = new MarkImpl(toStringRepresentationOfID, name);
            cacheMark(toStringRepresentationOfID, result);
        }
        return result;
    }

    public Mark getOrCreateMark(Serializable id, String name) {
        Mark result = markCache.get(id);
        if (result == null) {
            result = new MarkImpl(id, name);
            cacheMark(id, result);
        }
        return result;
    }

    public Mark getOrCreateMark(Serializable id, String name, MarkType type, String color, String shape, String pattern) {
        Mark result = markCache.get(id);
        if (result == null) {
            result = new MarkImpl(id, name, type, color, shape, pattern);
            cacheMark(id, result);
        }
        return result;
    }
    
    private void cacheMark(Serializable id, Mark result) {
        markCache.put(id, result);
        markIdCache.put(id.toString(), id);
    }

    public Gate createGate(Mark left, Mark right, String name) {
        return new GateImpl(left, right, name);
    }

    public Gate createGate(Serializable id, Mark left, Mark right, String name) {
        return new GateImpl(id, left, right, name);
    }

    public Waypoint createWaypoint(ControlPoint controlPoint, NauticalSide passingSide) {
        synchronized (waypointCache) {
            Waypoint result = new WaypointImpl(controlPoint, passingSide);
            waypointCache.put(result.getId(), result);
            return result;
        }
    }

    public Waypoint getExistingWaypointById(Waypoint waypointPrototype) {
        // TODO Auto-generated method stub
        return null;
    }

    public Waypoint getExistingWaypointByIdOrCache(Waypoint waypoint) {
        // TODO Auto-generated method stub
        return null;
    }

    public BoatClass getOrCreateBoatClass(String name, boolean typicallyStartsUpwind) {
        synchronized (boatClassCache) {
            BoatClass result = boatClassCache.get(name);
            if (result == null) {
                result = new BoatClassImpl(name, typicallyStartsUpwind);
                boatClassCache.put(name, result);
            }
            return result;
        }
    }

    @SuppressLint("DefaultLocale")
    public BoatClass getOrCreateBoatClass(String name) {
        return getOrCreateBoatClass(name, /* typicallyStartsUpwind */!mayStartWithNoUpwindLeg.contains(name.toLowerCase()));
    }

    public Competitor getExistingCompetitorById(Serializable competitorId) {
        return competitorCache.get(competitorId);
    }

    public Competitor createCompetitor(Serializable id, String name, Team team, Boat boat) {
        Competitor result = new CompetitorImpl(id, name, team, boat);
        competitorCache.put(id, result);
        return result;
    }

    public Competitor getOrCreateCompetitor(Serializable competitorId, String name, Team team, Boat boat) {
        Competitor result = getExistingCompetitorById(competitorId);
        if (result == null) {
            result = createCompetitor(competitorId, name, team, boat);
        }
        return result;
    }

}

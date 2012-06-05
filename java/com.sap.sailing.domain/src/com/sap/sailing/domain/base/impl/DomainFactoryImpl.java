package com.sap.sailing.domain.base.impl;

import java.awt.TrayIcon.MessageType;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Gate;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.ObjectInputStreamResolvingAgainstDomainFactory;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;

public class DomainFactoryImpl implements DomainFactory {
    /**
     * Ensure that the <em>same</em> string is used as key that is also used to set the {@link Nationality}
     * object's {@link Nationality#getThreeLetterIOCAcronym() IOC code}.
     */
    private final Map<String, Nationality> nationalityCache;
    
    private final Map<String, Buoy> buoyCache;
    
    private final Map<String, BoatClass> boatClassCache;
    
    private final Map<Serializable, Competitor> competitorCache;
    
    private final WeakHashMap<Serializable, Waypoint> waypointCache;

    private final Set<String> mayStartWithNoUpwindLeg;
    
    public DomainFactoryImpl() {
        nationalityCache = new HashMap<String, Nationality>();
        buoyCache = new HashMap<String, Buoy>();
        boatClassCache = new HashMap<String, BoatClass>();
        competitorCache = new HashMap<Serializable, Competitor>();
        waypointCache = new WeakHashMap<Serializable, Waypoint>();
        mayStartWithNoUpwindLeg = new HashSet<String>(Arrays.asList(new String[] { "extreme40", "ess", "ess40" }));
    }
    
    @Override
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
    
    /**
     * @param id
     *            the ID which is probably also used as the "device name" and the "sail number" in case of an
     *            {@link MessageType#RPD RPD} message
     */
    @Override
    public Buoy getOrCreateBuoy(String id) {
        Buoy result = buoyCache.get(id);
        if (result == null) {
            result = new BuoyImpl(id);
            buoyCache.put(id, result);
        }
        return result;
    }

    @Override
    public Gate createGate(Buoy left, Buoy right, String name) {
       return new GateImpl(left, right, name);
    }

    @Override
    public synchronized Waypoint createWaypoint(ControlPoint controlPoint) {
        Waypoint result = new WaypointImpl(controlPoint);
        waypointCache.put(result.getId(), result);
        return result;
    }

    @Override
    public synchronized void cacheWaypoint(Waypoint waypoint) {
        if (getExistingWaypointById(waypoint.getId()) != null) {
            throw new IllegalArgumentException("Trying to cache an already cached waypoint: "+waypoint);
        }
        waypointCache.put(waypoint.getId(), waypoint);
    }

    @Override
    public Waypoint getExistingWaypointById(Serializable id) {
        return waypointCache.get(id);
    }

    @Override
    public MarkPassing createMarkPassing(TimePoint timePoint, Waypoint waypoint, Competitor competitor) {
        return new MarkPassingImpl(timePoint, waypoint, competitor);
    }

    @Override
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
    
    @Override
    public BoatClass getOrCreateBoatClass(String name) {
        return getOrCreateBoatClass(name, /* typicallyStartsUpwind */!mayStartWithNoUpwindLeg.contains(name.toLowerCase()));
    }

    @Override
    public Competitor getExistingCompetitorById(Serializable competitorId) {
        return competitorCache.get(competitorId);
    }

    @Override
    public synchronized Competitor createCompetitor(Serializable id, String name, Team team, Boat boat) {
        Competitor result = new CompetitorImpl(id, name, team, boat);
        competitorCache.put(id, result);
        return result;
    }
    
    @Override
    public synchronized Competitor getOrCreateCompetitor(Serializable competitorId, String name, Team team, Boat boat) {
        Competitor result = getExistingCompetitorById(competitorId);
        if (result == null) {
            result = createCompetitor(competitorId, name, team, boat);
        }
        return result;
    }

    @Override
    public ObjectInputStreamResolvingAgainstDomainFactory createObjectInputStreamResolvingAgainstThisFactory(
            InputStream inputStream) throws IOException {
        return new ObjectInputStreamResolvingAgainstDomainFactoryImpl(inputStream, this);
    }

}

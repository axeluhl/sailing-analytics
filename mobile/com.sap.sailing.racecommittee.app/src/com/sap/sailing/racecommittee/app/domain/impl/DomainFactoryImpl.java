package com.sap.sailing.racecommittee.app.domain.impl;

import java.io.Serializable;

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
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.NauticalSide;

public enum DomainFactoryImpl implements SharedDomainFactory {
    INSTANCE;

    public Nationality getOrCreateNationality(String threeLetterIOCCode) {
        // TODO Auto-generated method stub
        return null;
    }

    public Mark getOrCreateMark(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    public Mark getOrCreateMark(String toStringRepresentationOfID, String name) {
        // TODO Auto-generated method stub
        return null;
    }

    public Mark getOrCreateMark(Serializable id, String name) {
        // TODO Auto-generated method stub
        return null;
    }

    public Mark getOrCreateMark(Serializable id, String name, MarkType type, String color, String shape, String pattern) {
        // TODO Auto-generated method stub
        return null;
    }

    public Gate createGate(Mark left, Mark right, String name) {
        // TODO Auto-generated method stub
        return null;
    }

    public Gate createGate(Serializable id, Mark left, Mark right, String name) {
        // TODO Auto-generated method stub
        return null;
    }

    public Waypoint createWaypoint(ControlPoint controlPoint, NauticalSide passingSide) {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
    }

    public BoatClass getOrCreateBoatClass(String name) {
        return new BoatClassImpl(name, false);
    }

    public Competitor getExistingCompetitorById(Serializable competitorId) {
        // TODO Auto-generated method stub
        return null;
    }

    public Competitor createCompetitor(Serializable id, String name, Team team, Boat boat) {
        // TODO Auto-generated method stub
        return null;
    }

    public Competitor getOrCreateCompetitor(Serializable competitorId, String name, Team team, Boat boat) {
        // TODO Auto-generated method stub
        return null;
    }

}

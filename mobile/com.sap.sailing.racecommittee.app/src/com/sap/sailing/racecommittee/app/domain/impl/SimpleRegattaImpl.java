package com.sap.sailing.racecommittee.app.domain.impl;

import java.io.Serializable;
import java.util.ArrayList;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.RegattaListener;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.leaderboard.ScoringScheme;

public class SimpleRegattaImpl implements Regatta {
	private static final long serialVersionUID = 8866928745954890844L;
	
	private Serializable id;
	private String baseName;
	private BoatClass boatClass;
	private ArrayList<RaceDefinition> races;

	public SimpleRegattaImpl(Serializable id, String baseName, BoatClass boatClass) {
		this.id = id;
		this.baseName = baseName;
		this.boatClass = boatClass;
		this.races = new ArrayList<RaceDefinition>();
	}

	public String getName() {
		return baseName;
	}

	public Serializable getId() {
		return id;
	}

	public Iterable<RaceDefinition> getAllRaces() {
		return races;
	}

	public RaceDefinition getRaceByName(String raceName) {
		for (RaceDefinition race : races) {
			if (race.getName().equals(raceName)) {
				return race;
			}
		}
		return null;
	}

	public BoatClass getBoatClass() {
		return boatClass;
	}

	public void addRace(RaceDefinition race) {
		races.add(race);
	}

	public void removeRace(RaceDefinition raceDefinition) {
		races.remove(raceDefinition);
	}

	public String getBaseName() {
		return baseName;
	}

	public ScoringScheme getScoringScheme() {
		throw new UnsupportedOperationException();
	}

	public Iterable<? extends Series> getSeries() {
		throw new UnsupportedOperationException();
	}

	public Series getSeriesByName(String seriesName) {
		throw new UnsupportedOperationException();
	}

	public Iterable<Competitor> getCompetitors() {
		throw new UnsupportedOperationException();
	}

	public boolean isPersistent() {
		throw new UnsupportedOperationException();
	}

	public void addRegattaListener(RegattaListener listener) {
		throw new UnsupportedOperationException();
	}

	public void removeRegattaListener(RegattaListener listener) {
		throw new UnsupportedOperationException();
	}

	public RegattaIdentifier getRegattaIdentifier() {
		throw new UnsupportedOperationException();
	}

	public void addRaceColumnListener(RaceColumnListener listener) {
		throw new UnsupportedOperationException();
	}

	public void removeRaceColumnListener(RaceColumnListener listener) {
		throw new UnsupportedOperationException();
	}

}

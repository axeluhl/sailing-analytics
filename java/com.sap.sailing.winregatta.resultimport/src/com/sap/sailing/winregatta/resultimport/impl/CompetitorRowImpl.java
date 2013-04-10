package com.sap.sailing.winregatta.resultimport.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.resultimport.CompetitorEntry;
import com.sap.sailing.resultimport.CompetitorRow;
import com.sap.sailing.resultimport.impl.DefaultCompetitorEntryImpl;

public class CompetitorRowImpl implements CompetitorRow
{
	private Integer totalRank;
	private String country;
	private String sailNumber;
	private String teamMember1Name;
	private String teamMember1Club;
	private String teamMember1DSVNumber;
	private String teamMember2Name;
	private String teamMember2Club;
	private String teamMember2DSVNumber;
	private String teamMember3Name;
	private String teamMember3Club;
	private String teamMember3DSVNumber;
	private Map<Integer, String> ranks;
	private Map<Integer, String> scores;
	
	private double totalScore;
	private int racesCount = 6;
	
	public CompetitorRowImpl() {
		ranks = new LinkedHashMap<Integer, String>();
		scores = new LinkedHashMap<Integer, String>();
	}

	public Object getResultKey() {
		return sailNumber;
	}

	public Integer getTotalRank() {
		return totalRank;
	}

	public void setTotalRank(Integer totalRank) {
		this.totalRank = totalRank;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getSailNumber() {
		return sailNumber;
	}

	public void setSailNumber(String sailNumber) {
		this.sailNumber = sailNumber;
	}

	public String getTeamMember1Name() {
		return teamMember1Name;
	}

	public void setTeamMember1Name(String teamMember1Name) {
		this.teamMember1Name = teamMember1Name;
	}

	public String getTeamMember1Club() {
		return teamMember1Club;
	}

	public void setTeamMember1Club(String teamMember1Club) {
		this.teamMember1Club = teamMember1Club;
	}

	public String getTeamMember1DSVNumber() {
		return teamMember1DSVNumber;
	}

	public void setTeamMember1DSVNumber(String teamMember1DSVNumber) {
		this.teamMember1DSVNumber = teamMember1DSVNumber;
	}

	public String getTeamMember2Name() {
		return teamMember2Name;
	}

	public void setTeamMember2Name(String teamMember2Name) {
		this.teamMember2Name = teamMember2Name;
	}

	public String getTeamMember2Club() {
		return teamMember2Club;
	}

	public void setTeamMember2Club(String teamMember2Club) {
		this.teamMember2Club = teamMember2Club;
	}

	public String getTeamMember2DSVNumber() {
		return teamMember2DSVNumber;
	}

	public void setTeamMember2DSVNumber(String teamMember2DSVNumber) {
		this.teamMember2DSVNumber = teamMember2DSVNumber;
	}

	public String getTeamMember3Name() {
		return teamMember3Name;
	}

	public void setTeamMember3Name(String teamMember3Name) {
		this.teamMember3Name = teamMember3Name;
	}

	public String getTeamMember3Club() {
		return teamMember3Club;
	}

	public void setTeamMember3Club(String teamMember3Club) {
		this.teamMember3Club = teamMember3Club;
	}

	public String getTeamMember3DSVNumber() {
		return teamMember3DSVNumber;
	}

	public void setTeamMember3DSVNumber(String teamMember3DSVNumber) {
		this.teamMember3DSVNumber = teamMember3DSVNumber;
	}

	public void setRace1Rank(String raceRank) {
		if(raceRank != null) {
			ranks.put(1, raceRank);
		}
	}

	public void setRace1Score(String raceScore) {
		if(raceScore != null) {
			scores.put(1, raceScore);
		}
	}

	public void setRace2Rank(String raceRank) {
		if(raceRank != null) {
			ranks.put(2, raceRank);
		}
	}

	public void setRace2Score(String raceScore) {
		if(raceScore != null) {
			scores.put(2, raceScore);
		}
	}

	public void setRace3Rank(String raceRank) {
		if(raceRank != null) {
			ranks.put(3, raceRank);
		}
	}

	public void setRace3Score(String raceScore) {
		if(raceScore != null) {
			scores.put(3, raceScore);
		}
	}

	public void setRace4Rank(String raceRank) {
		if(raceRank != null) {
			ranks.put(4, raceRank);
		}
	}

	public void setRace4Score(String raceScore) {
		if(raceScore != null) {
			scores.put(4, raceScore);
		}
	}

	public void setRace5Rank(String raceRank) {
		if(raceRank != null) {
			ranks.put(5, raceRank);
		}
	}

	public void setRace5Score(String raceScore) {
		if(raceScore != null) {
			scores.put(5, raceScore);
		}
	}

	public void setRace6Rank(String raceRank) {
		if(raceRank != null) {
			ranks.put(6, raceRank);
		}
	}

	public void setRace6Score(String raceScore) {
		if(raceScore != null) {
			scores.put(6, raceScore);
		}
	}

	public double getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(double totalScore) {
		this.totalScore = totalScore;
	}

	@Override
	public Iterable<CompetitorEntry> getRankAndMaxPointsReasonAndPointsAndDiscarded() {
	    List<CompetitorEntry> competitorEntries = new ArrayList<CompetitorEntry>();
	    for(int i = 1; i <= racesCount; i++) {
		    if(scores.containsKey(i)) {
		    	boolean discarded = false;
		    	Integer rank = null;
		        String maxPointsReason;
		    	
		        if(ranks.containsKey(i)) {
			    	String rankAsString = ranks.get(i);
			    	if(rankAsString.startsWith("(") && rankAsString.endsWith(")")) {
			    		rankAsString = rankAsString.substring(1, rankAsString.length()-1);
			    		discarded = true;
			    	}		        	
			        try {
			            rank = Integer.valueOf(rankAsString);
			        } catch (NumberFormatException nfe) {
			            rank = null;
			        }
		        }

		    	String scoreAsString = scores.get(i);
		    	if(scoreAsString.startsWith("(") && scoreAsString.endsWith(")")) {
		    		scoreAsString = scoreAsString.substring(1, scoreAsString.length()-1);
		    		discarded = true;
		    	}
		    	
		    	Double score;
		        try {
		            score= Double.valueOf(scoreAsString);
		            maxPointsReason = null;
		        } catch (NumberFormatException nfe) {
		            // must have been a disqualification / max-points-reason
		            score = null;
		            maxPointsReason = scoreAsString;
		        }
		        
		    	competitorEntries.add(new DefaultCompetitorEntryImpl(rank, maxPointsReason, score, discarded));
		    }
	    }

		return competitorEntries;
	}

	@Override
	public Double getNetPointsBeforeDiscarding() {
		return null;
	}

	@Override
	public Double getScoreAfterDiscarding() {
		return null;
	}

	@Override
	public Iterable<String> getNames() {
		List<String> names = new ArrayList<String>();
		if(teamMember1Name != null) {
			names.add(teamMember1Name);
		}
		if(teamMember2Name != null) {
			names.add(teamMember2Name);
		}
		if(teamMember3Name != null) {
			names.add(teamMember3Name);
		}
		return names;
	}

	@Override
	public String getCompetitorName() {
		return teamMember1Name;
	}

	@Override
	public String getSailID() {		
		return country.trim() + sailNumber.trim();
	}
}

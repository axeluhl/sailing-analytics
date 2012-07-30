package com.sap.sailing.winregatta.resultimport.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.resultimport.CompetitorEntry;
import com.sap.sailing.resultimport.CompetitorRow;

public class CompetitorResultImpl implements CompetitorRow
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
	private String race1Rank;
	private double race1Score;
	private String race2Rank;
	private double race2Score;
	private double totalScore;
	
	public CompetitorResultImpl() {
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

	public String getRace1Rank() {
		return race1Rank;
	}

	public void setRace1Rank(String race1Rank) {
		this.race1Rank = race1Rank;
	}

	public double getRace1Score() {
		return race1Score;
	}

	public void setRace1Score(double race1Score) {
		this.race1Score = race1Score;
	}

	public String getRace2Rank() {
		return race2Rank;
	}

	public void setRace2Rank(String race2Rank) {
		this.race2Rank = race2Rank;
	}

	public double getRace2Score() {
		return race2Score;
	}

	public void setRace2Score(double race2Score) {
		this.race2Score = race2Score;
	}

	public double getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(double totalScore) {
		this.totalScore = totalScore;
	}

	@Override
	public Iterable<CompetitorEntry> getRankAndMaxPointsReasonAndPointsAndDiscarded() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getTotalPointsBeforeDiscarding() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getScoreAfterDiscarding() {
		// TODO Auto-generated method stub
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
	public String getTeamName() {
		return "";
	}

	@Override
	public String getSailID() {		
		return country.trim() + sailNumber.trim();
	}

	@Override
	public String getClubName() {
		return "";
	}
}

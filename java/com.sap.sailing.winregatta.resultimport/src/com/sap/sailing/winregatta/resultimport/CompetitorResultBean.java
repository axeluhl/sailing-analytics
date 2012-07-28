package com.sap.sailing.winregatta.resultimport;

import java.util.Date;

public class CompetitorResultBean
{
	private String totalRank;
	private String country;
	private String sailID;
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
	private String race1Score;
	private String race2Rank;
	private String race2Score;
	private String totalScore;
	
	public CompetitorResultBean() {
	}

	public Object getResultKey() {
		return sailID;
	}

	public String getTotalRank() {
		return totalRank;
	}

	public void setTotalRank(String totalRank) {
		this.totalRank = totalRank;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getSailID() {
		return sailID;
	}

	public void setSailID(String sailID) {
		this.sailID = sailID;
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

	public String getRace1Score() {
		return race1Score;
	}

	public void setRace1Score(String race1Score) {
		this.race1Score = race1Score;
	}

	public String getRace2Rank() {
		return race2Rank;
	}

	public void setRace2Rank(String race2Rank) {
		this.race2Rank = race2Rank;
	}

	public String getRace2Score() {
		return race2Score;
	}

	public void setRace2Score(String race2Score) {
		this.race2Score = race2Score;
	}

	public String getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(String totalScore) {
		this.totalScore = totalScore;
	}

	private class ExcelDateFormatter {
		String valueAsString;
		Date valueAsDate;

		public ExcelDateFormatter(String importedDate) {
			if (importedDate != null && !importedDate.isEmpty()) {
				// the format is in excel time format (number between 0 and 1)
				try {
					Double value = Double.parseDouble(importedDate) * 86400;
					Long valueInSeconds = value.longValue();

					this.valueAsDate = new Date(valueInSeconds * 1000);
					this.valueAsString = String
							.format("%02d:%02d:%02d", valueInSeconds / 3600,
									(valueInSeconds % 3600) / 60,
									(valueInSeconds % 60));
				} catch (NumberFormatException e) {
					this.valueAsString = "Invalid format: " + importedDate;
				}
			} else {
				valueAsString = importedDate;
				valueAsDate = null;
			}
		}
	}
}

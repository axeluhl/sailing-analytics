package com.sap.sailing.winregatta.resultimport.impl;

import java.util.HashMap;
import java.util.Map;

public class RegattaInfo {
	private String eventName;
	private String boatClass;
	private String date;

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		String prefix = "Veranstaltung: ";
		if(eventName.startsWith(prefix)) {
			this.eventName = eventName.substring(prefix.length(), eventName.length()); 
		} else {
			this.eventName = eventName;
		}
	}

	public String getBoatClass() {
		return boatClass;
	}

	public void setBoatClass(String boatClass) {
		String prefix = "Klasse: ";
		if(boatClass.startsWith(prefix)) {
			this.boatClass = boatClass.substring(prefix.length(), boatClass.length()); 
		} else {
			this.boatClass = boatClass;
		}
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		String prefix = "Datum: ";
		if(date.startsWith(prefix)) {
			this.date = date.substring(prefix.length(), date.length()); 
		} else {
			this.date = date;
		}
	}
	
	public Map<String, String> toMap() {
		Map<String, String> dataAsMap = new  HashMap<String, String>();
		if(eventName != null) {
			dataAsMap.put("eventName", eventName);
		}
		if(boatClass != null) {
			dataAsMap.put("boatClass", boatClass);
		}
		if(date != null) {
			dataAsMap.put("date", date);
		}
		return dataAsMap;
	}
}

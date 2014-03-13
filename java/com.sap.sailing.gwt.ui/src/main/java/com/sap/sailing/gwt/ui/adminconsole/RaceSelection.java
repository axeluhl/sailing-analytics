package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.core.client.JavaScriptObject;

public class RaceSelection extends JavaScriptObject {

	protected RaceSelection() {};

	public static final native RaceSelection create() /*-{return [];}-*/;
	
	public final native String toJson() /*-{return JSON.stringify(this);}-*/;
	
	public final native void addRace(RaceEntry race) /*-{

		this.push(race);
	
	}-*/;

	public static class RaceEntry extends JavaScriptObject {

		protected RaceEntry() {};

		public static final native RaceEntry create() /*-{return {};}-*/;
		
		public final native void setRaceName(String raceName) /*-{this.race = raceName;}-*/;

		public final native void setRegatteName(String regatteName) /*-{this.regatta = regatteName;}-*/;
	}

}

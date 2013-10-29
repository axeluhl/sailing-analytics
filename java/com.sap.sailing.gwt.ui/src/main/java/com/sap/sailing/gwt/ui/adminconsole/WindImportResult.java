package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsDate;

public class WindImportResult extends JavaScriptObject {
	
	protected WindImportResult() {
	};
	
	static public final native WindImportResult fromJson(String json) /*-{return JSON.parse(json);}-*/;
	
	public final native String getError() /*-{return this.error;}-*/;
	
	public final native JsDate getFirst() /*-{return new Date(this.first);}-*/;
	
	public final native JsDate getLast() /*-{return new Date(this.last);}-*/;
	
	public final native JsArray<RaceEntry> getRaceEntries() /*-{return this.raceEntries;}-*/;
	
	static class RaceEntry extends JavaScriptObject {
		
		protected RaceEntry() {};
		
		public final native String getRegattaName() /*-{return this.regattaName;}-*/;
		
		public final native String getRaceName() /*-{return this.raceName;}-*/;
		
		public final native int getCount() /*-{return this.count;}-*/;
		
		public final native JsDate getFirst() /*-{return new Date(this.first);}-*/;
		
		public final native JsDate getLast() /*-{return new Date(this.last);}-*/;
		
	}

}

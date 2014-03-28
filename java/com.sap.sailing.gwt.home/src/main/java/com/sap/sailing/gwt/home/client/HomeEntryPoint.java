package com.sap.sailing.gwt.home.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Document;

public class HomeEntryPoint implements EntryPoint {

	@Override
	public void onModuleLoad() {
		HelloWorld helloWorld = new HelloWorld("able", "baker", "charlie");

		// Don't forget, this is DOM only; will not work with GWT widgets
		Document.get().getBody().appendChild(helloWorld.getElement());
	}
}

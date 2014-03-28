package com.sap.sailing.gwt.home.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

public class HomeEntryPoint implements EntryPoint {

	@Override
	public void onModuleLoad() {
        HelloWorld helloWorld = new HelloWorld("able", "baker", "charlie");

        RootPanel.get().add(helloWorld);
	}
}

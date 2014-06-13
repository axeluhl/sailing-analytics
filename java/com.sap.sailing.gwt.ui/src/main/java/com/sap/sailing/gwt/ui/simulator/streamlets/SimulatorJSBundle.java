package com.sap.sailing.gwt.ui.simulator.streamlets;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface SimulatorJSBundle extends ClientBundle {

	//@Source("wind-streamlets.js")
	//TextResource windStreamletsJS();

	@Source("com/sap/sailing/gwt/ui/simulator/streamlets/wind-data.js")
	TextResource windStreamletsDataJS();

}

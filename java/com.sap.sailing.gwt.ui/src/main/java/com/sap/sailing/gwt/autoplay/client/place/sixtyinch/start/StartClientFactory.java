package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.start;

import com.sap.sailing.gwt.autoplay.client.place.start.StartView;
import com.sap.sailing.gwt.ui.client.SailingClientFactory;

public interface StartClientFactory extends SailingClientFactory {
    StartView createStartView();
}

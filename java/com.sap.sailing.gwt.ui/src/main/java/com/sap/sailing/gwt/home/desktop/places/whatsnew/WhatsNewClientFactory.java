package com.sap.sailing.gwt.home.desktop.places.whatsnew;

import com.sap.sailing.gwt.home.desktop.places.whatsnew.WhatsNewPlace.WhatsNewNavigationTabs;
import com.sap.sailing.gwt.ui.client.SailingClientFactory;

public interface WhatsNewClientFactory extends SailingClientFactory {
    WhatsNewView createWhatsNewView(WhatsNewNavigationTabs navigationTab);
}

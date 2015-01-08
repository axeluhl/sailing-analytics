package com.sap.sailing.gwt.home.client.place.whatsnew;

import com.sap.sailing.gwt.home.client.place.whatsnew.WhatsNewPlace.WhatsNewNavigationTabs;
import com.sap.sailing.gwt.ui.client.SailingClientFactory;

public interface WhatsNewClientFactory extends SailingClientFactory {
    WhatsNewView createWhatsNewView(WhatsNewNavigationTabs navigationTab);
}

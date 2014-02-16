package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

interface AdminConsoleResources extends ClientBundle {

    @Source("com/sap/sailing/gwt/ui/client/images/settings.png")
    ImageResource settingsIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/settingsActionIcon.png")
    ImageResource settingsActionIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/remove.png")
    ImageResource removeIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/edit.png")
    ImageResource editIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/import.png")
    ImageResource importIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/reload.png")
    ImageResource reloadIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/link.png")
    ImageResource linkIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/link_break.png")
    ImageResource unlinkIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/openBrowser.png")
    ImageResource openBrowserIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/dice.png")
    ImageResource scoresIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/users.png")
    ImageResource competitorsIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/blackdot.png")
    ImageResource blackdotIcon();
    
    @Source("com/sap/sailing/gwt/ui/client/images/reddot.png")
    ImageResource reddotIcon();
    
    @Source("com/sap/sailing/gwt/ui/client/images/arrow_left.png")
    ImageResource arrowLeft();
    
    @Source("com/sap/sailing/gwt/ui/client/images/arrow_right.png")
    ImageResource arrowRight();
    
    @Source("com/sap/sailing/gwt/ui/client/images/help.png")
    ImageResource help();
    
    @Source("com/sap/sailing/gwt/ui/client/images/clock.png")
    ImageResource clockIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/flag_blue.png")
    ImageResource flagIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/xml.png")
    ImageResource exportXMLIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/add_racelog_tracker.png")
    ImageResource addRaceLogTracker();

    @Source("com/sap/sailing/gwt/ui/client/images/denote_for_racelog_tracking.png")
    ImageResource denoteForRaceLogTracking();
}
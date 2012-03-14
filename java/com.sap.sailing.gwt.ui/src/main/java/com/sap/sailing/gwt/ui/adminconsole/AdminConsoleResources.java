package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

interface AdminConsoleResources extends ClientBundle {

    @Source("com/sap/sailing/gwt/ui/client/images/settings.png")
    ImageResource settingsIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/remove.png")
    ImageResource removeIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/edit.png")
    ImageResource editIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/link.png")
    ImageResource linkIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/link_break.png")
    ImageResource unlinkIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/openBrowser.png")
    ImageResource openBrowserIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/dice.png")
    ImageResource scoresIcon();
    
    @Source("com/sap/sailing/gwt/ui/client/images/coursemark.png")
    ImageResource buoyIcon();
    
    @Source("com/sap/sailing/gwt/ui/client/images/blackdot.png")
    ImageResource blackdotIcon();
    
    @Source("com/sap/sailing/gwt/ui/client/images/reddot.png")
    ImageResource reddotIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/windarrow.png")
    ImageResource windDirectionIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/windsensor.png")
    ImageResource windSensorIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/boats/Blue_Port.png")
    ImageResource lowlightedBoatIcon_Port();

    @Source("com/sap/sailing/gwt/ui/client/images/boats/Blue_Starbord.png")
    ImageResource lowlightedBoatIcon_Starboard();
 
    @Source("com/sap/sailing/gwt/ui/client/images/boats/Blue_DW_Port.png")
    ImageResource lowlightedBoatIconDW_Port();

    @Source("com/sap/sailing/gwt/ui/client/images/boats/Blue_DW_Starbord.png")
    ImageResource lowlightedBoatIconDW_Starboard();

    @Source("com/sap/sailing/gwt/ui/client/images/boats/Blue_Port_Selected.png")
    ImageResource highlightedBoatIcon_Port();

    @Source("com/sap/sailing/gwt/ui/client/images/boats/Blue_Starbord_Selected.png")
    ImageResource highlightedBoatIcon_Starboard();

    @Source("com/sap/sailing/gwt/ui/client/images/boats/Blue_DW_Port_Selected.png")
    ImageResource highlightedBoatIconDW_Port();

    @Source("com/sap/sailing/gwt/ui/client/images/boats/Blue_DW_Starbord_Selected.png")
    ImageResource highlightedBoatIconDW_Starboard();
}
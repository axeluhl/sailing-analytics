package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Shared;

/**
 * Contains class names for styles that can get overwritten in the {@link RaceMap}.
 * The {@link RaceMap} may require customized styles of elements when it is used in
 * different contexts.
 * 
 * @author Alexander Ries
 *
 */
@Shared
public interface RaceMapStyle extends CssResource {
    public String raceMapIndicatorPanel();
    public String raceMapIndicatorPanelTextLabel();
    public String raceMapIndicatorPanelCanvas();
    public String combinedWindPanel();
    public String trueNorthIndicatorPanel();
    public String estimatedDuration();
    public String estimatedDurationWithHeader();
    public String raceMapShareLinkButton();
    public String raceMapVideoUploadButton();
    public String zoomOutButton();
    public String zoomInButton();
    public String fullScreenButton();
    public String exitFullScreenButton();
    public String trueNorthIndicatorButton();
    public String tagsButton();
    public String moreOptionsButton();
    public String moreOptions();
    public String settingsButton();
    public String advancedFunctionsPopup();
    public String underHeaderPosition();
    public String topLeftControlsWrapperPanel();
    public String premiumReady();
    public String premiumActive();
}

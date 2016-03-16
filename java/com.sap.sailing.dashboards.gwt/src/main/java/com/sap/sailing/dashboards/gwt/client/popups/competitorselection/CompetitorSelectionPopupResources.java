package com.sap.sailing.dashboards.gwt.client.popups.competitorselection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * @author Alexander Ries (D062114)
 *
 */
public interface CompetitorSelectionPopupResources extends ClientBundle {

    public static final CompetitorSelectionPopupResources INSTANCE = GWT.create(CompetitorSelectionPopupResources.class);

    @Source({"com/sap/sailing/dashboards/gwt/client/theme/theme.gss", "CompetitorSelectionPopup.gss"})
    CompetitorSelectionPopupGss gss();

    public interface CompetitorSelectionPopupGss extends CssResource {
        String competitorselectionbackground();
        String competitorselectionpopup();
        String header();
        String scrollpanel();
        String buttonSeparator();
        String button();
        String cancelButton();
        String okButton();
        String popuphide();
        String popupshow();
    }
}
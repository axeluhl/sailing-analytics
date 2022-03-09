package com.sap.sailing.gwt.common.client.help;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;

public class HelpButton extends Composite {
    private final Image icon;
    private final HelpButtonPopup popup;

    public HelpButton(final HelpButtonResources resources, final String description, final String url) {
        resources.style().ensureInjected();
		this.icon = new Image(resources.icon());
		this.icon.addStyleName(resources.style().icon());
		this.popup = new HelpButtonPopup(resources, description, url);
		this.icon.addClickHandler(event -> popup.showRelativeTo(icon));
		initWidget(icon);
        this.setDescription(description);
        this.setLinkUrl(url);
    }

    public void setDescription(final String description) {
        this.popup.textUi.setInnerText(description);
    }

    public void setLinkUrl(final String url) {
        this.popup.linkUi.setHref(url);
    }
}

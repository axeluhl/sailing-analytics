package com.sap.sailing.gwt.common.client.help;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

class HelpButtonPopup extends Composite {

    interface HelpButtonPopupUiBinder extends UiBinder<Widget, HelpButtonPopup> {
    }

    private static HelpButtonPopupUiBinder uiBinder = GWT.create(HelpButtonPopupUiBinder.class);

    @UiField(provided = true)
    HelpButtonResources local_res;

    @UiField
    Element textUi;

    @UiField
    AnchorElement linkUi;

    private final PopupPanel popupPanel = new PopupPanel(true, false);

    HelpButtonPopup(final HelpButtonResources resources, String description, String url) {
        this.local_res = resources;
        initWidget(uiBinder.createAndBindUi(this));
        this.popupPanel.addStyleName(resources.style().popup());
        this.popupPanel.setWidget(this);
        this.textUi.setInnerText(description);
        this.linkUi.setHref(url);
    }

    void showRelativeTo(final Widget relativeTo) {
        this.popupPanel.showRelativeTo(relativeTo);
    }
}

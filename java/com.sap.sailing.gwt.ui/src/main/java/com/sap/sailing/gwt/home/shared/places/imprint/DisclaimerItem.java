package com.sap.sailing.gwt.home.shared.places.imprint;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.places.imprint.data.DisclaimerData;

public class DisclaimerItem extends Composite {

    private static final String LINK_PLACEHOLDER = "${link}";

    private static DisclaimerItemUiBinder uiBinder = GWT.create(DisclaimerItemUiBinder.class);

    interface DisclaimerItemUiBinder extends UiBinder<Widget, DisclaimerItem> {
    }
    
    @UiField
    Label disclaimerItemTitle;
    @UiField
    DivElement disclaimerItemContent;

    public DisclaimerItem() {
        super();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public void setDisclaimer(final DisclaimerData disclaimer) {
        if(disclaimer != null) {
            disclaimerItemTitle.setText(disclaimer.getTitle());
            final String content = disclaimer.getContent();
            final String contentWithLink = content.replace(LINK_PLACEHOLDER,
                    "<a href=\"" + disclaimer.getLink() + "\" >" + disclaimer.getLink() + "</a>");
            disclaimerItemContent.setInnerHTML(contentWithLink);
        }
    }
}

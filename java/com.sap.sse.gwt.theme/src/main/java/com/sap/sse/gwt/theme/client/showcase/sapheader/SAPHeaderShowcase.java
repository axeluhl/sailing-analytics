package com.sap.sse.gwt.theme.client.showcase.sapheader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.theme.client.component.sapheader.SAPHeader;

public class SAPHeaderShowcase extends Composite {

    private static SAPHeaderShowcaseUiBinder uiBinder = GWT.create(SAPHeaderShowcaseUiBinder.class);

    interface SAPHeaderShowcaseUiBinder extends UiBinder<Widget, SAPHeaderShowcase> {
    }

    @UiField(provided = true)
    SAPHeader sapHeader;

    public SAPHeaderShowcase() {
        sapHeader = new SAPHeader("Sailing Analytics", "Demo page", false);
        
        initWidget(uiBinder.createAndBindUi(this));
    }
}

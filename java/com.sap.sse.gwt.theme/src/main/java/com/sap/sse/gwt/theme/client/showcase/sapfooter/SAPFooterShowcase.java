package com.sap.sse.gwt.theme.client.showcase.sapfooter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.theme.client.component.sapfooter.SAPFooter;

public class SAPFooterShowcase extends Composite {

    private static SAPHeaderShowcaseUiBinder uiBinder = GWT.create(SAPHeaderShowcaseUiBinder.class);

    interface SAPHeaderShowcaseUiBinder extends UiBinder<Widget, SAPFooterShowcase> {
    }

    @UiField(provided = true)
    SAPFooter sapFooter;

    public SAPFooterShowcase() {
        sapFooter = new SAPFooter();
        
        initWidget(uiBinder.createAndBindUi(this));
    }
}

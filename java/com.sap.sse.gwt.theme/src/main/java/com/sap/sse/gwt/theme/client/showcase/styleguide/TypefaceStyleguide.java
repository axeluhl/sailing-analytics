package com.sap.sse.gwt.theme.client.showcase.styleguide;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

public class TypefaceStyleguide extends AbstractStyleguideComposite {

    private static TypefaceStyleguideUiBinder uiBinder = GWT.create(TypefaceStyleguideUiBinder.class);

    interface TypefaceStyleguideUiBinder extends UiBinder<Widget, TypefaceStyleguide> {
    }

    public TypefaceStyleguide() {
        super();
        
        initWidget(uiBinder.createAndBindUi(this));
    }
}

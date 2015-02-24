package com.sap.sse.gwt.theme.client.showcase;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class MainPage extends Composite {

    private static MainPageUiBinder uiBinder = GWT.create(MainPageUiBinder.class);

    interface MainPageUiBinder extends UiBinder<Widget, MainPage> {
    }

    @UiField(provided = true)
    SimplePanel contentUi;

    @UiField(provided = true)
    ShowcaseStackLayoutPanel stackLayoutPanelUi;

    public MainPage() {
        contentUi = new SimplePanel();
        stackLayoutPanelUi = new ShowcaseStackLayoutPanel(Unit.EM);
        stackLayoutPanelUi.setTarget(contentUi);
        initWidget(uiBinder.createAndBindUi(this));
    }
}

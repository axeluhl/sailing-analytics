package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * This is the top-level view of the autoplay application. Every time another presenter wants to reveal itself,
 * {@link DesktopApplicationView} will add its content of the target inside the {@code mainContentPanel}.
 */
public class DesktopApplicationView extends Composite implements ApplicationTopLevelView {
    interface DesktopApplicationViewUiBinder extends UiBinder<Widget, DesktopApplicationView> {
    }

    private static DesktopApplicationViewUiBinder uiBinder = GWT.create(DesktopApplicationViewUiBinder.class);

    @UiField
    SimplePanel mainContentPanel;

    public DesktopApplicationView(PlaceNavigator navigator) {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public AcceptsOneWidget getContent() {
        return mainContentPanel;
    }
    
    @Override
    public void showLoading(boolean visible) {
    }
}

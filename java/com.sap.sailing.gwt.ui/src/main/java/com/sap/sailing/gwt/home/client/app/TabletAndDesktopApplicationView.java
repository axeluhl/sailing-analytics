package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.footer.FooterPanel;
import com.sap.sailing.gwt.home.client.shared.header.Header;
import com.sap.sailing.gwt.home.client.shared.mainmenu.MainMenuNavigator;

/**
 * This is the top-level view of the application. Every time another presenter wants to reveal itself,
 * {@link TabletAndDesktopApplicationView} will add its content of the target inside the {@code mainContantPanel}.
 */
public class TabletAndDesktopApplicationView extends Composite implements ApplicationTopLevelView {
    interface TabletAndDesktopApplicationViewUiBinder extends UiBinder<Widget, TabletAndDesktopApplicationView> {
    }

    private static TabletAndDesktopApplicationViewUiBinder uiBinder = GWT.create(TabletAndDesktopApplicationViewUiBinder.class);

    @UiField(provided=true)
    Header headerPanel;

    @UiField
    FooterPanel footerPanel;

    @UiField
    SimplePanel mainContentPanel;

    @UiField
    Element loadingMessage;

    public TabletAndDesktopApplicationView(MainMenuNavigator navigator) {
        headerPanel = new Header(navigator);
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public AcceptsOneWidget getContent() {
        return mainContentPanel;
    }
    
    @Override
    public void showLoading(boolean visibile) {
        loadingMessage.getStyle().setVisibility(visibile ? Visibility.VISIBLE : Visibility.HIDDEN);
    }
}

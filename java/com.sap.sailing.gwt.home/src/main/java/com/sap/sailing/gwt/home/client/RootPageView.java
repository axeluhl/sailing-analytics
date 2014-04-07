package com.sap.sailing.gwt.home.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;
import com.sap.sailing.gwt.home.client.RootPagePresenter.MyView;
import com.sap.sailing.gwt.home.client.shared.HeaderPanel;

/**
 * This is the top-level view of the application. Every time another presenter wants to reveal itself,
 * {@link RootPageView} will add its content of the target inside the {@code mainContantPanel}.
 */
public class RootPageView extends ViewImpl implements MyView {
    interface MainPageViewUiBinder extends UiBinder<Widget, RootPageView> {
    }

    private static MainPageViewUiBinder uiBinder = GWT.create(MainPageViewUiBinder.class);

    public final Widget widget;

    @UiField(provided=true)
    HeaderPanel headerPanel;

    @UiField
    FlowPanel mainContentPanel;

    @UiField
    Element loadingMessage;

    public RootPageView() {
        headerPanel = new HeaderPanel();
        
        widget = uiBinder.createAndBindUi(this);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == RootPagePresenter.TYPE_SetMainContent) {
            setMainContent(content);
        } else {
            super.setInSlot(slot, content);
        }
    }

    private void setMainContent(IsWidget content) {
        mainContentPanel.clear();

        if (content != null) {
            mainContentPanel.add(content);
        }
    }

    @Override
    public void showLoading(boolean visibile) {
        loadingMessage.getStyle().setVisibility(visibile ? Visibility.VISIBLE : Visibility.HIDDEN);
    }
}

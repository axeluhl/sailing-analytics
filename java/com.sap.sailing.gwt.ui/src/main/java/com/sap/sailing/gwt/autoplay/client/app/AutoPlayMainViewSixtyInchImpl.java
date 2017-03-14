package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SlideHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slideinit.SlideInitViewImpl;
import com.sap.sailing.gwt.common.authentication.SAPSailingHeaderWithAuthentication;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.DefaultErrorReporter;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.sapheader.SAPHeader;

public class AutoPlayMainViewSixtyInchImpl extends ResizeComposite
        implements ApplicationTopLevelView, AcceptsOneWidget {
    private static SixtyInchViewImplUiBinder uiBinder = GWT.create(SixtyInchViewImplUiBinder.class);

    @UiField
    protected LayoutPanel mainPanel;

    protected SAPHeader sapHeader = new SAPHeader(SAPSailingHeaderWithAuthentication.SAP_SAILING_APP_NAME,
            SAPSailingHeaderWithAuthentication.SAP_SAILING_URL);

    private IsWidget currentWidget;

    private static ErrorReporter errorReporter = new DefaultErrorReporter<StringMessages>(StringMessages.INSTANCE);

    interface SixtyInchViewImplUiBinder extends UiBinder<Widget, AutoPlayMainViewSixtyInchImpl> {
    }

    public AutoPlayMainViewSixtyInchImpl(EventBus eventBus) {
        initWidget(uiBinder.createAndBindUi(this));
        sapHeader.setHeaderTitle("Initializing");
        mainPanel.add(sapHeader);
        mainPanel.setWidgetTopHeight(sapHeader, 0, Unit.PX, 75, Unit.PX);
        eventBus.addHandler(SlideHeaderEvent.TYPE, new SlideHeaderEvent.Handler() {

            @Override
            public void onHeaderChanged(SlideHeaderEvent event) {
                sapHeader.setHeaderTitle(event.getHeaderText());
                sapHeader.setHeaderSubTitle(event.getHeaderSubText());
            }
        });
    }

    @Override
    public void setWidget(IsWidget widgetToShow) {
        if (widgetToShow == null) {
            // we can't display a null widget
            return;
        }
        if (currentWidget == null) {
            // first widget, just show.
            mainPanel.add(widgetToShow);
            currentWidget = widgetToShow.asWidget();
        } else {
            final IsWidget widgetToDispose = currentWidget;
            currentWidget = widgetToShow;
            mainPanel.add(widgetToShow);
            mainPanel.setWidgetTopHeight(widgetToShow, 75, Unit.PX, 100, Unit.PCT);
            mainPanel.setWidgetLeftWidth(widgetToShow, 100, Unit.PCT, 100, Unit.PCT);
            mainPanel.forceLayout();
            if (widgetToDispose != null) {
                new Timer() {
                    public void run() {
                        mainPanel.remove(widgetToDispose);
                    };
                }.schedule(2000);
            }
            mainPanel.setWidgetLeftWidth(widgetToDispose, -100, Unit.PCT, 100, Unit.PCT);
            mainPanel.setWidgetLeftWidth(widgetToShow, 0, Unit.PCT, 100, Unit.PCT);
            if (widgetToShow instanceof SlideInitViewImpl) {
                mainPanel.forceLayout();
            } else {
                mainPanel.animate(2000);
            }
        }
    }

    @Override
    public AcceptsOneWidget getContent() {
        return this;
    }

    @Override
    public void showLoading(boolean visible) {
    }

    @Override
    public ErrorReporter getErrorReporter() {
        return errorReporter;
    }
}

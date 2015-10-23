package com.sap.sailing.gwt.home.mobile.partials.solutions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.utils.CollapseAnimation;

public class Solutions extends Composite {

    interface MyUiBinder extends UiBinder<Widget, Solutions> {
    }
    
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiField DivElement sapSailingAnalyticsUi;
    @UiField DivElement raceCommitteeAppUi;
    @UiField DivElement postRaceAnalyticsUi;
    @UiField DivElement stgTrainingDiaryUi;
    @UiField DivElement strategySimulatorUi;
    
    public Solutions() {
        SolutionsResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        initAnimation(sapSailingAnalyticsUi, true);
        initAnimation(raceCommitteeAppUi, false);
        initAnimation(postRaceAnalyticsUi, false);
        initAnimation(stgTrainingDiaryUi, false);
        initAnimation(strategySimulatorUi, false);
    }

    private void initAnimation(final DivElement rootElement, boolean showInitial) {
        final CollapseAnimation animation = new CollapseAnimation(getContent(rootElement), showInitial);
        setClassName(rootElement, SolutionsResources.INSTANCE.css().accordioncollapsed(), !showInitial);
        Element header = getHeader(rootElement);
        DOM.sinkEvents(header, Event.ONCLICK);
        DOM.setEventListener(header, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                boolean collapsed = isCollapsed(rootElement);
                setClassName(rootElement, SolutionsResources.INSTANCE.css().accordioncollapsed(), !collapsed);
                animation.animate(collapsed);
            }
        });
    }
    
    private void setClassName(Element element, String className, boolean add) {
        if (add) element.addClassName(className);
        else element.removeClassName(className);
    }
    
    private boolean isCollapsed(Element rootElement) {
        return rootElement.hasClassName(SolutionsResources.INSTANCE.css().accordioncollapsed());
    }
    
    private Element getHeader(Element rootElement) {
        return rootElement.getFirstChildElement();
    }
    
    private Element getContent(Element rootElement) {
        return rootElement.getFirstChildElement().getNextSiblingElement();
    }
}

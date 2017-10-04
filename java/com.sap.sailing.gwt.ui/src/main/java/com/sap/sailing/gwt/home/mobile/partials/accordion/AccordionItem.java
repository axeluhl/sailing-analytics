package com.sap.sailing.gwt.home.mobile.partials.accordion;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.partials.solutions.SolutionsResources;
import com.sap.sailing.gwt.home.shared.utils.CollapseAnimation;

public class AccordionItem extends Composite {

    private static AccordionItemUiBinder uiBinder = GWT.create(AccordionItemUiBinder.class);

    interface AccordionItemUiBinder extends UiBinder<Widget, AccordionItem> {
    }
    
    @UiField
    DivElement headerUi;
    
    @UiField
    HeadingElement titleUi;
    
    @UiField
    ImageElement imageUi;
    
    @UiField
    SimplePanel contentUi;
    
    @UiField
    DivElement contentWrapperUi;

    public AccordionItem(String title, ImageResource image, String imageAltText, boolean showInitial) {
        initWidget(uiBinder.createAndBindUi(this));
        titleUi.setInnerText(title);
        imageUi.setSrc(image.getSafeUri().asString());
        imageUi.setAlt(imageAltText);
        initAnimation(showInitial);
    }

    private void initAnimation(boolean showInitial) {
        final CollapseAnimation animation = new CollapseAnimation(contentWrapperUi, showInitial);
        Element rootElement = getWidget().getElement();
        setClassName(rootElement, SolutionsResources.INSTANCE.css().accordioncollapsed(), !showInitial);
        DOM.sinkEvents(headerUi, Event.ONCLICK);
        DOM.setEventListener(headerUi, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                boolean collapsed = isCollapsed(rootElement);
                setClassName(rootElement, AccordionResources.INSTANCE.css().accordioncollapsed(), !collapsed);
                animation.animate(collapsed);
            }
        });
    }
    
    private void setClassName(Element element, String className, boolean add) {
        if (add) element.addClassName(className);
        else element.removeClassName(className);
    }
    
    private boolean isCollapsed(Element rootElement) {
        return rootElement.hasClassName(AccordionResources.INSTANCE.css().accordioncollapsed());
    }
    
    @UiChild
    public void addContent(Widget content) {
        contentUi.setWidget(content);
    }
}

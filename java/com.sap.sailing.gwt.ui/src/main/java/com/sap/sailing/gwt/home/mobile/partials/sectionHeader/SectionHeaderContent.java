package com.sap.sailing.gwt.home.mobile.partials.sectionHeader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.LinkUtil;
import com.sap.sailing.gwt.home.client.shared.LabelTypeUtil;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.shared.general.LabelType;

public class SectionHeaderContent extends Widget {
    
    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    interface MyBinder extends UiBinder<Element, SectionHeaderContent> {
    }

    @UiField AnchorElement headerMainUi;
    @UiField DivElement headerLeftUi; 
    @UiField DivElement titleAndLabelContainerUi;
    @UiField HeadingElement titleUi;
    @UiField DivElement labelUi;
    @UiField DivElement imageUi;
    @UiField DivElement subtitleUi;
    @UiField DivElement headerRightUi;
    @UiField DivElement infoTextUi;
    @UiField ImageElement actionArrowUi;

    public SectionHeaderContent() {
        SectionHeaderResources.INSTANCE.css().ensureInjected();
        setElement(uiBinder.createAndBindUi(this));
    }
    
    public void setSectionTitle(String sectionHeaderTitle) {
        titleUi.setInnerText(sectionHeaderTitle);
    }
    
    public void setSubtitle(String subtitle) {
        subtitleUi.getStyle().clearDisplay();
        subtitleUi.setInnerText(subtitle);
        headerLeftUi.addClassName(SectionHeaderResources.INSTANCE.css().sectionheader_itemdoublerow());
    }
    
    public void setLabelType(LabelType labelType) {
        if (labelType == null || !labelType.isRendered()) {
            labelUi.getStyle().setDisplay(Display.NONE);
        } else {
            labelUi.getStyle().clearDisplay();
            LabelTypeUtil.renderLabelType(labelUi, labelType);
        }
    }
    
    public void setImageUrl(String imageUrl) {
        imageUi.getStyle().clearDisplay();
        imageUi.getStyle().setBackgroundImage("url(" + imageUrl + ")");
        titleAndLabelContainerUi.addClassName(SectionHeaderResources.INSTANCE.css().sectionheader_item_adjust_title());
    }
    
    public void setInfoText(String infoText) {
        headerRightUi.getStyle().clearDisplay();
        infoTextUi.getStyle().clearDisplay();
        infoTextUi.setInnerText(infoText);
    }
    
    public void setClickAction(final PlaceNavigation<?> placeNavigation) {
        placeNavigation.configureAnchorElement(headerMainUi);
        headerRightUi.getStyle().clearDisplay();
        actionArrowUi.getStyle().clearDisplay();
    }
    
    public void setClickAction(final String url) {
        headerMainUi.setHref(url);
        DOM.setEventListener(headerMainUi, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
            }
        });
        headerRightUi.getStyle().clearDisplay();
        actionArrowUi.getStyle().clearDisplay();
    }

    public void setClickAction(final Runnable commandToExecute) {
        headerMainUi.setHref(Window.Location.getHref());
        headerRightUi.getStyle().clearDisplay();
        actionArrowUi.getStyle().clearDisplay();
        LinkUtil.configureForAction(headerMainUi, commandToExecute);
    }
    
}

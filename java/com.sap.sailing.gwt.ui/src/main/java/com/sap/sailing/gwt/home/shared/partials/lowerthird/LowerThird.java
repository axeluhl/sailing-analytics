package com.sap.sailing.gwt.home.shared.partials.lowerthird;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.LinkUtil;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.communication.event.LabelType;
import com.sap.sailing.gwt.home.shared.utils.ButtonUtil;
import com.sap.sailing.gwt.home.shared.utils.LabelTypeUtil;
import com.sap.sailing.gwt.home.shared.utils.ButtonUtil.ButtonType;

public class LowerThird extends Widget {
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Element, LowerThird> {
    }
    
    @UiField DivElement titleWrapperUi;
    @UiField SpanElement titleUi;
    @UiField ParagraphElement subtitleUi;
    @UiField DivElement labelUi;
    @UiField DivElement buttonWrapperUi;
    @UiField AnchorElement buttonUi;
    
    private final SharedResources.MediaCss mediaCss = SharedResources.INSTANCE.mediaCss();
    private final LowerThirdResources.LocalCss style = LowerThirdResources.INSTANCE.css();
    
    public LowerThird() {
        style.ensureInjected();
        setElement(uiBinder.createAndBindUi(this));
        Event.sinkEvents(buttonUi, Event.ONCLICK);
    }
    
    public void setData(String title, String subtitle, LabelType labelType) {
        titleUi.setInnerText(title);
        
        if(subtitle == null) {
            subtitleUi.getStyle().setDisplay(Display.NONE);
            titleUi.removeClassName(style.video_content_item_info_text_titlesmall());
        } else {
            subtitleUi.getStyle().clearDisplay();
            subtitleUi.setInnerText(subtitle);
            titleUi.addClassName(style.video_content_item_info_text_titlesmall());
        }
        
        LabelTypeUtil.renderLabelTypeOrHide(labelUi, labelType);
    }
    
    public void hideAction() {
        titleWrapperUi.removeClassName(mediaCss.small8());
        titleWrapperUi.addClassName(mediaCss.small12());
        
        buttonUi.getStyle().setDisplay(Display.NONE);
    }
    
    public void setAction(String title, ButtonType buttonType, String link, final Runnable action) {
        titleWrapperUi.addClassName(mediaCss.small8());
        titleWrapperUi.removeClassName(mediaCss.small12());
        
        buttonUi.getStyle().clearDisplay();
        buttonUi.setInnerText(title);
        
        ButtonUtil.applyButtonStyle(buttonUi, buttonType);
        buttonUi.setHref(link);
        if(action == null) {
            Event.setEventListener(buttonUi, null);
        } else {
            Event.setEventListener(buttonUi, new EventListener() {
                @Override
                public void onBrowserEvent(Event event) {
                    if(LinkUtil.handleLinkClick(event)) {
                        event.preventDefault();
                        action.run();
                    }
                }
            });
        }
    }
}

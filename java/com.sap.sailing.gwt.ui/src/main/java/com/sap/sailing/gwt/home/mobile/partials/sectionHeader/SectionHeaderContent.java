package com.sap.sailing.gwt.home.mobile.partials.sectionHeader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.event.LabelType;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.partials.bubble.Bubble;
import com.sap.sailing.gwt.home.shared.partials.bubble.Bubble.Direction;
import com.sap.sailing.gwt.home.shared.partials.bubble.BubbleContentBoatClass;
import com.sap.sailing.gwt.home.shared.utils.CollapseAnimation;
import com.sap.sailing.gwt.home.shared.utils.LabelTypeUtil;
import com.sap.sse.gwt.client.LinkUtil;

public class SectionHeaderContent extends Composite {
    
    protected static final String ACCORDION_COLLAPSED_STYLE = SectionHeaderResources.INSTANCE.css().collapsed();
    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    interface MyBinder extends UiBinder<Widget, SectionHeaderContent> {
    }

    @UiField SectionHeaderResources local_res;
    @UiField AnchorElement headerMainUi;
    @UiField DivElement headerLeftUi; 
    @UiField DivElement titleAndLabelContainerUi;
    @UiField HeadingElement titleUi;
    @UiField DivElement labelUi;
    @UiField DivElement imageUi;
    @UiField DivElement subtitleUi;
    @UiField DivElement headerRightUi;
    @UiField DivElement infoTextUi;
    @UiField DivElement actionArrowUi;
    @UiField SimplePanel widgetContainerUi;

    public SectionHeaderContent() {
        SectionHeaderResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        widgetContainerUi.setVisible(false);
    }
    
    public void setSectionTitle(String sectionHeaderTitle) {
        titleUi.setInnerText(sectionHeaderTitle);
    }
    
    public void setSubtitle(String subtitle) {
        subtitleUi.getStyle().clearDisplay();
        subtitleUi.setInnerText(subtitle);
        headerLeftUi.addClassName(local_res.css().sectionheader_itemdoublerow());
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
        titleAndLabelContainerUi.addClassName(local_res.css().sectionheader_item_adjust_title_left());
    }
    
    public void setInfoText(String infoText) {
        headerRightUi.getStyle().clearDisplay();
        infoTextUi.getStyle().clearDisplay();
        infoTextUi.setInnerText(infoText);
    }
    
    public void initCollapsibility(Element content, boolean showInitial) {
        final CollapseAnimation animation = new CollapseAnimation(content, showInitial);
        UIObject.setStyleName(actionArrowUi, ACCORDION_COLLAPSED_STYLE, !showInitial);
        LinkUtil.configureForAction(headerMainUi, () -> {
            boolean collapsed = actionArrowUi.hasClassName(ACCORDION_COLLAPSED_STYLE);
            UIObject.setStyleName(actionArrowUi, ACCORDION_COLLAPSED_STYLE, !collapsed);
            animation.animate(collapsed);
        });
        actionArrowUi.addClassName(SectionHeaderResources.INSTANCE.css().accordion());
        titleAndLabelContainerUi.addClassName(local_res.css().sectionheader_item_adjust_title_right());
        actionArrowUi.getStyle().clearDisplay();
        headerRightUi.getStyle().clearDisplay();
    }
    
    public void setClickAction(final PlaceNavigation<?> placeNavigation) {
        placeNavigation.configureAnchorElement(headerMainUi);
        this.adjustedActionStyles();
    }
    
    public void setClickAction(final String url) {
        headerMainUi.setHref(url);
        DOM.setEventListener(headerMainUi, event -> {
        });
        this.adjustedActionStyles();
    }

    private void adjustedActionStyles() {
        titleAndLabelContainerUi.addClassName(local_res.css().sectionheader_item_adjust_title_right());
        headerRightUi.getStyle().clearDisplay();
        actionArrowUi.getStyle().clearDisplay();
    }

    public void setClickAction(final Runnable commandToExecute) {
        headerMainUi.setHref(Window.Location.getHref());
        headerRightUi.getStyle().clearDisplay();
        actionArrowUi.getStyle().clearDisplay();
        LinkUtil.configureForAction(headerMainUi, commandToExecute);
    }
    
    public void initAdditionalWidget(final Widget additionalWidget) {
        headerRightUi.getStyle().clearDisplay();
        widgetContainerUi.setVisible(true);
        widgetContainerUi.setWidget(additionalWidget);
        additionalWidget.getElement().setAttribute("dir", "rtl");
        Scheduler.get().scheduleDeferred(
                () -> titleAndLabelContainerUi.getStyle().setMarginRight(widgetContainerUi.getOffsetWidth(), Unit.PX));
    }

    public void initBoatClassPopup(String boatClassName) {
        BubbleContentBoatClass content = new BubbleContentBoatClass(boatClassName);
        Bubble.DefaultPresenter presenter = new Bubble.DefaultPresenter(content, getElement(), imageUi,
                Direction.RIGHT);
        presenter.registerTarget(imageUi);
    }

}

package com.sap.sailing.gwt.home.desktop.partials.desktopaccordion;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.shared.utils.CollapseAnimation;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * This object is similar to the accordion ({@link MobileSection})on mobile. It has a header with a title and a
 * collapsable content panel.
 */
public class DesktopAccordion extends Composite {

    interface DesktopAccordionUiBinder extends UiBinder<Widget, DesktopAccordion> {
    }

    private static DesktopAccordionUiBinder uiBinder = GWT.create(DesktopAccordionUiBinder.class);

    @UiField
    SpanElement titleUi;
    @UiField
    FlowPanel contentPanelUi;
    @UiField
    HTMLPanel headerDivUi;
    @UiField
    StringMessages i18n;

    private boolean isContentVisible;

    private final CollapseAnimation animation;

    /** true, as soon as the accordion was expanded once. */
    private boolean wasOpenend;
    private List<AccordionExpansionListener> accordionListeners;

    private boolean expanded;

    public interface AccordionExpansionListener {
        void onExpansion(boolean expanded);
    }

    public DesktopAccordion() {
        this(false);
    }

    public DesktopAccordion(boolean showInitial) {
        accordionListeners = new ArrayList<>();
        DesktopAccordionResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));

        this.titleUi.setInnerText("Title");

        headerDivUi.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onHeaderCicked();
            }
        }, ClickEvent.getType());

        isContentVisible = showInitial;
        animation = new CollapseAnimation(contentPanelUi.getElement(), showInitial);
        updateAccordionState();
    }

    public void setTitle(String title) {
        titleUi.setInnerText(title);
    }

    private void onHeaderCicked() {
        isContentVisible = !isContentVisible;
        updateContentVisibility();
    }

    private void updateContentVisibility() {
        animation.animate(isContentVisible);
        updateAccordionState();
    }

    public void addWidget(Widget w) {
        contentPanelUi.add(w);
    }

    private void updateAccordionState() {
        if (isContentVisible) {
            getElement().removeClassName(DesktopAccordionResources.INSTANCE.css().accordionCollapsed());
            if (!wasOpenend) {
                wasOpenend = true;
                expanded = true;
                for (AccordionExpansionListener accordionListener : accordionListeners) {
                    accordionListener.onExpansion(expanded);
                }
                accordionListeners.clear();
            }
        } else {
            expanded = false;
            getElement().addClassName(DesktopAccordionResources.INSTANCE.css().accordionCollapsed());
        }
    }

    public void clear() {
        contentPanelUi.clear();
        accordionListeners.clear();
    }

    public void addAccordionListener(AccordionExpansionListener listener) {
        accordionListeners.add(listener);
    }

    public boolean isExpanded() {
        return expanded;
    }
}

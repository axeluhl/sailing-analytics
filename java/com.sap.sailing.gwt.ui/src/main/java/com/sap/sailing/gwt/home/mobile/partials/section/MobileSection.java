package com.sap.sailing.gwt.home.mobile.partials.section;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderResources;

public class MobileSection extends Composite {
    
    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    interface MyBinder extends UiBinder<Widget, MobileSection> {
    }

    @UiField MobileSectionResources local_res;
    @UiField DivElement sectionHeaderHolderUi;
    @UiField DivElement sectionContentHolderUi;
    @UiField SimplePanel sectionHeaderUi;
    @UiField SimplePanel sectionFooterUi;
    @UiField FlowPanel sectionContentUi;

    public MobileSection() {
        MobileSectionResources.INSTANCE.css().ensureInjected();
        SectionHeaderResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @UiChild
    public void addHeader(Widget theHeaderWidget) {
        sectionHeaderUi.setWidget(theHeaderWidget);
    }
    
    @UiChild
    public void addFooter(Widget theFooterWidget) {
        sectionFooterUi.setWidget(theFooterWidget);
    }

    @UiChild
    public void addContent(Widget theContentWidget) {
        if (theContentWidget instanceof IsMobileSection) {
            IsMobileSection mobileSubSection = (IsMobileSection) theContentWidget;
            mobileSubSection.getMobileSection().showAsSubSection();
        }
        if (theContentWidget instanceof MobileSection) {
            MobileSection mobileSubSection = (MobileSection) theContentWidget;
            mobileSubSection.showAsSubSection();
        }
        sectionContentUi.add(theContentWidget);
    }

    private void showAsSubSection() {
        sectionHeaderHolderUi.addClassName(local_res.css().sectionSubHeader());
    }
    
    public void clearContent() {
        sectionContentUi.clear();
    }

    public void setEdgeToEdgeContent(boolean setIsEdgeToEdgeContent) {
        if (setIsEdgeToEdgeContent) {
            sectionContentHolderUi.addClassName(local_res.css().edgeToEdgeSectionContentHolder());
        } else if (!setIsEdgeToEdgeContent) {
            sectionContentHolderUi.removeClassName(local_res.css().edgeToEdgeSectionContentHolder());
        }
    }
    
    public void setBorderTop(boolean borderTop) {
        UIObject.setStyleName(sectionHeaderHolderUi, local_res.css().sectionHeaderNoBorder(), !borderTop);
    }
    
    public Element getContentContainerElement() {
        return sectionContentHolderUi;
    }
}

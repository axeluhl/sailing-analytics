package com.sap.sailing.gwt.home.client.place.event.partials.regattaHeader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sailing.gwt.common.client.BoatClassImageResolver;
import com.sap.sailing.gwt.home.client.shared.LabelTypeUtil;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;

public class RegattaHeaderBody extends UIObject {

    private static final StringMessages I18N = StringMessages.INSTANCE;
    private static RegattaHeaderBodyUiBinder uiBinder = GWT.create(RegattaHeaderBodyUiBinder.class);

    interface RegattaHeaderBodyUiBinder extends UiBinder<Element, RegattaHeaderBody> {
    }
    
    @UiField protected DivElement logoUi;
    @UiField protected SpanElement nameUi;
    @UiField protected DivElement labelUi;
    @UiField protected DivElement detailsItemContainerUi;

    public RegattaHeaderBody(RegattaMetadataDTO regattaMetadata) {
        RegattaHeaderResources.INSTANCE.css().ensureInjected();
        setElement(uiBinder.createAndBindUi(this));
        ImageResource logo = BoatClassImageResolver.getBoatClassIconResource(regattaMetadata.getBoatClass());
        logoUi.getStyle().setBackgroundImage("url('" + logo.getSafeUri().asString() + "')");
        nameUi.setInnerText(regattaMetadata.getDisplayName());
        LabelTypeUtil.renderLabelTypeOrHide(labelUi, regattaMetadata.getState().getStateMarker());
        addDetailsItem(regattaMetadata.getCompetitorsCount(), I18N.competitorsCount(regattaMetadata.getCompetitorsCount()));
        addDetailsItem(regattaMetadata.getRaceCount(), I18N.racesCount(regattaMetadata.getRaceCount()));
        String defaultCourseAreaName = regattaMetadata.getDefaultCourseAreaName();
        if(defaultCourseAreaName != null) {
            addDetailsItem(I18N.courseAreaName(defaultCourseAreaName));
        }
        addDetailsItem(regattaMetadata.getBoatCategory());
    }
    
    private void addDetailsItem(int count, String text) {
        if (count > 0) {
            addDetailsItem(text);
        }
    }
    
    private void addDetailsItem(String text) {
        if(text != null) {
            DivElement detailsItem = DOM.createDiv().cast();
            detailsItem.addClassName(RegattaHeaderResources.INSTANCE.css().regattaheader_content_details_item());
            detailsItem.setInnerText(text);
            detailsItemContainerUi.appendChild(detailsItem);
        }
    }

}

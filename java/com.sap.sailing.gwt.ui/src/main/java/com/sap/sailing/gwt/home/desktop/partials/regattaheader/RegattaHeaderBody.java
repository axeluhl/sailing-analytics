package com.sap.sailing.gwt.home.desktop.partials.regattaheader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sailing.gwt.home.communication.event.LabelType;
import com.sap.sailing.gwt.home.communication.eventview.HasRegattaMetadata.RegattaState;
import com.sap.sailing.gwt.home.communication.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.home.shared.utils.LabelTypeUtil;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;

public class RegattaHeaderBody extends UIObject {

    private static final StringMessages I18N = StringMessages.INSTANCE;
    private static RegattaHeaderBodyUiBinder uiBinder = GWT.create(RegattaHeaderBodyUiBinder.class);

    interface RegattaHeaderBodyUiBinder extends UiBinder<Element, RegattaHeaderBody> {
    }
    
    @UiField protected SpanElement nameUi;
    @UiField protected DivElement labelUi;
    @UiField protected DivElement detailsItemContainerUi;

    public RegattaHeaderBody(RegattaMetadataDTO regattaMetadata, boolean showStateMarker) {
        RegattaHeaderResources.INSTANCE.css().ensureInjected();
        setElement(uiBinder.createAndBindUi(this));
        nameUi.setInnerText(regattaMetadata.getDisplayName());
        RegattaState state = regattaMetadata.getState();
        LabelTypeUtil.renderLabelTypeOrHide(labelUi, showStateMarker ? state.getStateMarker() : LabelType.NONE);
        addDetailsItem(regattaMetadata.getCompetitorsCount(), I18N.competitorsCount(regattaMetadata.getCompetitorsCount()));
        addDetailsItem(regattaMetadata.getRaceCount(), I18N.racesCount(regattaMetadata.getRaceCount()));
        String defaultCourseAreaName = regattaMetadata.getDefaultCourseAreaName();
        if (defaultCourseAreaName != null) {
            addDetailsItem(I18N.courseAreaName(defaultCourseAreaName));
        }
        if (regattaMetadata.getLeaderboardGroupNames() != null && !Util.isEmpty(regattaMetadata.getLeaderboardGroupNames())) {
            addDetailsItem(Util.joinStrings(", ", regattaMetadata.getLeaderboardGroupNames()));
        }
        UIObject.ensureDebugId(nameUi, "RegattaNameSpan");
        UIObject.ensureDebugId(labelUi, "RegattaStateLabelDiv");
    }
    
    private void addDetailsItem(int count, String text) {
        if (count > 0) {
            addDetailsItem(text);
        }
    }
    
    private void addDetailsItem(String text) {
        if (text != null) {
            DivElement detailsItem = DOM.createDiv().cast();
            detailsItem.addClassName(RegattaHeaderResources.INSTANCE.css().regattaheader_content_details_item());
            detailsItem.setInnerText(text);
            detailsItemContainerUi.appendChild(detailsItem);
        }
    }

}

package com.sap.sailing.gwt.home.mobile.partials.updatesBox;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.BoatClassImageResolver;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.shared.dispatch.news.NewsEntryDTO;

public class UpdatesBoxItem extends Widget {
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Element, UpdatesBoxItem> {
    }
    
    @UiField AnchorElement link;
    @UiField DivElement icon;
    @UiField SpanElement titleUi;
    @UiField DivElement messageUi;
    @UiField DivElement timestampUi;
    
    public UpdatesBoxItem(NewsEntryDTO entry, NewsItemLinkProvider provider) {
        setElement(uiBinder.createAndBindUi(this));
        
        titleUi.setInnerText(entry.getTitle());
        messageUi.setInnerText(entry.getMessage());
        
        String boatClass = entry.getBoatClass();
        if(boatClass != null && !boatClass.isEmpty()) {
            icon.getStyle().setBackgroundImage("url(\"" + BoatClassImageResolver.getBoatClassIconResource(boatClass).getSafeUri().asString() + "\")");
        }
        
        Date timestamp = entry.getTimestamp();
        if(timestamp == null) {
            timestampUi.removeFromParent();
        } else {
            timestampUi.setInnerText(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM).format(timestamp));
        }
        
        String directLink = entry.getExternalURL();
        if(directLink != null) {
            link.setHref(directLink);
            link.setTarget("_blank");
        } else {
            final PlaceNavigation<?> placeNavigation = provider.getNewsEntryPlaceNavigation(entry);
            if(placeNavigation != null) {
                placeNavigation.configureAnchorElement(link);
            } else {
                link.removeFromParent();
                while(link.hasChildNodes()) {
                    Node firstChild = link.getChild(0);
                    firstChild.removeFromParent();
                    getElement().appendChild(firstChild);
                }
                addStyleName(UpdatesBoxResources.INSTANCE.css().updatesbox_item());
            }
        }
    }


}

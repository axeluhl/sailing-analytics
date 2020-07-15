package com.sap.sailing.gwt.home.desktop.partials.sharing;

import static com.google.gwt.dom.client.Style.Display.NONE;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.MetaElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.shared.ClientConfiguration;

public class SharingButtons extends Composite {

    private static final String RDFA_ATTRIBUTE_IDENTIFIER = "property";
    private static final String DEFAULT_TYPE_PROPERTY_VALUE = "website";
    private static final String OG_IMAGE = "og:image";
    private static final String OG_TYPE = "og:type";
    private static final String OG_TITLE = "og:title";
    private static final String OG_DESCRIPTION = "og:description";
    private static SharingButtonsUiBinder uiBinder = GWT.create(SharingButtonsUiBinder.class);

    interface SharingButtonsUiBinder extends UiBinder<Widget, SharingButtons> {
    }
    
    @UiField HTMLPanel htmlPanel;
    @UiField AnchorElement mail;
    @UiField AnchorElement twitter;
    @UiField AnchorElement facebook;

    public SharingButtons() {
        SharingButtonsResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        if (!ClientConfiguration.getInstance().isBrandingActive()) {
            htmlPanel.getElement().getStyle().setDisplay(NONE);
        } 
    }
    
    public void setUp(SharingMetadataProvider provider) {
        if (!ClientConfiguration.getInstance().isBrandingActive()) {
            return;
        }
        String shortText = provider.getShortText();
        String longText = provider.getLongText(Window.Location.getHref());
        appendOpenGraphMetaTags(provider);
        UrlBuilder mailtoLink = new UrlBuilder().setProtocol("mailto").setParameter("subject", shortText).setParameter("body", longText);
        // URLBuilder encodes spaces in parameters using "+" instead of "%20". This causes problems in Mail programs that do not decode "+" as space.
        mail.setHref(mailtoLink.buildString().replace("+", "%20"));
        UrlBuilder twitterLink = new UrlBuilder().setProtocol("https").setHost("twitter.com").setPath("intent/tweet").setParameter("text", shortText).setParameter("url", Window.Location.getHref()).setParameter("short_url_length", "8");
        twitter.setHref(twitterLink.buildString());
        UrlBuilder facebookLink = new UrlBuilder().setProtocol("https").setHost("www.facebook.com")
                .setPath("sharer/sharer.php").setParameter("u", Window.Location.getHref());
        facebook.setHref(facebookLink.buildString());
    }

    private void appendOpenGraphMetaTags(SharingMetadataProvider provider) {
        final Document document = Document.get();
        final ArrayList<MetaElement> metaElements = new ArrayList<>();
        final MetaElement descriptionMetaTag = findOrCreateMetaElement(OG_DESCRIPTION, document);
        descriptionMetaTag.setAttribute(RDFA_ATTRIBUTE_IDENTIFIER, OG_DESCRIPTION);
        descriptionMetaTag.setContent(provider.getDescription());
        metaElements.add(descriptionMetaTag);
        final MetaElement titleMetaTag = findOrCreateMetaElement(OG_TITLE, document);
        titleMetaTag.setAttribute(RDFA_ATTRIBUTE_IDENTIFIER, OG_TITLE);
        titleMetaTag.setContent(provider.getTitle());
        metaElements.add(titleMetaTag);
        final MetaElement typeMetaTag = findOrCreateMetaElement(OG_TYPE, document);
        typeMetaTag.setAttribute(RDFA_ATTRIBUTE_IDENTIFIER, OG_TYPE);
        typeMetaTag.setContent(DEFAULT_TYPE_PROPERTY_VALUE);
        metaElements.add(typeMetaTag);
        final MetaElement imageMetaTag = findOrCreateMetaElement(OG_IMAGE, document);
        imageMetaTag.setAttribute(RDFA_ATTRIBUTE_IDENTIFIER, OG_IMAGE);
        imageMetaTag.setContent(provider.getImageUrl());
        metaElements.add(imageMetaTag);
        appendMetaElements(metaElements);
    }

    private MetaElement findOrCreateMetaElement(final String tagId, final Document document) {
        final Element elementById = document.getElementById(tagId);
        MetaElement metaElement;
        if(elementById != null && elementById instanceof MetaElement) {
            metaElement = (MetaElement) elementById;
        }else {
            metaElement = document.createMetaElement();
        }
        metaElement.setId(tagId);
        metaElement.removeFromParent();
        return metaElement;
    }

    private void appendMetaElements(final ArrayList<MetaElement> metaElements) {
        final Document document = Document.get();
        final NodeList<Element> nodes = document.getElementsByTagName("head");
        final Element head = (Element) nodes.getItem(0);
        for(final MetaElement metaTag : metaElements) {
            head.appendChild(metaTag);
        }
    }
}

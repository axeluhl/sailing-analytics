package com.sap.sailing.gwt.regattaoverview.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.gwt.ui.client.shared.controls.ImagesBarCell;

public class SailingFlagsBuilder {
    
    private final static SailingFlagsTemplates imageTemplate = GWT.create(SailingFlagsTemplates.class);
    
    interface SailingFlagsTemplates extends SafeHtmlTemplates {
        /**
         * @param title
         *            the tool-tip to display for the image on mouse-over
         * @param upperFlag
         *            how to render the image; this needs to be an &lt;img&gt; tag, not enclosed by any other element,
         *            as returned by {@link ImagesBarCell#makeImagePrototype(ImageResource)}
         */
        @SafeHtmlTemplates.Template("<div title=\"{0}\" ><div style=\"{1}\"><div>{2}</div><div>{3}</div></div><div>{4}</div></div>")
        SafeHtml cell(String title, SafeStyles styles, SafeHtml upperFlag, SafeHtml lowerFlag, SafeHtml directionArrow);
    }
    
    protected static SailingFlagsTemplates getImageTemplate() {
        return imageTemplate;
    }
    
    protected static AbstractImagePrototype makeImagePrototype(ImageResource resource) {
        return AbstractImagePrototype.create(resource);
    }
    
    protected static SafeStyles getImageStyle() {
        return SafeStylesUtils.fromTrustedString("float:left;padding-right:10px;");
    }
    
    public static SafeHtml render(Flags upperFlag, Flags lowerFlag, boolean isDisplayed, boolean displayStateChanged, String tooltip) {
        FlagImageResolver flagImageResolver = new FlagImageResolver();
        ImageResource upperFlagImage = flagImageResolver.resolveFlagToImage(upperFlag, isDisplayed, displayStateChanged);
        ImageResource lowerFlagImage = flagImageResolver.resolveFlagToImage(lowerFlag, isDisplayed, displayStateChanged);
        ImageResource directionImage = flagImageResolver.resolveFlagDirectionToImage(isDisplayed, displayStateChanged);
        
        SafeHtml upperFlagHtml = (upperFlagImage != null) ? makeImagePrototype(upperFlagImage).getSafeHtml() : SafeHtmlUtils.EMPTY_SAFE_HTML;
        SafeHtml lowerFlagHtml = (lowerFlagImage != null) ? makeImagePrototype(lowerFlagImage).getSafeHtml() : SafeHtmlUtils.EMPTY_SAFE_HTML;
        SafeHtml directionHtml = (directionImage != null) ? makeImagePrototype(directionImage).getSafeHtml() : SafeHtmlUtils.EMPTY_SAFE_HTML;
        
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        SafeStyles imgStyle = getImageStyle();
        SafeHtml rendered = getImageTemplate().cell(tooltip, imgStyle,
                upperFlagHtml, lowerFlagHtml, directionHtml);
        builder.append(rendered);
        return builder.toSafeHtml();
    }

}
